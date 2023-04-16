/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.services

import java.util.concurrent.ForkJoinPool

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt

import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.config.Config
import spray.json.DefaultJsonProtocol.*
import spray.json.JsObject
import spray.json.JsString
import spray.json.JsValue
import spray.json.JsonFormat
import spray.json.JsonReader
import spray.json.RootJsonReader
import spray.json.RootJsonWriter
import spray.json.deserializationError
import spray.json.enrichAny

import AnyOps.*
import stores.application.Serializers.given
import stores.application.actors.commands.DittoCommand.{RaiseAlarm, ShowItemData}
import stores.application.actors.commands.MessageBrokerCommand.{
  CatalogItemLifted as CatalogItemLiftedCommand,
  ItemReturned as ItemReturnedCommand
}
import stores.application.actors.commands.{DittoCommand, MessageBrokerCommand}
import stores.application.routes.entities.Entity.*
import stores.store.Repository
import stores.store.domainevents.*
import stores.store.valueobjects.{CatalogItem, Currency, ItemId, StoreId}

trait ItemStateHandlers {

  def onItemInserted(event: ItemInsertedInDropSystem): Validated[Unit]

  def onItemReturned(event: ItemReturned): Validated[Unit]

  def onCatalogItemLiftingRegistered(event: CatalogItemLiftingRegistered)(using Repository): Validated[Unit]

  def onItemDetected(event: ItemDetected): Validated[Unit]
}

object ItemStateHandlers extends SprayJsonSupport {

  case object EventRejected extends ValidationError {

    override val message: String = "The event could not be properly handled"
  }

  private class ItemStateHandlersImpl(
    messageBrokerActor: ActorRef[MessageBrokerCommand],
    dittoActor: ActorRef[DittoCommand],
    itemServerConfig: Config,
    httpClient: HttpExt
  ) extends ItemStateHandlers {

    private val itemURI: String = s"http://${itemServerConfig.getString("hostName")}:${itemServerConfig.getString("portNumber")}/"
    private val duration: Duration = 60.seconds
    private given ExecutionContext = ExecutionContext.fromExecutor(ForkJoinPool.commonPool())
    private given ActorSystem = httpClient.system.classicSystem

    private case class CatalogItemRequest(id: CatalogItem, store: StoreId)

    private given RootJsonWriter[CatalogItemRequest] = jsonFormat2(CatalogItemRequest.apply)

    private case class Price(amount: Double, currency: Currency)

    private given JsonFormat[Price] = jsonFormat2(Price.apply)

    private case class CatalogItemData(category: Long, price: Price)

    private given JsonFormat[CatalogItemData] = jsonFormat2(CatalogItemData.apply)

    private case class ItemCategoryRequest(id: Long)

    private given RootJsonWriter[ItemCategoryRequest] = jsonFormat1(ItemCategoryRequest.apply)

    private case class ItemCategoryData(name: String, description: String)

    private given JsonFormat[ItemCategoryData] = jsonFormat2(ItemCategoryData.apply)

    override def onItemInserted(event: ItemInsertedInDropSystem): Validated[Unit] =
      Await
        .result(
          (for {
            cr <- httpClient.singleRequest(Get(itemURI + "catalog_item", CatalogItemRequest(event.catalogItem, event.storeId)))
            c <- Unmarshal(cr.entity).to[ResultResponseEntity[CatalogItemData]]
            ir <- httpClient.singleRequest(Get(itemURI + "item_category", ItemCategoryRequest(c.result.category)))
            i <- Unmarshal(ir.entity).to[ResultResponseEntity[ItemCategoryData]]
          } yield Right[ValidationError, ShowItemData](
            ShowItemData(event.storeId, i.result.name, i.result.description, c.result.price.amount, c.result.price.currency)
          ))
            .fallbackTo(Future.successful(Left[ValidationError, ShowItemData](EventRejected))),
          duration
        )
        .map(dittoActor ! _)

    override def onItemReturned(event: ItemReturned): Validated[Unit] =
      Right[ValidationError, Unit](messageBrokerActor ! ItemReturnedCommand(event))

    override def onCatalogItemLiftingRegistered(event: CatalogItemLiftingRegistered)(using Repository): Validated[Unit] =
      summon[Repository]
        .findById(event.storeId)
        .map(s =>
          for {
            shelvingGroup <- s.layout.find(_.shelvingGroupId === event.shelvingGroupId)
            shelving <- shelvingGroup.shelvings.find(_.shelvingId === event.shelvingId)
            shelf <- shelving.shelves.find(_.shelfId === event.shelfId)
            itemsRow <- shelf.itemsRows.find(_.itemsRowId === event.itemsRowId)
          } yield CatalogItemLifted(itemsRow.catalogItem, s.storeId)
        )
        .flatMap(_.toRight[ValidationError](EventRejected))
        .map(e => messageBrokerActor ! CatalogItemLiftedCommand(e))

    private case class ItemRequest(id: ItemId, kind: CatalogItem, store: StoreId)

    private given RootJsonWriter[ItemRequest] = jsonFormat3(ItemRequest.apply)

    private enum ItemState(val name: String) {

      case InPlaceItem extends ItemState(name = "InPlaceItem")

      case ReturnedItem extends ItemState(name = "ReturnedItem")

      case InCartItem extends ItemState(name = "InCartItem")
    }

    private given JsonFormat[ItemState] with {

      override def read(json: JsValue): ItemState = json.asJsObject.getFields("state") match {
        case Seq(JsString(state)) =>
          ItemState.values.find(_.name === state).getOrElse(deserializationError(msg = "Json format was not valid"))
        case _ => deserializationError(msg = "Json format was not valid")
      }

      override def write(itemState: ItemState): JsValue = JsObject(
        "state" -> itemState.name.toJson
      )
    }

    override def onItemDetected(event: ItemDetected): Validated[Unit] =
      Await
        .result(
          (for {
            ir <- httpClient.singleRequest(Get(itemURI + "item", ItemRequest(event.itemId, event.catalogItem, event.storeId)))
            i <- Unmarshal(ir.entity).to[ResultResponseEntity[ItemState]]
          } yield Right[ValidationError, ItemState](i.result))
            .fallbackTo(Future.successful(Left[ValidationError, ItemState](EventRejected))),
          duration
        )
        .map {
          case ItemState.InCartItem => ()
          case _ => dittoActor ! RaiseAlarm(event.storeId)
        }
  }

  def apply(
    messageBrokerActor: ActorRef[MessageBrokerCommand],
    dittoActor: ActorRef[DittoCommand],
    itemServerConfig: Config,
    httpClient: HttpExt
  ): ItemStateHandlers =
    ItemStateHandlersImpl(messageBrokerActor, dittoActor, itemServerConfig, httpClient)
}

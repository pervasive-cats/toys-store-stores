/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.application.actors

import java.util.concurrent.CompletionException

import scala.jdk.OptionConverters.RichOptional
import scala.util.matching.Regex

import akka.actor.testkit.typed.scaladsl.TestProbe
import com.typesafe.config.Config
import eu.timepit.refined.auto.autoUnwrap
import org.eclipse.ditto.base.model.common.HttpStatus
import org.eclipse.ditto.client.DittoClient
import org.eclipse.ditto.client.live.messages.RepliableMessage
import org.eclipse.ditto.json.JsonArray
import org.eclipse.ditto.json.JsonObject
import org.eclipse.ditto.messages.model.MessageDirection
import org.eclipse.ditto.policies.model.PolicyId
import org.eclipse.ditto.things.model.Thing
import org.eclipse.ditto.things.model.ThingId
import org.eclipse.ditto.things.model.signals.commands.exceptions.ThingNotAccessibleException
import org.scalatest.Assertions.fail
import org.scalatest.OptionValues.*
import org.scalatest.matchers.should.Matchers.*
import spray.json.DefaultJsonProtocol
import spray.json.JsNumber
import spray.json.JsString
import spray.json.JsValue
import spray.json.enrichAny
import spray.json.enrichString

import stores.application.actors.DittoActor.DittoError
import stores.application.actors.commands.DittoCommand
import stores.application.actors.commands.DittoCommand.*
import stores.application.routes.entities.Entity.{ErrorResponseEntity, ResultResponseEntity}
import stores.store.valueobjects.*
import AnyOps.*

private object DittoActorTestHelper extends DefaultJsonProtocol {

  def antiTheftSystemThingId(storeId: StoreId)(dittoConfig: Config): ThingId =
    ThingId.of(s"${dittoConfig.getString("namespace")}:antiTheftSystem-${storeId.value}")

  def dropSystemThingId(storeId: StoreId)(dittoConfig: Config): ThingId =
    ThingId.of(s"${dittoConfig.getString("namespace")}:dropSystem-${storeId.value}")

  def shelvingThingId(
    storeId: StoreId,
    shelvingGroupId: ShelvingGroupId,
    shelvingId: ShelvingId
  )(
    dittoConfig: Config
  ): ThingId =
    ThingId.of(
      s"${dittoConfig.getString("namespace")}:shelving-${storeId.value}-${shelvingGroupId.value}-${shelvingId.value}"
    )

  def checkShelvingPresence(
    storeId: StoreId,
    shelvingGroupId: ShelvingGroupId,
    shelvingId: ShelvingId
  )(
    client: DittoClient,
    dittoConfig: Config
  ): Unit = {
    val list =
      client
        .twin
        .retrieve(shelvingThingId(storeId, shelvingGroupId, shelvingId)(dittoConfig))
        .toCompletableFuture
        .get()
    if (list.size() !== 1)
      fail()
    val thing: Thing = list.get(0)
    (thing.getDefinition.toScala, thing.getAttributes.toScala) match {
      case (Some(definition), Some(attributes)) =>
        definition.getUrl.toScala.value.toExternalForm shouldBe dittoConfig.getString("thingModelShelving")
        (
          attributes.getValue("id").toScala,
          attributes.getValue("store").toScala,
          attributes.getValue("shelvingGroup").toScala
        ) match {
          case (Some(h), Some(s), Some(g)) if h.isLong && s.isLong && g.isLong =>
            h.asLong() shouldBe (shelvingId.value: Long)
            s.asLong() shouldBe (storeId.value: Long)
            g.asLong() shouldBe (shelvingGroupId.value: Long)
          case _ => fail()
        }
      case _ => fail()
    }
  }

  def checkShelvingAbsence(
    storeId: StoreId,
    shelvingGroupId: ShelvingGroupId,
    shelvingId: ShelvingId
  )(
    client: DittoClient,
    dittoConfig: Config
  ): Unit =
    try {
      client
        .twin
        .retrieve(shelvingThingId(storeId, shelvingGroupId, shelvingId)(dittoConfig))
        .toCompletableFuture
        .get()
    } catch {
      case e: CompletionException =>
        e.getCause match {
          case e: ThingNotAccessibleException if e.getHttpStatus === HttpStatus.NOT_FOUND => ()
          case _ => fail()
        }
    }

  def createAntiTheftSystemThing(storeId: StoreId)(client: DittoClient, dittoConfig: Config): Unit =
    createThing(
      antiTheftSystemThingId(storeId)(dittoConfig),
      "thingModelAntiTheftSystem",
      JsonObject
        .newBuilder
        .set("storeId", storeId.value: Long)
        .build
    )(client, dittoConfig)

  def createDropSystemThing(storeId: StoreId)(client: DittoClient, dittoConfig: Config): Unit =
    createThing(
      dropSystemThingId(storeId)(dittoConfig),
      "thingModelDropSystem",
      JsonObject
        .newBuilder
        .set("store", storeId.value: Long)
        .build
    )(client, dittoConfig)

  private def createThing(
    thingId: ThingId,
    thingModel: String,
    attributes: JsonObject
  )(
    client: DittoClient,
    dittoConfig: Config
  ): Unit =
    client
      .twin
      .create(
        JsonObject
          .newBuilder
          .set("thingId", s"${thingId.getNamespace}:${thingId.getName}")
          .set("definition", dittoConfig.getString(thingModel))
          .set(
            "attributes",
            attributes
          )
          .build
      )
      .toCompletableFuture
      .get()

  def removeAntiTheftSystemThing(storeId: StoreId)(client: DittoClient, dittoConfig: Config): Unit =
    removeThing(antiTheftSystemThingId(storeId)(dittoConfig))(client)

  def removeDropSystemThing(storeId: StoreId)(client: DittoClient, dittoConfig: Config): Unit =
    removeThing(dropSystemThingId(storeId)(dittoConfig))(client)

  private def removeThing(thingId: ThingId)(client: DittoClient): Unit =
    client
      .twin
      .delete(thingId)
      .thenCompose(_ => client.policies().delete(PolicyId.of(thingId.getNamespace, thingId.getName)))
      .toCompletableFuture
      .get()

  private def sendReply(
    message: RepliableMessage[String, String],
    correlationId: String,
    status: HttpStatus,
    payload: String
  ): Unit = message.reply().httpStatus(status).correlationId(correlationId).payload(payload).send()

  private def handleMessage(
    message: RepliableMessage[String, String],
    messageHandler: (
      RepliableMessage[String, String],
      StoreId,
      Option[ShelvingGroupId],
      Option[ShelvingId],
      String,
      Seq[JsValue]
    ) => Unit,
    payloadFields: String*
  ): Unit = {
    val thingIdMatcherAntiTheftSystem: Regex = "antiTheftSystem-(?<store>[0-9]+)".r
    val thingIdMatcherDropSystem: Regex = "dropSystem-(?<store>[0-9]+)".r
    val thingIdMatcherShelving: Regex = "shelving-(?<store>[0-9]+)-(?<shelvingGroup>[0-9]+)-(?<shelving>[0-9]+)".r
    (message.getDirection, message.getEntityId.getName, message.getCorrelationId.toScala) match {
      case (MessageDirection.TO, thingIdMatcherAntiTheftSystem(store), Some(correlationId)) if store.toLongOption.isDefined =>
        StoreId(store.toLong).fold(
          error => sendReply(message, correlationId, HttpStatus.BAD_REQUEST, ErrorResponseEntity(error).toJson.compactPrint),
          storeId =>
            messageHandler(
              message,
              storeId,
              None,
              None,
              correlationId,
              message.getPayload.toScala.map(_.parseJson.asJsObject.getFields(payloadFields: _*)).getOrElse(Seq.empty[JsValue])
            )
        )
      case (MessageDirection.TO, thingIdMatcherDropSystem(store), Some(correlationId)) if store.toLongOption.isDefined =>
        StoreId(store.toLong).fold(
          error => sendReply(message, correlationId, HttpStatus.BAD_REQUEST, ErrorResponseEntity(error).toJson.compactPrint),
          storeId =>
            messageHandler(
              message,
              storeId,
              None,
              None,
              correlationId,
              message.getPayload.toScala.map(_.parseJson.asJsObject.getFields(payloadFields: _*)).getOrElse(Seq.empty[JsValue])
            )
        )
      case (MessageDirection.TO, thingIdMatcherShelving(store, shelvingGroup, shelving), Some(correlationId))
           if store.toLongOption.isDefined && shelvingGroup.toLongOption.isDefined && shelving.toLongOption.isDefined =>
        (for {
          storeId <- StoreId(store.toLong)
          shelvingGroupId <- ShelvingGroupId(shelvingGroup.toLong)
          shelvingId <- ShelvingId(shelving.toLong)
        } yield (storeId, shelvingGroupId, shelvingId)).fold(
          error => sendReply(message, correlationId, HttpStatus.BAD_REQUEST, ErrorResponseEntity(error).toJson.compactPrint),
          (storeId, shelvingGroupId, shelvingId) =>
            messageHandler(
              message,
              storeId,
              Some(shelvingGroupId),
              Some(shelvingId),
              correlationId,
              message.getPayload.toScala.map(_.parseJson.asJsObject.getFields(payloadFields: _*)).getOrElse(Seq.empty[JsValue])
            )
        )
      case _ => ()
    }
  }

  def registerForMessages(
    client: DittoClient,
    serviceProbe: TestProbe[DittoCommand],
    responseProbe: TestProbe[Validated[Unit]]
  ): Unit = {
    client
      .live
      .registerForMessage[String, String](
        "ditto_actor_raiseAlarm",
        "raiseAlarm",
        classOf[String],
        (msg: RepliableMessage[String, String]) =>
          handleMessage(
            msg,
            (msg, storeId, _, _, correlationId, _) => {
              serviceProbe ! RaiseAlarm(storeId)
              sendReply(
                msg,
                correlationId,
                HttpStatus.OK,
                ResultResponseEntity(()).toJson.compactPrint
              )
            }
          )
      )
    client
      .live
      .registerForMessage[String, String](
        "ditto_actor_showItemData",
        "showItemData",
        classOf[String],
        (msg: RepliableMessage[String, String]) =>
          handleMessage(
            msg,
            (msg, storeId, _, _, correlationId, fields) =>
              fields match {
                case Seq(JsNumber(amount), JsString(currency), JsString(description), JsString(name)) =>
                  serviceProbe ! ShowItemData(storeId, name, description, amount.doubleValue, Currency.withName(currency))
                  sendReply(
                    msg,
                    correlationId,
                    HttpStatus.OK,
                    ResultResponseEntity(()).toJson.compactPrint
                  )
                case _ =>
                  sendReply(
                    msg,
                    correlationId,
                    HttpStatus.BAD_REQUEST,
                    ErrorResponseEntity(DittoError).toJson.compactPrint
                  )
              },
            "amount",
            "currency",
            "description",
            "name"
          )
      )
    client
      .live
      .registerForMessage[String, String](
        "ditto_actor_addShelf",
        "addShelf",
        classOf[String],
        (msg: RepliableMessage[String, String]) =>
          handleMessage(
            msg,
            (msg, storeId, shelvingGroupId, shelvingId, correlationId, fields) =>
              fields match {
                case Seq(JsNumber(shelfId)) =>
                  serviceProbe ! AddShelf(
                    storeId,
                    shelvingGroupId.getOrElse(fail()),
                    shelvingId.getOrElse(fail()),
                    ShelfId(shelfId.toLong).getOrElse(fail()),
                    responseProbe.ref
                  )
                  sendReply(
                    msg,
                    correlationId,
                    HttpStatus.OK,
                    ResultResponseEntity(()).toJson.compactPrint
                  )
                case _ =>
                  sendReply(
                    msg,
                    correlationId,
                    HttpStatus.BAD_REQUEST,
                    ErrorResponseEntity(DittoError).toJson.compactPrint
                  )
              },
            "shelfId"
          )
      )
    client
      .live
      .registerForMessage[String, String](
        "ditto_actor_removeShelf",
        "removeShelf",
        classOf[String],
        (msg: RepliableMessage[String, String]) =>
          handleMessage(
            msg,
            (msg, storeId, shelvingGroupId, shelvingId, correlationId, fields) =>
              fields match {
                case Seq(JsNumber(shelfId)) =>
                  serviceProbe ! RemoveShelf(
                    storeId,
                    shelvingGroupId.getOrElse(fail()),
                    shelvingId.getOrElse(fail()),
                    ShelfId(shelfId.toLong).getOrElse(fail()),
                    responseProbe.ref
                  )
                  sendReply(
                    msg,
                    correlationId,
                    HttpStatus.OK,
                    ResultResponseEntity(()).toJson.compactPrint
                  )
                case _ =>
                  sendReply(
                    msg,
                    correlationId,
                    HttpStatus.BAD_REQUEST,
                    ErrorResponseEntity(DittoError).toJson.compactPrint
                  )
              },
            "shelfId"
          )
      )
    client
      .live
      .registerForMessage[String, String](
        "ditto_actor_addItemsRow",
        "addItemsRow",
        classOf[String],
        (msg: RepliableMessage[String, String]) =>
          handleMessage(
            msg,
            (msg, storeId, shelvingGroupId, shelvingId, correlationId, fields) =>
              fields match {
                case Seq(JsNumber(shelfId), JsNumber(itemsRowId)) =>
                  serviceProbe ! AddItemsRow(
                    storeId,
                    shelvingGroupId.getOrElse(fail()),
                    shelvingId.getOrElse(fail()),
                    ShelfId(shelfId.toLong).getOrElse(fail()),
                    ItemsRowId(itemsRowId.toLong).getOrElse(fail()),
                    responseProbe.ref
                  )
                  sendReply(
                    msg,
                    correlationId,
                    HttpStatus.OK,
                    ResultResponseEntity(()).toJson.compactPrint
                  )
                case _ =>
                  sendReply(
                    msg,
                    correlationId,
                    HttpStatus.BAD_REQUEST,
                    ErrorResponseEntity(DittoError).toJson.compactPrint
                  )
              },
            "shelfId",
            "itemsRowId"
          )
      )
    client
      .live
      .registerForMessage[String, String](
        "ditto_actor_removeItemsRow",
        "removeItemsRow",
        classOf[String],
        (msg: RepliableMessage[String, String]) =>
          handleMessage(
            msg,
            (msg, storeId, shelvingGroupId, shelvingId, correlationId, fields) =>
              fields match {
                case Seq(JsNumber(shelfId), JsNumber(itemsRowId)) =>
                  serviceProbe ! RemoveItemsRow(
                    storeId,
                    shelvingGroupId.getOrElse(fail()),
                    shelvingId.getOrElse(fail()),
                    ShelfId(shelfId.toLong).getOrElse(fail()),
                    ItemsRowId(itemsRowId.toLong).getOrElse(fail()),
                    responseProbe.ref
                  )
                  sendReply(
                    msg,
                    correlationId,
                    HttpStatus.OK,
                    ResultResponseEntity(()).toJson.compactPrint
                  )
                case _ =>
                  sendReply(
                    msg,
                    correlationId,
                    HttpStatus.BAD_REQUEST,
                    ErrorResponseEntity(DittoError).toJson.compactPrint
                  )
              },
            "shelfId",
            "itemsRowId"
          )
      )
  }
}

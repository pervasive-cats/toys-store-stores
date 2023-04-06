/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.application.actors

import stores.application.actors.MessageBrokerActor
import stores.application.Serializers.given
import stores.application.actors.commands.DittoCommand.*
import stores.store.Repository
import AnyOps.===
import stores.application.actors.commands.*
import stores.application.actors.commands.RootCommand.Startup
import stores.application.routes.entities.Entity.{ErrorResponseEntity, ResultResponseEntity}
import stores.store.Repository.StoreNotFound
import stores.store.domainevents.{
  CatalogItemLiftingRegistered as CatalogItemLiftingRegisteredEvent,
  ItemDetected as ItemDetectedEvent,
  ItemInsertedInDropSystem as ItemInsertedInDropSystemEvent,
  ItemReturned as ItemReturnedEvent
}
import stores.store.entities.Store
import stores.store.services.ItemStateHandlers
import stores.store.valueobjects.*

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.typesafe.config.Config
import eu.timepit.refined.auto.autoUnwrap
import org.eclipse.ditto.base.model.common.HttpStatus
import org.eclipse.ditto.client.{DittoClient, DittoClients}
import org.eclipse.ditto.client.configuration.*
import org.eclipse.ditto.client.live.commands.LiveCommandHandler
import org.eclipse.ditto.client.live.messages.{MessageSender, RepliableMessage}
import org.eclipse.ditto.client.messaging.{AuthenticationProviders, MessagingProviders}
import org.eclipse.ditto.client.options.Options
import org.eclipse.ditto.json.JsonObject
import org.eclipse.ditto.messages.model.{MessageDirection, Message as DittoMessage}
import org.eclipse.ditto.policies.model.PolicyId
import org.eclipse.ditto.things.model.*
import org.eclipse.ditto.things.model.signals.commands.exceptions.ThingNotAccessibleException
import spray.json.{enrichAny, enrichString, JsNumber, JsObject, JsValue}

import java.net.http.HttpHeaders
import java.util.concurrent.{CompletionException, ForkJoinPool}
import java.util.function.{BiConsumer, BiFunction}
import java.util.regex.Pattern
import javax.sql.DataSource
import scala.concurrent.*
import scala.concurrent.duration.DurationInt
import scala.jdk.OptionConverters.RichOptional
import scala.util.{Failure, Success}
import scala.util.matching.Regex

object DittoActor extends SprayJsonSupport {

  case object DittoError extends ValidationError {

    override val message: String = "An error with the Ditto service was encountered."
  }

  private def sendReply(
    message: RepliableMessage[String, String],
    correlationId: String,
    status: HttpStatus,
    payload: Option[String]
  ): Unit = {
    val msg: MessageSender.SetPayloadOrSend[String] = message.reply().httpStatus(status).correlationId(correlationId)
    payload match {
      case Some(p) => msg.payload(p)
      case None => ()
    }
    msg.send()
  }

  @SuppressWarnings(Array("org.wartremover.warts.Null", "scalafix:DisableSyntax.null"))
  private def responseHandler[T]: ActorRef[Validated[Unit]] => BiConsumer[T, Throwable] =
    r =>
      (_, t) =>
        if (t === null)
          r ! Right[ValidationError, Unit](())
        else
          t.getCause match {
            case e: ThingNotAccessibleException if e.getHttpStatus === HttpStatus.NOT_FOUND =>
              r ! Left[ValidationError, Unit](StoreNotFound)
            case _ => r ! Left[ValidationError, Unit](DittoError)
          }

  private def sendMessage(
    client: DittoClient,
    thingId: CharSequence,
    subject: String,
    payload: Option[JsonObject],
    replyTo: Option[ActorRef[Validated[Unit]]]
  ): Unit = {
    val message: MessageSender.SetPayloadOrSend[JsonObject] =
      client
        .live()
        .forId(ThingId.of(thingId))
        .message()
        .to() // message to device
        .subject(subject)
    (payload, replyTo) match {
      case (Some(p), Some(r)) => message.payload(p).send(classOf[String], responseHandler(r))
      case (None, Some(r)) => message.send(classOf[String], responseHandler(r))
      case (Some(p), None) => message.payload(p).send()
      case _ => message.send()
    }
  }

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
      case (MessageDirection.FROM, thingIdMatcherAntiTheftSystem(store), Some(correlationId)) if store.toLongOption.isDefined =>
        StoreId(store.toLong).fold(
          error =>
            sendReply(message, correlationId, HttpStatus.BAD_REQUEST, Some(ErrorResponseEntity(error).toJson.compactPrint)),
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
      case (MessageDirection.FROM, thingIdMatcherDropSystem(store), Some(correlationId)) if store.toLongOption.isDefined =>
        StoreId(store.toLong).fold(
          error =>
            sendReply(message, correlationId, HttpStatus.BAD_REQUEST, Some(ErrorResponseEntity(error).toJson.compactPrint)),
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
      case (MessageDirection.FROM, thingIdMatcherShelving(store, shelvingGroup, shelving), Some(correlationId))
           if store.toLongOption.isDefined && shelvingGroup.toLongOption.isDefined && shelving.toLongOption.isDefined =>
        (for {
          storeId <- StoreId(store.toLong)
          shelvingGroupId <- ShelvingGroupId(shelvingGroup.toLong)
          shelvingId <- ShelvingId(shelving.toLong)
        } yield (storeId, shelvingGroupId, shelvingId)).fold(
          error =>
            sendReply(message, correlationId, HttpStatus.BAD_REQUEST, Some(ErrorResponseEntity(error).toJson.compactPrint)),
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

  @SuppressWarnings(Array("org.wartremover.warts.Null", "scalafix:DisableSyntax.null", "org.wartremover.warts.OptionPartial"))
  def apply(
    root: ActorRef[RootCommand],
    messageBrokerActor: ActorRef[MessageBrokerCommand],
    dataSource: DataSource,
    dittoConfig: Config
  ): Behavior[DittoCommand] =
    Behaviors.setup[DittoCommand] { ctx =>
      val disconnectedDittoClient = DittoClients.newInstance(
        MessagingProviders.webSocket(
          WebSocketMessagingConfiguration
            .newBuilder
            .endpoint(s"ws://${dittoConfig.getString("hostName")}:${dittoConfig.getString("portNumber")}/ws/2")
            .connectionErrorHandler(_ => root ! Startup(success = false))
            .build,
          AuthenticationProviders.basic(
            BasicAuthenticationConfiguration
              .newBuilder
              .username(dittoConfig.getString("username"))
              .password(dittoConfig.getString("password"))
              .build
          )
        )
      )
      disconnectedDittoClient
        .connect
        .thenAccept(ctx.self ! DittoClientConnected(_))
        .exceptionally { _ =>
          disconnectedDittoClient.destroy()
          root ! Startup(success = false)
          null
        }
      Behaviors.receiveMessage {
        case DittoClientConnected(client) =>
          client
            .live
            .startConsumption(
              Options.Consumption.namespaces(dittoConfig.getString("namespace"))
            )
            .thenRun(() => ctx.self ! DittoMessagesIncoming)
            .exceptionally { _ =>
              disconnectedDittoClient.destroy()
              root ! Startup(success = false)
              null
            }
          Behaviors.receiveMessage {
            case DittoMessagesIncoming =>
              client
                .live
                .registerForMessage[String, String](
                  "ditto_actor_itemDetected",
                  "itemDetected",
                  classOf[String],
                  (msg: RepliableMessage[String, String]) =>
                    handleMessage(
                      msg,
                      (msg, storeId, _, _, correlationId, fields) =>
                        fields match {
                          case Seq(JsNumber(catalogItem), JsNumber(itemId)) if catalogItem.isValidLong && itemId.isValidLong =>
                            (for {
                              k <- CatalogItem(catalogItem.longValue)
                              i <- ItemId(itemId.longValue)
                            } yield ctx.self ! ItemDetected(storeId, k, i)).fold(
                              e =>
                                sendReply(
                                  msg,
                                  correlationId,
                                  HttpStatus.BAD_REQUEST,
                                  Some(ErrorResponseEntity(e).toJson.compactPrint)
                                ),
                              _ =>
                                sendReply(msg, correlationId, HttpStatus.OK, Some(ResultResponseEntity(()).toJson.compactPrint))
                            )
                          case _ =>
                            sendReply(
                              msg,
                              correlationId,
                              HttpStatus.BAD_REQUEST,
                              Some(ErrorResponseEntity(DittoError).toJson.compactPrint)
                            )
                        },
                      "catalogItemId",
                      "itemId"
                    )
                )
              client
                .live()
                .registerForMessage[String, String](
                  "ditto_actor_itemInsertedIntoDropSystem",
                  "itemInsertedIntoDropSystem",
                  classOf[String],
                  (msg: RepliableMessage[String, String]) =>
                    handleMessage(
                      msg,
                      (msg, storeId, _, _, correlationId, fields) =>
                        fields match {
                          case Seq(JsNumber(catalogItem), JsNumber(itemId)) if catalogItem.isValidLong && itemId.isValidLong =>
                            (for {
                              k <- CatalogItem(catalogItem.longValue)
                              i <- ItemId(itemId.longValue)
                            } yield ctx.self ! ItemInsertedIntoDropSystem(storeId, k, i)).fold(
                              e =>
                                sendReply(
                                  msg,
                                  correlationId,
                                  HttpStatus.BAD_REQUEST,
                                  Some(ErrorResponseEntity(e).toJson.compactPrint)
                                ),
                              _ =>
                                sendReply(msg, correlationId, HttpStatus.OK, Some(ResultResponseEntity(()).toJson.compactPrint))
                            )
                          case _ =>
                            sendReply(
                              msg,
                              correlationId,
                              HttpStatus.BAD_REQUEST,
                              Some(ErrorResponseEntity(DittoError).toJson.compactPrint)
                            )
                        },
                      "catalogItem",
                      "itemId"
                    )
                )
              client
                .live()
                .registerForMessage[String, String](
                  "ditto_actor_itemReturned",
                  "itemReturned",
                  classOf[String],
                  (msg: RepliableMessage[String, String]) =>
                    handleMessage(
                      msg,
                      (msg, storeId, _, _, correlationId, fields) =>
                        fields match {
                          case Seq(JsNumber(catalogItem), JsNumber(itemId)) if catalogItem.isValidLong && itemId.isValidLong =>
                            (for {
                              k <- CatalogItem(catalogItem.longValue)
                              i <- ItemId(itemId.longValue)
                            } yield ctx.self ! ItemReturned(storeId, k, i)).fold(
                              e =>
                                sendReply(
                                  msg,
                                  correlationId,
                                  HttpStatus.BAD_REQUEST,
                                  Some(ErrorResponseEntity(e).toJson.compactPrint)
                                ),
                              _ =>
                                sendReply(msg, correlationId, HttpStatus.OK, Some(ResultResponseEntity(()).toJson.compactPrint))
                            )
                          case _ =>
                            sendReply(
                              msg,
                              correlationId,
                              HttpStatus.BAD_REQUEST,
                              Some(ErrorResponseEntity(DittoError).toJson.compactPrint)
                            )
                        },
                      "catalogItem",
                      "itemId"
                    )
                )
              client
                .live()
                .registerForMessage[String, String](
                  "ditto_actor_catalogItemLiftingRegistered",
                  "catalogItemLiftingRegistered",
                  classOf[String],
                  (msg: RepliableMessage[String, String]) =>
                    handleMessage(
                      msg,
                      (msg, storeId, shelvingGroupId, shelvingId, correlationId, fields) =>
                        fields match {
                          case Seq(JsNumber(shelfId), JsNumber(itemsRowId))
                               if shelfId.isValidLong && itemsRowId.isValidLong
                               && shelvingGroupId.isDefined && shelvingId.isDefined =>
                            (for {
                              shelfId <- ShelfId(shelfId.longValue)
                              itemsRowId <- ItemsRowId(itemsRowId.longValue)
                            } yield ctx.self ! CatalogItemLiftingRegistered(
                              storeId,
                              shelvingGroupId.get,
                              shelvingId.get,
                              shelfId,
                              itemsRowId
                            )).fold(
                              e =>
                                sendReply(
                                  msg,
                                  correlationId,
                                  HttpStatus.BAD_REQUEST,
                                  Some(ErrorResponseEntity(e).toJson.compactPrint)
                                ),
                              _ =>
                                sendReply(msg, correlationId, HttpStatus.OK, Some(ResultResponseEntity(()).toJson.compactPrint))
                            )
                          case _ =>
                            sendReply(
                              msg,
                              correlationId,
                              HttpStatus.BAD_REQUEST,
                              Some(ErrorResponseEntity(DittoError).toJson.compactPrint)
                            )
                        },
                      "shelfId",
                      "itemsRowId"
                    )
                )
              onDittoMessagesIncoming(root, client, messageBrokerActor, dataSource, dittoConfig)
            case _ => Behaviors.unhandled[DittoCommand]
          }
        case _ => Behaviors.unhandled[DittoCommand]
      }
    }

  private def onDittoMessagesIncoming(
    root: ActorRef[RootCommand],
    client: DittoClient,
    messageBrokerActor: ActorRef[MessageBrokerCommand],
    dataSource: DataSource,
    dittoConfig: Config
  ): Behavior[DittoCommand] = {
    root ! Startup(success = true)
    Behaviors.receive { (ctx, msg) =>
      val itemStateHandlers: ItemStateHandlers = ItemStateHandlers(messageBrokerActor, ctx.self)
      given Repository = Repository(dataSource)
      msg match {
        case RaiseAlarm(storeId) =>
          sendMessage(
            client,
            s"${dittoConfig.getString("namespace")}:antiTheftSystem-${storeId.value}",
            "raiseAlarm",
            None,
            None
          )
          Behaviors.same[DittoCommand]
        case ShowItemData(storeId, name, description, amount, currency) =>
          sendMessage(
            client,
            s"${dittoConfig.getString("namespace")}:dropSystem-${storeId.value}",
            "showItemData",
            Some(
              JsonObject.of(
                JsObject(
                  "name" -> name.toJson,
                  "description" -> description.toJson,
                  "amount" -> amount.toJson,
                  "currency" -> currency.toString.toJson
                ).compactPrint
              )
            ),
            None
          )
          Behaviors.same[DittoCommand]
        case AddShelf(storeId, shelvingGroupId, shelvingId, shelfId) =>
          sendMessage(
            client,
            s"${dittoConfig.getString("namespace")}:shelving-${storeId.value}-${shelvingGroupId.value}-${shelvingId.value}",
            "addShelf",
            Some(
              JsonObject.of(
                JsObject(
                  "shelfId" -> shelfId.value.toLong.toJson
                ).compactPrint
              )
            ),
            None
          )
          Behaviors.same[DittoCommand]
        case RemoveShelf(storeId, shelvingGroupId, shelvingId, shelfId) =>
          sendMessage(
            client,
            s"${dittoConfig.getString("namespace")}:shelving-${storeId.value}-${shelvingGroupId.value}-${shelvingId.value}",
            "removeShelf",
            Some(
              JsonObject.of(
                JsObject(
                  "shelfId" -> shelfId.value.toLong.toJson
                ).compactPrint
              )
            ),
            None
          )
          Behaviors.same[DittoCommand]
        case AddItemsRow(storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId) =>
          sendMessage(
            client,
            s"${dittoConfig.getString("namespace")}:shelving-${storeId.value}-${shelvingGroupId.value}-${shelvingId.value}",
            "addItemsRow",
            Some(
              JsonObject.of(
                JsObject(
                  "shelfId" -> shelfId.value.toLong.toJson,
                  "itemsRowId" -> itemsRowId.value.toLong.toJson
                ).compactPrint
              )
            ),
            None
          )
          Behaviors.same[DittoCommand]
        case RemoveItemsRow(storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId) =>
          sendMessage(
            client,
            s"${dittoConfig.getString("namespace")}:shelving-${storeId.value}-${shelvingGroupId.value}-${shelvingId.value}",
            "removeItemsRow",
            Some(
              JsonObject.of(
                JsObject(
                  "shelfId" -> shelfId.value.toLong.toJson,
                  "itemsRowId" -> itemsRowId.value.toLong.toJson
                ).compactPrint
              )
            ),
            None
          )
          Behaviors.same[DittoCommand]
        case ItemDetected(storeId, catalogItem, itemId) =>
          itemStateHandlers.onItemDetected(ItemDetectedEvent(itemId, catalogItem, storeId))
          Behaviors.same[DittoCommand]
        case ItemInsertedIntoDropSystem(storeId, catalogItem, itemId) =>
          itemStateHandlers.onItemInserted(ItemInsertedInDropSystemEvent(catalogItem, itemId, storeId))
          Behaviors.same[DittoCommand]
        case ItemReturned(storeId, catalogItem, itemId) =>
          itemStateHandlers.onItemReturned(ItemReturnedEvent(catalogItem, itemId, storeId))
          Behaviors.same[DittoCommand]
        case CatalogItemLiftingRegistered(storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId) =>
          itemStateHandlers.onCatalogItemLiftingRegistered(
            CatalogItemLiftingRegisteredEvent(storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId)
          )
          Behaviors.same[DittoCommand]
      }
    }
  }
}

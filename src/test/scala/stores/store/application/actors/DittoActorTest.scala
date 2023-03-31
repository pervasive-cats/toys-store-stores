/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.application.actors

import stores.store.valueobjects.{CatalogItem, ItemId, ItemsRowId, ShelfId, ShelvingGroupId, ShelvingId, StoreId}

import scala.jdk.OptionConverters.RichOptional
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import akka.actor.{ActorSystem, ActorRef as UntypedActorRef}
import akka.http.scaladsl.client.RequestBuilding.{Get, Post}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.ws.*
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{CompletionStrategy, OverflowStrategy}
import com.typesafe.config.{Config, ConfigFactory}
import eu.timepit.refined.auto.autoUnwrap
import stores.application.actors.DittoActor
import stores.application.actors.commands.DittoCommand.*
import stores.application.actors.commands.RootCommand.Startup
import stores.application.actors.commands.{Currency, DittoCommand, MessageBrokerCommand, RootCommand}
import stores.store.entities.Store
import stores.application.routes.entities.Entity.ResultResponseEntity
import stores.application.actors.DittoActor.DittoError

import org.eclipse.ditto.base.model.common.HttpStatus
import org.eclipse.ditto.client.configuration.{BasicAuthenticationConfiguration, WebSocketMessagingConfiguration}
import org.eclipse.ditto.client.{DittoClient, DittoClients}
import org.eclipse.ditto.client.live.messages.RepliableMessage
import org.eclipse.ditto.client.messaging.{AuthenticationProviders, MessagingProviders}
import org.eclipse.ditto.client.options.Options
import spray.json.DefaultJsonProtocol.IntJsonFormat
import org.eclipse.ditto.json.JsonObject
import org.eclipse.ditto.json.JsonArray
import org.eclipse.ditto.messages.model.MessageDirection
import org.eclipse.ditto.things.model.ThingId
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.{BeforeAndAfterAll, DoNotDiscover, Ignore, Tag}
import spray.json.{JsBoolean, JsNumber, JsObject, JsString, JsValue, enrichAny, enrichString}
import stores.application.Serializers.given

import java.util.concurrent.{CountDownLatch, ForkJoinPool, TimeUnit}
import java.util.function.BiConsumer
import java.util.regex.Pattern
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex
import stores.application.routes.entities.Entity.{ErrorResponseEntity, ResultResponseEntity}

import org.eclipse.ditto.policies.model.PolicyId

import scala.util.{Failure, Success} // scalafix:ok

@DoNotDiscover
class DittoActorTest extends AnyFunSpec with BeforeAndAfterAll with SprayJsonSupport {

  private val testKit: ActorTestKit = ActorTestKit()
  private val rootActorProbe: TestProbe[RootCommand] = testKit.createTestProbe[RootCommand]()
  private val messageBrokerActorProbe: TestProbe[MessageBrokerCommand] = testKit.createTestProbe[MessageBrokerCommand]()
  private val serviceProbe: TestProbe[DittoCommand] = testKit.createTestProbe[DittoCommand]()
  private val config: Config = ConfigFactory.load()

  private val dittoConfig: Config = config.getConfig("ditto")

  private val dittoActor: ActorRef[DittoCommand] =
    testKit.spawn(DittoActor(rootActorProbe.ref, messageBrokerActorProbe.ref, dittoConfig))

  @SuppressWarnings(Array("org.wartremover.warts.Var", "scalafix:DisableSyntax.var"))
  private var maybeClient: Option[DittoClient] = None

  private val storeId: StoreId = StoreId(2).getOrElse(fail())
  private val catalogItem: CatalogItem = CatalogItem(1).getOrElse(fail())
  private val itemId: ItemId = ItemId(1).getOrElse(fail())
  private val shelvingGroupId: ShelvingGroupId = ShelvingGroupId(7).getOrElse(fail())
  private val shelvingId: ShelvingId = ShelvingId(7).getOrElse(fail())
  private val shelfId: ShelfId = ShelfId(1).getOrElse(fail())
  private val itemsRowId: ItemsRowId = ItemsRowId(1).getOrElse(fail())

  private val defaultShelves: JsonArray = JsonArray
    .newBuilder()
    .add(
      JsonObject
        .newBuilder()
        .set("shelfId", shelfId.value)
        .set("itemRows", JsonArray.newBuilder().add(itemsRowId.value, 2, 3).build)
        .build
    )
    .add(
      JsonObject
        .newBuilder()
        .set("shelfId", 2)
        .set("itemRows", JsonArray.newBuilder().add(1, 2, 3).build)
        .build
    )
    .add(
      JsonObject
        .newBuilder()
        .set("shelfId", 3)
        .set("itemRows", JsonArray.newBuilder().add(1, 2, 3).build)
        .build
    )
    .build

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

  override def beforeAll(): Unit = {
    val disconnectedDittoClient = DittoClients.newInstance(
      MessagingProviders.webSocket(
        WebSocketMessagingConfiguration
          .newBuilder
          .endpoint(s"ws://${dittoConfig.getString("hostName")}:${dittoConfig.getString("portNumber")}/ws/2")
          .connectionErrorHandler(_ => fail())
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
    val client: DittoClient =
      disconnectedDittoClient
        .connect
        .exceptionally { _ =>
          disconnectedDittoClient.destroy()
          fail()
        }
        .toCompletableFuture
        .get()
    client
      .live
      .startConsumption(
        Options.Consumption.namespaces(dittoConfig.getString("namespace"))
      )
      .exceptionally { _ =>
        disconnectedDittoClient.destroy()
        fail()
      }
      .toCompletableFuture
      .get()
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
                  serviceProbe ! ShowItemData(storeId, name, description, amount.doubleValue, Currency.valueOf(currency))
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
                    ShelfId(shelfId.toLong).getOrElse(fail())
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
                    ShelfId(shelfId.toLong).getOrElse(fail())
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
                    ItemsRowId(itemsRowId.toLong).getOrElse(fail())
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
                    ItemsRowId(itemsRowId.toLong).getOrElse(fail())
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
    testKit.spawn(DittoActor(rootActorProbe.ref, testKit.createTestProbe[MessageBrokerCommand]().ref, dittoConfig))
    maybeClient = Some(client)
  }

  override def afterAll(): Unit = testKit.shutdownTestKit()

  private def antiTheftSystemThingId(storeId: StoreId): ThingId =
    ThingId.of(s"${dittoConfig.getString("namespace")}:antiTheftSystem-${storeId.value}")

  private def dropSystemThingId(storeId: StoreId): ThingId =
    ThingId.of(s"${dittoConfig.getString("namespace")}:dropSystem-${storeId.value}")

  private def shelvingThingId(storeId: StoreId, shelvingGroupId: ShelvingGroupId, shelvingId: ShelvingId): ThingId =
    ThingId.of(
      s"${dittoConfig.getString("namespace")}:shelving-${storeId.value}-${shelvingGroupId.value}-${shelvingId.value}"
    )

  private def createAntiTheftSystemThing(storeId: StoreId): Unit = createThing(
    antiTheftSystemThingId(storeId),
    "thingModelAntiTheftSystem",
    JsonObject
      .newBuilder
      .set("storeId", storeId.value: Long)
      .build
  )

  private def createDropSystemThing(storeId: StoreId): Unit = createThing(
    dropSystemThingId(storeId),
    "thingModelDropSystem",
    JsonObject
      .newBuilder
      .set("store", storeId.value: Long)
      .build
  )

  private def createShelvingThing(storeId: StoreId, shelvingGroupId: ShelvingGroupId, shelvingId: ShelvingId): Unit = createThing(
    shelvingThingId(storeId, shelvingGroupId, shelvingId),
    "thingModelShelving",
    JsonObject
      .newBuilder
      .set("storeId", storeId.value: Long)
      .set("shelvingGroupId", shelvingGroupId.value: Long)
      .set("id", shelvingId.value: Long)
      .set(
        "shelves",
        defaultShelves
      )
      .build
  )

  private def createThing(thingId: ThingId, thingModel: String, attributes: JsonObject): Unit =
    maybeClient
      .getOrElse(fail())
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

  private def removeAntiTheftSystemThing(storeId: StoreId): Unit = removeThing(antiTheftSystemThingId(storeId))

  private def removeDropSystemThing(storeId: StoreId): Unit = removeThing(dropSystemThingId(storeId))

  private def removeShelvingThing(storeId: StoreId, shelvingGroupId: ShelvingGroupId, shelvingId: ShelvingId): Unit = removeThing(
    shelvingThingId(storeId, shelvingGroupId, shelvingId)
  )

  private def removeThing(thingId: ThingId): Unit =
    maybeClient
      .getOrElse(fail())
      .twin
      .delete(thingId)
      .thenCompose(_ =>
        maybeClient
          .getOrElse(fail())
          .policies()
          .delete(PolicyId.of(thingId.getNamespace, thingId.getName))
      )
      .toCompletableFuture
      .get()

  describe("A Ditto actor") {
    describe("when first started up") {
      it("should notify the root actor of its start") {
        rootActorProbe.expectMessage(60.seconds, Startup(true))
      }
    }

    describe("when it receives a notification that an item is near an anti-theft system") {
      it("should sound the alarm if the item is not in cart") {
        val latch: CountDownLatch = CountDownLatch(1)
        createAntiTheftSystemThing(storeId)
        maybeClient
          .getOrElse(fail())
          .live
          .message[String]
          .from(antiTheftSystemThingId(storeId))
          .subject("itemDetected")
          .payload(JsObject("catalogItemId" -> catalogItem.toJson, "itemId" -> itemId.toJson).compactPrint)
          .send((_, t) => Option(t).fold(latch.countDown())(_ => fail()))
        latch.await(1, TimeUnit.MINUTES)
        serviceProbe.expectMessage[DittoCommand](1.minutes, RaiseAlarm(storeId))
        removeAntiTheftSystemThing(storeId)
      }
    }

    describe("when asked to raise a shop's alarm") {
      it("should sound the alarm") {
        createAntiTheftSystemThing(storeId)
        dittoActor ! RaiseAlarm(storeId)
        serviceProbe.expectMessage[DittoCommand](1.minutes, RaiseAlarm(storeId))
        removeAntiTheftSystemThing(storeId)
      }
    }

    describe("when notified of an item being inserted into the drop system") {
      it("should handle the event") {
        createDropSystemThing(storeId)
        val latch: CountDownLatch = CountDownLatch(1)
        maybeClient
          .getOrElse(fail())
          .live
          .message[String]
          .from(dropSystemThingId(storeId))
          .subject("itemInsertedIntoDropSystem")
          .payload(JsObject("catalogItem" -> catalogItem.toJson, "itemId" -> itemId.toJson).compactPrint)
          .send((_, t) => Option(t).fold(latch.countDown())(_ => fail()))
        latch.await(1, TimeUnit.MINUTES)
        // stoutput
        removeDropSystemThing(storeId)
      }
    }

    describe("when notified of an item being returned") {
      it("should self-send the corresponding message") {
        createDropSystemThing(storeId)
        val latch: CountDownLatch = CountDownLatch(1)
        maybeClient
          .getOrElse(fail())
          .live
          .message[String]
          .from(dropSystemThingId(storeId))
          .subject("itemReturned")
          .payload(JsObject("catalogItem" -> catalogItem.toJson, "itemId" -> itemId.toJson).compactPrint)
          .send((_, t) => Option(t).fold(latch.countDown())(_ => fail()))
        latch.await(1, TimeUnit.MINUTES)
        // stoutput
        removeDropSystemThing(storeId)
      }
    }

    describe("when ordering a drop system to display item information") {
      it("should correctly send the information") {
        val name = "Teddy"
        val description = "A soft bear plushie."
        val amount = 8.99
        val currency = Currency.EUR
        createDropSystemThing(storeId)
        dittoActor ! ShowItemData(storeId, name, description, amount, currency)
        serviceProbe.expectMessage[DittoCommand](1.minutes, ShowItemData(storeId, name, description, amount, currency))
        removeDropSystemThing(storeId)
      }
    }

    describe("when it receives a notification that an item has been lifted from a specific row of items of a shelf") {
      it("should handle said event") {
        val latch: CountDownLatch = CountDownLatch(1)
        createShelvingThing(storeId, shelvingGroupId, shelvingId)
        maybeClient
          .getOrElse(fail())
          .live
          .message[String]
          .from(shelvingThingId(storeId, shelvingGroupId, shelvingId))
          .subject("catalogItemLiftingRegistered")
          .payload(JsObject("shelfId" -> 40.toJson, "itemsRowId" -> 4.toJson).compactPrint)
          .send((_, t) => Option(t).fold(latch.countDown())(_ => fail()))
        latch.await(1, TimeUnit.MINUTES)
        // stoutput
        removeShelvingThing(storeId, shelvingGroupId, shelvingId)
      }
    }

    describe("when adding a shelf to a specific shelving") {
      it("should get added") {
        val newShelfId: ShelfId = ShelfId(11).getOrElse(fail())
        createShelvingThing(storeId, shelvingGroupId, shelvingId)
        dittoActor ! AddShelf(storeId, shelvingGroupId, shelvingId, newShelfId)
        serviceProbe.expectMessage[DittoCommand](1.minutes, AddShelf(storeId, shelvingGroupId, shelvingId, newShelfId))
        removeShelvingThing(storeId, shelvingGroupId, shelvingId)
      }
    }

    describe("when removing a shelf from a specific shelving") {
      it("should get removed") {
        createShelvingThing(storeId, shelvingGroupId, shelvingId)
        dittoActor ! RemoveShelf(storeId, shelvingGroupId, shelvingId, shelfId)
        serviceProbe.expectMessage[DittoCommand](1.minutes, RemoveShelf(storeId, shelvingGroupId, shelvingId, shelfId))
        removeShelvingThing(storeId, shelvingGroupId, shelvingId)
      }
    }

    describe("when adding a row of items to a specific shelf") {
      it("should get added") {
        val newItemsRowId: ItemsRowId = ItemsRowId(11).getOrElse(fail())
        createShelvingThing(storeId, shelvingGroupId, shelvingId)
        dittoActor ! AddItemsRow(storeId, shelvingGroupId, shelvingId, shelfId, newItemsRowId)
        serviceProbe
          .expectMessage[DittoCommand](1.minutes, AddItemsRow(storeId, shelvingGroupId, shelvingId, shelfId, newItemsRowId))
        removeShelvingThing(storeId, shelvingGroupId, shelvingId)
      }
    }

    describe("when adding a row of items from a specific shelf") {
      it("should get removed") {
        createShelvingThing(storeId, shelvingGroupId, shelvingId)
        dittoActor ! RemoveItemsRow(storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId)
        serviceProbe
          .expectMessage[DittoCommand](1.minutes, RemoveItemsRow(storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId))
        removeShelvingThing(storeId, shelvingGroupId, shelvingId)
      }
    }
  }
}

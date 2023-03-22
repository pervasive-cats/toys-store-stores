/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.application.actors

import stores.store.valueobjects.{CatalogItem, ItemId, StoreId}

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
import stores.application.actors.commands.DittoCommand.RaiseAlarm
import stores.application.actors.commands.RootCommand.Startup
import stores.application.actors.commands.{DittoCommand, MessageBrokerCommand, RootCommand}
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
import org.eclipse.ditto.messages.model.MessageDirection
import org.eclipse.ditto.things.model.ThingId
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.{BeforeAndAfterAll, DoNotDiscover, Ignore, Tag}
import spray.json.{enrichAny, enrichString, JsBoolean, JsNumber, JsObject, JsString, JsValue}
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
  private val dittoActor: ActorRef[DittoCommand] = testKit.spawn(DittoActor(rootActorProbe.ref, messageBrokerActorProbe.ref, dittoConfig))

  @SuppressWarnings(Array("org.wartremover.warts.Var", "scalafix:DisableSyntax.var"))
  private var maybeClient: Option[DittoClient] = None

  private val store: Store = Store(StoreId(6).getOrElse(fail()))
  private val catalogItem: CatalogItem = CatalogItem(1).getOrElse(fail())
  private val itemId: ItemId = ItemId(1).getOrElse(fail())

  private def sendReply(
    message: RepliableMessage[String, String],
    correlationId: String,
    status: HttpStatus,
    payload: String
  ): Unit = message.reply().httpStatus(status).correlationId(correlationId).payload(payload).send()

  private def handleMessage(
    message: RepliableMessage[String, String],
    messageHandler: (RepliableMessage[String, String], Store, String, Seq[JsValue]) => Unit,
    payloadFields: String*
  ): Unit = {
    val thingIdMatcher: Regex = "antiTheftSystem-(?<store>[0-9]+)".r
    (message.getDirection, message.getEntityId.getName, message.getCorrelationId.toScala) match {
      case (MessageDirection.TO, thingIdMatcher(store), Some(correlationId)) if store.toLongOption.isDefined =>
        StoreId(store.toLong).fold(
          error => sendReply(message, correlationId, HttpStatus.BAD_REQUEST, ErrorResponseEntity(error).toJson.compactPrint),
          storeId =>
            messageHandler(
              message,
              Store(storeId),
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
            (msg, store, correlationId, _) => {
              serviceProbe ! RaiseAlarm(store.storeId)
              sendReply(
                msg,
                correlationId,
                HttpStatus.OK,
                ResultResponseEntity(()).toJson.compactPrint
              )
            }
          )
      )
    testKit.spawn(DittoActor(rootActorProbe.ref, testKit.createTestProbe[MessageBrokerCommand]().ref, dittoConfig))
    maybeClient = Some(client)
  }

  override def afterAll(): Unit = testKit.shutdownTestKit()

  private def thingId(store: Store): ThingId =
    ThingId.of(s"${dittoConfig.getString("namespace")}:antiTheftSystem-${store.storeId.value}")

  private def createAntiTheftThing(store: Store): Unit =
    maybeClient
      .getOrElse(fail())
      .twin
      .create(
        JsonObject
          .newBuilder
          .set("thingId", s"${dittoConfig.getString("namespace")}:antiTheftSystem-${store.storeId.value}")
          .set("definition", dittoConfig.getString("thingModel"))
          .set(
            "attributes",
            JsonObject
              .newBuilder
              .set("storeId", store.storeId.value: Long)
              .build
          )
          .build
      )
      .toCompletableFuture
      .get()

  private def removeAntiTheftThing(store: Store): Unit =
    maybeClient
      .getOrElse(fail())
      .twin
      .delete(thingId(store))
      .thenCompose(_ =>
        maybeClient
          .getOrElse(fail())
          .policies()
          .delete(PolicyId.of(dittoConfig.getString("namespace"), s"antiTheftSystem-${store.storeId.value}"))
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
        createAntiTheftThing(store)
        maybeClient
          .getOrElse(fail())
          .live
          .message[String]
          .from(thingId(store))
          .subject("itemDetected")
          .payload(JsObject("catalogItemId" -> catalogItem.toJson, "itemId" -> itemId.toJson).compactPrint)
          .send((_, t) => Option(t).fold(latch.countDown())(_ => fail()))
        latch.await(1, TimeUnit.MINUTES)
        serviceProbe.expectMessage[DittoCommand](1.minutes, RaiseAlarm(store.storeId))
        removeAntiTheftThing(store)
      }
    }

    describe("when asked to raise a shop's alarm") {
      it("should sound the alarm") {
        dittoActor ! RaiseAlarm(store.storeId)
        serviceProbe.expectMessage[DittoCommand](1.minutes, RaiseAlarm(store.storeId))
      }
    }
  }
}

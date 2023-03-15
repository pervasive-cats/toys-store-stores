/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.application.actors

import stores.store.Repository
import stores.store.entities.Store
import stores.store.valueobjects.StoreId

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

import spray.json.DefaultJsonProtocol.IntJsonFormat
import org.eclipse.ditto.json.JsonObject
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.{BeforeAndAfterAll, DoNotDiscover, Ignore, Tag}
import spray.json.{JsBoolean, JsNumber, JsObject, JsString, JsValue, enrichAny, enrichString}

import java.util.concurrent.{CountDownLatch, ForkJoinPool}
import java.util.regex.Pattern
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex
import scala.util.{Failure, Success} // scalafix:ok

@DoNotDiscover
class DittoActorTest extends AnyFunSpec with BeforeAndAfterAll with SprayJsonSupport {

  private val testKit: ActorTestKit = ActorTestKit()
  private val rootActorProbe: TestProbe[RootCommand] = testKit.createTestProbe[RootCommand]()
  private val messageBrokerActorProbe: TestProbe[MessageBrokerCommand] = testKit.createTestProbe[MessageBrokerCommand]()
  private val responseProbe: TestProbe[Validated[Unit]] = testKit.createTestProbe[Validated[Unit]]()
  private val serviceProbe: TestProbe[DittoCommand] = testKit.createTestProbe[DittoCommand]()
  private val config: Config = ConfigFactory.load()
  private val dittoConfig: Config = config.getConfig("ditto")

  private given ActorSystem = testKit.system.classicSystem
  private val client: HttpExt = Http()
  private val repository: Repository = Repository()

  private val dittoActor: ActorRef[DittoCommand] = testKit.spawn(
    DittoActor(rootActorProbe.ref, messageBrokerActorProbe.ref, dittoConfig)
  )

  private val store: Store = Store(StoreId(1).getOrElse(fail()))

  private def uri(hostName: String, port: String, namespace: String, store: Store): String =
    s"http://$hostName:$port/api/2/things/$namespace:antiTheftSytem-${store.storeId.value}"

  private case class DittoData(
    direction: String,
    messageSubject: String,
    store: Store,
    payload: Seq[(String, JsValue)]
  )

  private def parseDittoProtocol(namespace: String, message: String): Option[DittoData] = {
    val thingIdMatcher: Regex = (Pattern.quote(namespace) + ":antiTheftSystem-(?<store>[0-9]+)").r
    message.parseJson.asJsObject.getFields("headers", "value") match {
      case Seq(headers, value) =>
        headers
          .asJsObject
          .getFields("ditto-message-direction", "ditto-message-subject", "ditto-message-thing-id") match {
            case Seq(JsString(direction), JsString(messageSubject), JsString(thingIdMatcher(cartId, store)))
                 if cartId.toLongOption.isDefined && store.toLongOption.isDefined =>
              StoreId(store.toLong)
                .map(s => Some(DittoData(direction, messageSubject, Store(s), value.asJsObject.fields.toSeq.sortBy(_._1))))
                .getOrElse(None)
            case _ => None
          }
      case Seq(headers) =>
        headers
          .asJsObject
          .getFields("ditto-message-direction", "ditto-message-subject", "ditto-message-thing-id") match {
            case Seq(JsString(direction), JsString(messageSubject), JsString(thingIdMatcher(store)))
                 if store.toLongOption.isDefined =>
              StoreId(store.toLong).map(s => Some(DittoData(direction, messageSubject, Store(s), Seq.empty))).getOrElse(None)
            case _ => None
          }
      case _ => None
    }
  }

  override def beforeAll(): Unit = {
    val latch: CountDownLatch = CountDownLatch(1)
    val (websocket, response): (UntypedActorRef, Future[WebSocketUpgradeResponse]) =
      Source
        .actorRef[Message](
          { case m: TextMessage.Strict if m.text === "SUCCESS" => CompletionStrategy.draining },
          { case m: TextMessage.Strict if m.text === "ERROR" => IllegalStateException() },
          bufferSize = 1,
          OverflowStrategy.dropTail
        )
        .viaMat(
          client.webSocketClientFlow(
            WebSocketRequest(
              s"ws://${dittoConfig.getString("hostName")}:${dittoConfig.getString("portNumber")}/ws/2",
              extraHeaders =
                Seq(Authorization(BasicHttpCredentials(dittoConfig.getString("username"), dittoConfig.getString("password"))))
            )
          )
        )(Keep.both)
        .toMat(
          Flow[Message]
            .mapAsync(parallelism = 2) {
              case t: TextMessage => t.toStrict(60.seconds)
              case _ => Future.failed[TextMessage.Strict](IllegalArgumentException())
            }
            .mapConcat[DittoCommand](t =>
              if (t.text === "START-SEND-MESSAGES:ACK") {
                latch.countDown()
                List.empty
              } else {
                parseDittoProtocol(dittoConfig.getString("namespace"), t.text) match { // send action messages to devices
                  case Some(DittoData("TO", "raiseAlarm", store, Seq())) =>
                    RaiseAlarm(store.storeId) :: Nil
                  case _ => Nil
                }
              }
            )
            .to(Sink.foreach(serviceProbe ! _))
        )(Keep.left)
        .run()
    given ExecutionContext = ExecutionContext.fromExecutor(ForkJoinPool.commonPool())
    response
      .onComplete {
        case Failure(_) => fail()
        case Success(r) =>
          if (r.response.status === StatusCodes.SwitchingProtocols)
            websocket ! TextMessage("START-SEND-MESSAGES?namespaces=" + dittoConfig.getString("namespace"))
          else {
            println(r.response)
            fail()
          }
      }
    latch.await()
  }

  override def afterAll(): Unit = testKit.shutdownTestKit()

  private given ExecutionContext = ExecutionContext.fromExecutor(ForkJoinPool.commonPool())

  describe("A Ditto actor") {
    describe("when first started up") {
      ignore("should notify the root actor of its start") {
        rootActorProbe.expectMessage(60.seconds, Startup(true))
      }
    }
  }
}

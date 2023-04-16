/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.application.actors

import stores.application.actors.DittoActor
import stores.application.actors.commands.MessageBrokerCommand.{CatalogItemLifted, ItemReturned}
import stores.application.actors.commands.RootCommand.Startup
import stores.application.actors.DittoActor.DittoError
import stores.application.Serializers.given
import stores.application.actors.commands.*
import stores.application.actors.commands.DittoCommand.*
import stores.application.routes.entities.Entity.{ErrorResponseEntity, ResultResponseEntity}
import stores.store.domainevents.{CatalogItemLifted as CatalogItemLiftedEvent, ItemReturned as ItemReturnedEvent}
import stores.store.entities.Store
import stores.store.valueobjects.*
import stores.store.Repository.StoreNotFound

import akka.actor.{ActorSystem, ActorRef as UntypedActorRef}
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import akka.http.scaladsl.{Http, HttpExt}
import akka.http.scaladsl.client.RequestBuilding.{Get, Post}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.ws.*
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, get, path}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{CompletionStrategy, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import com.typesafe.config.{Config, ConfigFactory}
import io.getquill.JdbcContextConfig
import org.eclipse.ditto.base.model.common.HttpStatus
import org.eclipse.ditto.client.{DittoClient, DittoClients}
import org.eclipse.ditto.client.configuration.{BasicAuthenticationConfiguration, WebSocketMessagingConfiguration}
import org.eclipse.ditto.client.live.messages.RepliableMessage
import org.eclipse.ditto.client.messaging.{AuthenticationProviders, MessagingProviders}
import org.eclipse.ditto.client.options.Options
import org.eclipse.ditto.json.{JsonArray, JsonObject}
import org.eclipse.ditto.messages.model.MessageDirection
import org.eclipse.ditto.policies.model.PolicyId
import org.eclipse.ditto.things.model.ThingId
import org.scalatest.{BeforeAndAfterAll, DoNotDiscover}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*
import spray.json.{JsBoolean, JsNull, JsNumber, JsObject, JsString, JsValue, enrichAny}
import spray.json.DefaultJsonProtocol.IntJsonFormat

import java.util.concurrent.*
import java.util.function.BiConsumer
import java.util.regex.Pattern
import javax.sql.DataSource
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.{Failure, Success}
import scala.util.matching.Regex // scalafix:ok

@DoNotDiscover
class DittoActorTest extends AnyFunSpec with BeforeAndAfterAll with SprayJsonSupport {

  private val testKit: ActorTestKit = ActorTestKit()
  private val rootActorProbe: TestProbe[RootCommand] = testKit.createTestProbe[RootCommand]()
  private val messageBrokerActorProbe: TestProbe[MessageBrokerCommand] = testKit.createTestProbe[MessageBrokerCommand]()
  private val responseProbe: TestProbe[Validated[Unit]] = testKit.createTestProbe[Validated[Unit]]()
  private val serviceProbe: TestProbe[DittoCommand] = testKit.createTestProbe[DittoCommand]()
  private val config: Config = ConfigFactory.load()
  private val dataSource: DataSource = JdbcContextConfig(config.getConfig("repository")).dataSource
  private val dittoConfig: Config = config.getConfig("ditto")
  private val itemServerConfig: Config = config.getConfig("itemServer")

  private val storeId: StoreId = StoreId(1).getOrElse(fail())
  private val shelvingGroupId: ShelvingGroupId = ShelvingGroupId(2).getOrElse(fail())
  private val shelvingId: ShelvingId = ShelvingId(3).getOrElse(fail())
  private val shelfId: ShelfId = ShelfId(4).getOrElse(fail())
  private val itemsRowId: ItemsRowId = ItemsRowId(5).getOrElse(fail())
  private val catalogItem: CatalogItem = CatalogItem(6).getOrElse(fail())
  private val inCartItemId: ItemId = ItemId(7).getOrElse(fail())
  private val returnedItemId: ItemId = ItemId(9).getOrElse(fail())
  private val inPlaceItemId: ItemId = ItemId(10).getOrElse(fail())
  private val itemCategory: Long = 8
  private val amount: Double = 5.99
  private val currency: Currency = Currency.EUR
  private val name: String = "Puppy plush"
  private val description: String = "A plush of a cute dog."
  private given ActorSystem = testKit.system.classicSystem
  private given ExecutionContext = ExecutionContext.fromExecutor(ForkJoinPool.commonPool())

  private val dittoActor: ActorRef[DittoCommand] =
    testKit.spawn(DittoActor(rootActorProbe.ref, messageBrokerActorProbe.ref, dataSource, dittoConfig, itemServerConfig, Http()))

  private val itemServer =
    Http()
      .newServerAt(itemServerConfig.getString("hostName"), itemServerConfig.getInt("portNumber"))
      .bind(
        concat(
          path("item") {
            get {
              entity(as[JsValue]) {
                _.asJsObject.getFields("id", "kind", "store") match {
                  case Seq(JsNumber(id), JsNumber(kind), JsNumber(store))
                       if id.isValidLong && kind.longValue === catalogItem.id && store.longValue === storeId.value =>
                    complete(
                      JsObject(
                        "error" -> JsNull,
                        "result" -> JsObject(
                          "state" -> JsString(
                            if (id.longValue === inCartItemId.value)
                              "InCartItem"
                            else if (id.longValue === returnedItemId.value)
                              "ReturnedItem"
                            else
                              "InPlaceItem"
                          )
                        )
                      )
                    )
                  case _ => complete(StatusCodes.BadRequest, JsObject.empty)
                }
              }
            }
          },
          path("catalog_item") {
            get {
              entity(as[JsValue]) {
                _.asJsObject.getFields("id", "store") match {
                  case Seq(JsNumber(id), JsNumber(store))
                       if id.longValue === catalogItem.id && store.longValue === storeId.value =>
                    complete(
                      JsObject(
                        "error" -> JsNull,
                        "result" -> JsObject(
                          "category" -> JsNumber(itemCategory),
                          "price" -> JsObject(
                            "amount" -> JsNumber(amount),
                            "currency" -> JsString(currency.entryName)
                          )
                        )
                      )
                    )
                  case _ => complete(StatusCodes.BadRequest, JsObject.empty)
                }
              }
            }
          },
          path("item_category") {
            get {
              entity(as[JsValue]) {
                _.asJsObject.getFields("id") match {
                  case Seq(JsNumber(id)) if id.longValue === itemCategory =>
                    complete(
                      JsObject(
                        "error" -> JsNull,
                        "result" -> JsObject(
                          "name" -> JsString(name),
                          "description" -> JsString(description)
                        )
                      )
                    )
                  case _ => complete(StatusCodes.BadRequest, JsObject.empty)
                }
              }
            }
          }
        )
      )

  @SuppressWarnings(Array("org.wartremover.warts.Var", "scalafix:DisableSyntax.var"))
  private var maybeClient: Option[DittoClient] = None

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
      .startConsumption(Options.Consumption.namespaces(dittoConfig.getString("namespace")))
      .exceptionally { _ =>
        disconnectedDittoClient.destroy()
        fail()
      }
      .toCompletableFuture
      .get()
    DittoActorTestHelper.registerForMessages(client, serviceProbe, responseProbe)
    maybeClient = Some(client)
  }

  override def afterAll(): Unit = {
    testKit.shutdownTestKit()
    val countdownLatch: CountDownLatch = CountDownLatch(1)
    itemServer.foreach(_.unbind().foreach(_ => countdownLatch.countDown()))
    countdownLatch.await()
  }

  describe("A Ditto actor") {
    describe("when first started up") {
      it("should notify the root actor of its start") {
        rootActorProbe.expectMessage(1.minute, Startup(true))
      }
    }

    describe("when it receives a notification that an item is near an anti-theft system") {
      it("should sound the alarm if the item is in place") {
        DittoActorTestHelper.createAntiTheftSystemThing(storeId)(maybeClient.getOrElse(fail()), dittoConfig)
        maybeClient
          .getOrElse(fail())
          .live
          .message[String]
          .from(DittoActorTestHelper.antiTheftSystemThingId(storeId)(dittoConfig))
          .subject("itemDetected")
          .payload(JsObject("catalogItemId" -> catalogItem.toJson, "itemId" -> inPlaceItemId.toJson).compactPrint)
          .send()
        serviceProbe.expectMessage(1.minutes, RaiseAlarm(storeId))
        DittoActorTestHelper.removeAntiTheftSystemThing(storeId)(maybeClient.getOrElse(fail()), dittoConfig)
      }
    }

    describe("when it receives a notification that an item is near an anti-theft system") {
      it("should sound the alarm if the item is returned") {
        DittoActorTestHelper.createAntiTheftSystemThing(storeId)(maybeClient.getOrElse(fail()), dittoConfig)
        maybeClient
          .getOrElse(fail())
          .live
          .message[String]
          .from(DittoActorTestHelper.antiTheftSystemThingId(storeId)(dittoConfig))
          .subject("itemDetected")
          .payload(JsObject("catalogItemId" -> catalogItem.toJson, "itemId" -> returnedItemId.toJson).compactPrint)
          .send()
        serviceProbe.expectMessage(1.minutes, RaiseAlarm(storeId))
        DittoActorTestHelper.removeAntiTheftSystemThing(storeId)(maybeClient.getOrElse(fail()), dittoConfig)
      }
    }

    describe("when it receives a notification that an item is near an anti-theft system") {
      it("should do nothing if the item is in cart") {
        DittoActorTestHelper.createAntiTheftSystemThing(storeId)(maybeClient.getOrElse(fail()), dittoConfig)
        maybeClient
          .getOrElse(fail())
          .live
          .message[String]
          .from(DittoActorTestHelper.antiTheftSystemThingId(storeId)(dittoConfig))
          .subject("itemDetected")
          .payload(JsObject("catalogItemId" -> catalogItem.toJson, "itemId" -> inCartItemId.toJson).compactPrint)
          .send()
        serviceProbe.expectNoMessage(1.minutes)
        DittoActorTestHelper.removeAntiTheftSystemThing(storeId)(maybeClient.getOrElse(fail()), dittoConfig)
      }
    }

    describe("when notified of an item being inserted into the drop system") {
      it("should show the item data") {
        DittoActorTestHelper.createDropSystemThing(storeId)(maybeClient.getOrElse(fail()), dittoConfig)
        maybeClient
          .getOrElse(fail())
          .live
          .message[String]
          .from(DittoActorTestHelper.dropSystemThingId(storeId)(dittoConfig))
          .subject("itemInsertedIntoDropSystem")
          .payload(JsObject("catalogItem" -> catalogItem.toJson, "itemId" -> inPlaceItemId.toJson).compactPrint)
          .send()
        serviceProbe.expectMessage(1.minute, ShowItemData(storeId, name, description, amount, currency))
        DittoActorTestHelper.removeDropSystemThing(storeId)(maybeClient.getOrElse(fail()), dittoConfig)
      }
    }

    describe("when notified of an item being returned") {
      it("should forward the message to the message broker actor") {
        DittoActorTestHelper.createDropSystemThing(storeId)(maybeClient.getOrElse(fail()), dittoConfig)
        maybeClient
          .getOrElse(fail())
          .live
          .message[String]
          .from(DittoActorTestHelper.dropSystemThingId(storeId)(dittoConfig))
          .subject("itemReturned")
          .payload(JsObject("catalogItem" -> catalogItem.toJson, "itemId" -> inPlaceItemId.toJson).compactPrint)
          .send()
        messageBrokerActorProbe.expectMessage(
          1.minute,
          ItemReturned(ItemReturnedEvent(catalogItem, inPlaceItemId, storeId))
        )
        DittoActorTestHelper.removeDropSystemThing(storeId)(maybeClient.getOrElse(fail()), dittoConfig)
      }
    }

    describe("when it receives a notification that an item has been lifted from a specific row of items of a shelf") {
      it("should tell the message broker actor to forward the message") {
        dittoActor ! AddShelving(storeId, shelvingGroupId, shelvingId, responseProbe.ref)
        responseProbe.expectMessage(1.minute, Right[ValidationError, Unit](()))
        DittoActorTestHelper.checkShelvingPresence(storeId, shelvingGroupId, shelvingId)(
          maybeClient.getOrElse(fail()),
          dittoConfig
        )
        maybeClient
          .getOrElse(fail())
          .live
          .message[String]
          .from(DittoActorTestHelper.shelvingThingId(storeId, shelvingGroupId, shelvingId)(dittoConfig))
          .subject("catalogItemLiftingRegistered")
          .payload(JsObject("shelfId" -> shelfId.toJson, "itemsRowId" -> itemsRowId.toJson).compactPrint)
          .send()
        messageBrokerActorProbe.expectMessage(1.minute, CatalogItemLifted(CatalogItemLiftedEvent(catalogItem, storeId)))
        dittoActor ! RemoveShelving(storeId, shelvingGroupId, shelvingId, responseProbe.ref)
        responseProbe.expectMessage(1.minute, Right[ValidationError, Unit](()))
        DittoActorTestHelper.checkShelvingAbsence(storeId, shelvingGroupId, shelvingId)(
          maybeClient.getOrElse(fail()),
          dittoConfig
        )
      }
    }
  }

  describe("A shelving") {
    describe("after being added as a digital twin") {
      it("should be present in the Ditto service") {
        dittoActor ! AddShelving(storeId, shelvingGroupId, shelvingId, responseProbe.ref)
        responseProbe.expectMessage(1.minute, Right[ValidationError, Unit](()))
        DittoActorTestHelper.checkShelvingPresence(storeId, shelvingGroupId, shelvingId)(
          maybeClient.getOrElse(fail()),
          dittoConfig
        )
        dittoActor ! RemoveShelving(storeId, shelvingGroupId, shelvingId, responseProbe.ref)
        responseProbe.expectMessage(1.minute, Right[ValidationError, Unit](()))
        DittoActorTestHelper.checkShelvingAbsence(storeId, shelvingGroupId, shelvingId)(
          maybeClient.getOrElse(fail()),
          dittoConfig
        )
      }
    }

    describe("if never added as a digital twin") {
      it("should not be present in the Ditto service") {
        DittoActorTestHelper.checkShelvingAbsence(storeId, shelvingGroupId, shelvingId)(
          maybeClient.getOrElse(fail()),
          dittoConfig
        )
      }

      it("should not be removable") {
        dittoActor ! RemoveShelving(storeId, shelvingGroupId, shelvingId, responseProbe.ref)
        DittoActorTestHelper.checkShelvingAbsence(storeId, shelvingGroupId, shelvingId)(
          maybeClient.getOrElse(fail()),
          dittoConfig
        )
        responseProbe.expectMessage(1.minute, Left[ValidationError, Unit](StoreNotFound))
      }
    }

    describe("when adding a shelf to it") {
      it("should get added") {
        val newShelfId: ShelfId = ShelfId(11).getOrElse(fail())
        dittoActor ! AddShelving(storeId, shelvingGroupId, shelvingId, responseProbe.ref)
        responseProbe.expectMessage(1.minute, Right[ValidationError, Unit](()))
        DittoActorTestHelper.checkShelvingPresence(storeId, shelvingGroupId, shelvingId)(
          maybeClient.getOrElse(fail()),
          dittoConfig
        )
        dittoActor ! AddShelf(storeId, shelvingGroupId, shelvingId, newShelfId, responseProbe.ref)
        serviceProbe.expectMessage(1.minute, AddShelf(storeId, shelvingGroupId, shelvingId, newShelfId, responseProbe.ref))
        responseProbe.expectMessage(1.minute, Right[ValidationError, Unit](()))
        dittoActor ! RemoveShelving(storeId, shelvingGroupId, shelvingId, responseProbe.ref)
        responseProbe.expectMessage(1.minute, Right[ValidationError, Unit](()))
        DittoActorTestHelper.checkShelvingAbsence(storeId, shelvingGroupId, shelvingId)(
          maybeClient.getOrElse(fail()),
          dittoConfig
        )
      }
    }

    describe("when removing a shelf from it") {
      it("should get removed") {
        dittoActor ! AddShelving(storeId, shelvingGroupId, shelvingId, responseProbe.ref)
        responseProbe.expectMessage(1.minute, Right[ValidationError, Unit](()))
        DittoActorTestHelper.checkShelvingPresence(storeId, shelvingGroupId, shelvingId)(
          maybeClient.getOrElse(fail()),
          dittoConfig
        )
        dittoActor ! RemoveShelf(storeId, shelvingGroupId, shelvingId, shelfId, responseProbe.ref)
        serviceProbe.expectMessage(1.minute, RemoveShelf(storeId, shelvingGroupId, shelvingId, shelfId, responseProbe.ref))
        responseProbe.expectMessage(1.minute, Right[ValidationError, Unit](()))
        dittoActor ! RemoveShelving(storeId, shelvingGroupId, shelvingId, responseProbe.ref)
        responseProbe.expectMessage(1.minute, Right[ValidationError, Unit](()))
        DittoActorTestHelper.checkShelvingAbsence(storeId, shelvingGroupId, shelvingId)(
          maybeClient.getOrElse(fail()),
          dittoConfig
        )
      }
    }

    describe("when adding a row of items to it") {
      it("should get added") {
        val newItemsRowId: ItemsRowId = ItemsRowId(11).getOrElse(fail())
        dittoActor ! AddShelving(storeId, shelvingGroupId, shelvingId, responseProbe.ref)
        responseProbe.expectMessage(1.minute, Right[ValidationError, Unit](()))
        DittoActorTestHelper.checkShelvingPresence(storeId, shelvingGroupId, shelvingId)(
          maybeClient.getOrElse(fail()),
          dittoConfig
        )
        dittoActor ! AddItemsRow(storeId, shelvingGroupId, shelvingId, shelfId, newItemsRowId, responseProbe.ref)
        serviceProbe.expectMessage(
          1.minute,
          AddItemsRow(storeId, shelvingGroupId, shelvingId, shelfId, newItemsRowId, responseProbe.ref)
        )
        responseProbe.expectMessage(1.minute, Right[ValidationError, Unit](()))
        dittoActor ! RemoveShelving(storeId, shelvingGroupId, shelvingId, responseProbe.ref)
        responseProbe.expectMessage(1.minute, Right[ValidationError, Unit](()))
        DittoActorTestHelper.checkShelvingAbsence(storeId, shelvingGroupId, shelvingId)(
          maybeClient.getOrElse(fail()),
          dittoConfig
        )
      }
    }

    describe("when removing a row of items from it") {
      it("should get removed") {
        dittoActor ! AddShelving(storeId, shelvingGroupId, shelvingId, responseProbe.ref)
        responseProbe.expectMessage(1.minute, Right[ValidationError, Unit](()))
        DittoActorTestHelper.checkShelvingPresence(storeId, shelvingGroupId, shelvingId)(
          maybeClient.getOrElse(fail()),
          dittoConfig
        )
        dittoActor ! RemoveItemsRow(storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId, responseProbe.ref)
        serviceProbe.expectMessage(
          1.minute,
          RemoveItemsRow(storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId, responseProbe.ref)
        )
        responseProbe.expectMessage(1.minute, Right[ValidationError, Unit](()))
        dittoActor ! RemoveShelving(storeId, shelvingGroupId, shelvingId, responseProbe.ref)
        responseProbe.expectMessage(1.minute, Right[ValidationError, Unit](()))
        DittoActorTestHelper.checkShelvingAbsence(storeId, shelvingGroupId, shelvingId)(
          maybeClient.getOrElse(fail()),
          dittoConfig
        )
      }
    }
  }
}

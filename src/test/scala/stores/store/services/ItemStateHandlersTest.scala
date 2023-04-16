/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.services

import java.util.concurrent.*
import javax.sql.DataSource

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.FiniteDuration

import akka.actor.ActorSystem
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.ActorRef
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.*
import com.dimafeng.testcontainers.JdbcDatabaseContainer.CommonParams
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import com.typesafe.config.*
import io.getquill.JdbcContextConfig
import org.scalatest.BeforeAndAfterAll
import org.scalatest.EitherValues.*
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*
import org.testcontainers.utility.DockerImageName
import spray.json.DefaultJsonProtocol.*
import spray.json.JsNull
import spray.json.JsNumber
import spray.json.JsObject
import spray.json.JsString
import spray.json.JsValue

import stores.application.actors.commands.*
import stores.application.actors.DittoActor
import stores.application.actors.commands.DittoCommand.{RaiseAlarm, ShowItemData}
import stores.application.actors.commands.MessageBrokerCommand.{
  CatalogItemLifted as CatalogItemLiftedCommand,
  ItemReturned as ItemReturnedCommand
}
import stores.store.Repository
import stores.store.domainevents.*
import stores.store.services.ItemStateHandlers.EventRejected
import stores.store.valueobjects.*

class ItemStateHandlersTest extends AnyFunSpec with TestContainerForAll with BeforeAndAfterAll with SprayJsonSupport {

  private val timeout: FiniteDuration = 300.seconds

  override val containerDef: PostgreSQLContainer.Def = PostgreSQLContainer.Def(
    dockerImageName = DockerImageName.parse("postgres:15.1"),
    databaseName = "stores",
    username = "test",
    password = "test",
    commonJdbcParams = CommonParams(timeout, timeout, Some("stores.sql"))
  )

  private val testKit: ActorTestKit = ActorTestKit()
  private val messageBrokerActorProbe: TestProbe[MessageBrokerCommand] = testKit.createTestProbe[MessageBrokerCommand]()
  private val dittoActorProbe: TestProbe[DittoCommand] = testKit.createTestProbe[DittoCommand]()
  private val config: Config = ConfigFactory.load()
  private val itemServerConfig: Config = config.getConfig("itemServer")
  private given ActorSystem = testKit.system.classicSystem
  private given ExecutionContext = ExecutionContext.fromExecutor(ForkJoinPool.commonPool())

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

  @SuppressWarnings(Array("org.wartremover.warts.Var", "scalafix:DisableSyntax.var"))
  private var maybeRepository: Option[Repository] = None

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

  private val itemStateHandlers: ItemStateHandlers =
    ItemStateHandlers(messageBrokerActorProbe.ref, dittoActorProbe.ref, itemServerConfig, Http())

  override def afterAll(): Unit = {
    testKit.shutdownTestKit()
    val countdownLatch: CountDownLatch = CountDownLatch(1)
    itemServer.foreach(_.unbind().foreach(_ => countdownLatch.countDown()))
    countdownLatch.await()
  }

  override def afterContainersStart(containers: Containers): Unit = {
    val repository: Repository = Repository(
      JdbcContextConfig(
        ConfigFactory
          .load()
          .getConfig("repository")
          .withValue(
            "dataSource.portNumber",
            ConfigValueFactory.fromAnyRef(containers.container.getFirstMappedPort.intValue())
          )
      ).dataSource
    )
    maybeRepository = Some(repository)
  }

  describe("A onItemInserted handler") {
    describe("when invoked with an existing item") {
      it("should send a message to Ditto with the data on that item") {
        itemStateHandlers.onItemInserted(ItemInsertedInDropSystem(catalogItem, inPlaceItemId, storeId)).value shouldBe ()
        dittoActorProbe.expectMessage(1.minute, ShowItemData(storeId, name, description, amount, currency))
      }
    }

    describe("when invoked with a non existing item") {
      it("should fail") {
        itemStateHandlers
          .onItemInserted(ItemInsertedInDropSystem(CatalogItem(999).getOrElse(fail()), inPlaceItemId, storeId))
          .left
          .value shouldBe EventRejected
        dittoActorProbe.expectNoMessage(1.minute)
      }
    }
  }

  describe("A onItemReturned handler") {
    describe("when invoked with an item") {
      it("should send a message to the message broker containing the same item") {
        val event: ItemReturned = ItemReturned(catalogItem, inPlaceItemId, storeId)
        itemStateHandlers.onItemReturned(event)
        messageBrokerActorProbe.expectMessage(1.minute, ItemReturnedCommand(event))
      }
    }
  }

  describe("A onCatalogItemLiftingRegistered handler") {
    given Repository = maybeRepository.getOrElse(fail())

    describe("when invoked with the correct position in a shelving for a catalog item") {
      it("should send a message to the message broker containing the catalog item data") {
        itemStateHandlers
          .onCatalogItemLiftingRegistered(
            CatalogItemLiftingRegistered(storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId)
          )
          .value shouldBe ()
        messageBrokerActorProbe.expectMessage(1.minute, CatalogItemLiftedCommand(CatalogItemLifted(catalogItem, storeId)))
      }
    }

    describe("when invoked with a position in a shelving which is not associated with any catalog item") {
      it("should fail") {
        itemStateHandlers
          .onCatalogItemLiftingRegistered(
            CatalogItemLiftingRegistered(storeId, shelvingGroupId, shelvingId, shelfId, ItemsRowId(999).getOrElse(fail()))
          )
          .left
          .value shouldBe EventRejected
        messageBrokerActorProbe.expectNoMessage(1.minute)
      }
    }
  }

  describe("A onItemDetected handler") {
    describe("when invoked with an item which is in a cart") {
      it("should do nothing") {
        itemStateHandlers
          .onItemDetected(ItemDetected(inCartItemId, catalogItem, storeId))
          .value shouldBe ()
        messageBrokerActorProbe.expectNoMessage(1.minute)
      }
    }

    describe("when invoked with an item which is in place") {
      it("should send a message to ditto for raising the anti-theft system alarm") {
        itemStateHandlers
          .onItemDetected(ItemDetected(inPlaceItemId, catalogItem, storeId))
          .value shouldBe ()
        dittoActorProbe.expectMessage(1.minute, RaiseAlarm(storeId))
      }
    }

    describe("when invoked with an item which is returned") {
      it("should send a message to ditto for raising the anti-theft system alarm") {
        itemStateHandlers
          .onItemDetected(ItemDetected(returnedItemId, catalogItem, storeId))
          .value shouldBe ()
        dittoActorProbe.expectMessage(1.minute, RaiseAlarm(storeId))
      }
    }
  }
}

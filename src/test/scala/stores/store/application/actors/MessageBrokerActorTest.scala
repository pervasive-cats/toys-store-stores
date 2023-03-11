/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.application.actors

import stores.application.actors.MessageBrokerActor
import stores.application.actors.commands.{MessageBrokerCommand, RootCommand}
import stores.application.actors.commands.RootCommand.Startup
import stores.application.routes.entities.Response.*
import stores.application.Serializers.given
import stores.application.actors.commands.MessageBrokerCommand.{CatalogItemLiftingRegistered, ItemReturned}
import stores.application.routes.entities.Entity.{ErrorResponseEntity, ResultResponseEntity}
import stores.store.domainevents.{
  CatalogItemLiftingRegistered as CatalogItemLiftingRegisteredEvent,
  ItemReturned as ItemReturnedEvent
}
import stores.store.valueobjects.*
import stores.ValidationError

import akka.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import com.dimafeng.testcontainers.GenericContainer.DockerImage
import com.dimafeng.testcontainers.scalatest.{TestContainerForAll, TestContainersForAll}
import com.dimafeng.testcontainers.GenericContainer
import com.rabbitmq.client.*
import com.typesafe.config.*
import eu.timepit.refined.auto.given
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*
import org.testcontainers.containers.Container
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import org.testcontainers.utility.DockerImageName
import spray.json.{enrichAny, enrichString}

import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.*
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.jdk.CollectionConverters.MapHasAsJava

class MessageBrokerActorTest extends AnyFunSpec with TestContainerForAll with BeforeAndAfterAll {

  override val containerDef: GenericContainer.Def[GenericContainer] = GenericContainer.Def(
    dockerImage = DockerImage(Left[String, Future[String]]("rabbitmq:3.11.7")),
    exposedPorts = Seq(5672),
    env = Map(
      "RABBITMQ_DEFAULT_USER" -> "test",
      "RABBITMQ_DEFAULT_PASS" -> "test"
    ),
    waitStrategy = LogMessageWaitStrategy().withRegEx("^.*?Server startup complete.*?$")
  )

  private val testKit: ActorTestKit = ActorTestKit()
  private val rootActorProbe: TestProbe[RootCommand] = testKit.createTestProbe[RootCommand]()

  @SuppressWarnings(Array("org.wartremover.warts.Var", "scalafix:DisableSyntax.var"))
  private var messageBroker: Option[ActorRef[MessageBrokerCommand]] = None

  @SuppressWarnings(Array("org.wartremover.warts.Var", "scalafix:DisableSyntax.var"))
  private var maybeChannel: Option[Channel] = None

  private val itemsQueue: BlockingQueue[Map[String, String]] = LinkedBlockingDeque()
  private val shoppingQueue: BlockingQueue[Map[String, String]] = LinkedBlockingDeque()

  private val storeId: StoreId = StoreId(8140).getOrElse(fail())

  private val itemReturnedEvent: ItemReturnedEvent = ItemReturnedEvent(
    CatalogItem(9000).getOrElse(fail()),
    ItemId(9231).getOrElse(fail()),
    storeId
  )

  private val catalogItemLiftingEvent: CatalogItemLiftingRegisteredEvent = CatalogItemLiftingRegisteredEvent(
    storeId,
    ShelvingGroupId(0).getOrElse(fail()),
    ShelvingId(0).getOrElse(fail()),
    ShelfId(0).getOrElse(fail()),
    ItemsRowId(0).getOrElse(fail())
  )

  case object TestError extends ValidationError {

    override val message: String = "Test error"
  }

  private def forwardToQueue(queue: BlockingQueue[Map[String, String]]): DeliverCallback =
    (_: String, message: Delivery) =>
      queue.put(
        Map(
          "exchange" -> message.getEnvelope.getExchange,
          "routingKey" -> message.getEnvelope.getRoutingKey,
          "body" -> String(message.getBody, StandardCharsets.UTF_8),
          "contentType" -> message.getProperties.getContentType,
          "correlationId" -> message.getProperties.getCorrelationId,
          "replyTo" -> message.getProperties.getReplyTo
        )
      )

  override def afterContainersStart(containers: Containers): Unit = {
    val messageBrokerConfig: Config =
      ConfigFactory
        .load()
        .getConfig("messageBroker")
        .withValue(
          "portNumber",
          ConfigValueFactory.fromAnyRef(containers.container.getFirstMappedPort.intValue())
        )
    messageBroker = Some(testKit.spawn(MessageBrokerActor(rootActorProbe.ref, messageBrokerConfig)))
    val factory: ConnectionFactory = ConnectionFactory()
    factory.setUsername(messageBrokerConfig.getString("username"))
    factory.setPassword(messageBrokerConfig.getString("password"))
    factory.setVirtualHost(messageBrokerConfig.getString("virtualHost"))
    factory.setHost(messageBrokerConfig.getString("hostName"))
    factory.setPort(messageBrokerConfig.getInt("portNumber"))
    val connection: Connection = factory.newConnection()
    val channel: Channel = connection.createChannel()
    val couples: Seq[(String, String)] = Seq(
      "stores" -> "items",
      "stores" -> "shopping"
    )
    val queueArgs: Map[String, String] = Map("x-dead-letter-exchange" -> "dead_letters")
    couples.flatMap(Seq(_, _)).distinct.foreach(e => channel.exchangeDeclare(e, BuiltinExchangeType.TOPIC, true))
    couples
      .flatMap((b1, b2) => Seq(b1 + "_" + b2, b2 + "_" + b1))
      .foreach(q => channel.queueDeclare(q, true, false, false, queueArgs.asJava))
    couples
      .flatMap((b1, b2) => Seq((b1, b1 + "_" + b2, b2), (b2, b2 + "_" + b1, b1)))
      .foreach((e, q, r) => channel.queueBind(q, e, r))
    channel.basicConsume("stores_items", true, forwardToQueue(itemsQueue), (_: String) => {})
    channel.basicConsume("stores_shopping", true, forwardToQueue(shoppingQueue), (_: String) => {})
    maybeChannel = Some(channel)
  }

  override def afterAll(): Unit = testKit.shutdownTestKit()

  describe("A message broker actor") {
    describe("after being created") {
      it("should notify its root actor about it") {
        rootActorProbe.expectMessage(10.seconds, Startup(true))
      }
    }

    @SuppressWarnings(Array("org.wartremover.warts.Var", "scalafix:DisableSyntax.var"))
    var maybeShoppingCorrelationId: Option[String] = None

    @SuppressWarnings(Array("org.wartremover.warts.Var", "scalafix:DisableSyntax.var"))
    var maybeItemsCorrelationId: Option[String] = None

    describe("after being notified that an item has been returned") {
      it("should notify the message broker") {
        messageBroker.getOrElse(fail()) ! ItemReturned(itemReturnedEvent)
        val shoppingMessage: Map[String, String] = shoppingQueue.poll(10, TimeUnit.SECONDS)
        shoppingMessage("exchange") shouldBe "stores"
        shoppingMessage("routingKey") shouldBe "shopping"
        shoppingMessage("contentType") shouldBe "application/json"
        shoppingMessage(
          "correlationId"
        ) should fullyMatch regex "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"
        shoppingMessage("replyTo") shouldBe "stores"
        shoppingMessage("body").parseJson.convertTo[ResultResponseEntity[ItemReturnedEvent]].result shouldBe itemReturnedEvent
        maybeShoppingCorrelationId = Some(shoppingMessage("correlationId"))
        val itemsMessage: Map[String, String] = itemsQueue.poll(10, TimeUnit.SECONDS)
        itemsMessage("exchange") shouldBe "stores"
        itemsMessage("routingKey") shouldBe "items"
        itemsMessage("contentType") shouldBe "application/json"
        itemsMessage(
          "correlationId"
        ) should fullyMatch regex "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"
        itemsMessage("replyTo") shouldBe "stores"
        itemsMessage("body").parseJson.convertTo[ResultResponseEntity[ItemReturnedEvent]].result shouldBe itemReturnedEvent
        maybeItemsCorrelationId = Some(itemsMessage("correlationId"))
      }
    }

    describe("after receiving an error reply from the message broker for an item returned event") {
      it("should resend the message") {
        val channel: Channel = maybeChannel.getOrElse(fail())
        channel.basicPublish(
          "shopping",
          "stores",
          AMQP
            .BasicProperties
            .Builder()
            .contentType("application/json")
            .deliveryMode(2)
            .priority(0)
            .correlationId(maybeShoppingCorrelationId.getOrElse(fail()))
            .build(),
          ErrorResponseEntity(TestError).toJson.compactPrint.getBytes(StandardCharsets.UTF_8)
        )
        val shoppingMessage: Map[String, String] = shoppingQueue.poll(10, TimeUnit.SECONDS)
        shoppingMessage("exchange") shouldBe "stores"
        shoppingMessage("routingKey") shouldBe "shopping"
        shoppingMessage("contentType") shouldBe "application/json"
        shoppingMessage(
          "correlationId"
        ) should fullyMatch regex "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"
        shoppingMessage("replyTo") shouldBe "stores"
        shoppingMessage("body").parseJson.convertTo[ResultResponseEntity[ItemReturnedEvent]].result shouldBe itemReturnedEvent
        channel.basicPublish(
          "items",
          "stores",
          AMQP
            .BasicProperties
            .Builder()
            .contentType("application/json")
            .deliveryMode(2)
            .priority(0)
            .correlationId(maybeShoppingCorrelationId.getOrElse(fail()))
            .build(),
          ErrorResponseEntity(TestError).toJson.compactPrint.getBytes(StandardCharsets.UTF_8)
        )
        val itemsMessage: Map[String, String] = itemsQueue.poll(10, TimeUnit.SECONDS)
        itemsMessage("exchange") shouldBe "stores"
        itemsMessage("routingKey") shouldBe "items"
        itemsMessage("contentType") shouldBe "application/json"
        itemsMessage(
          "correlationId"
        ) should fullyMatch regex "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"
        itemsMessage("replyTo") shouldBe "stores"
        itemsMessage("body").parseJson.convertTo[ResultResponseEntity[ItemReturnedEvent]].result shouldBe itemReturnedEvent
      }
    }

    @SuppressWarnings(Array("org.wartremover.warts.Var", "scalafix:DisableSyntax.var"))
    var maybeCorrelationId: Option[String] = None

    describe("after being notified that a catalog item has been lifted") {
      it("should notify the message broker") {
        messageBroker.getOrElse(fail()) ! CatalogItemLiftingRegistered(catalogItemLiftingEvent)
        val itemsMessage: Map[String, String] = itemsQueue.poll(10, TimeUnit.SECONDS)
        itemsMessage("exchange") shouldBe "stores"
        itemsMessage("routingKey") shouldBe "items"
        itemsMessage("contentType") shouldBe "application/json"
        itemsMessage(
          "correlationId"
        ) should fullyMatch regex "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"
        itemsMessage("replyTo") shouldBe "stores"
        itemsMessage("body")
          .parseJson
          .convertTo[ResultResponseEntity[CatalogItemLiftingRegisteredEvent]]
          .result shouldBe catalogItemLiftingEvent
        maybeCorrelationId = Some(itemsMessage("correlationId"))
      }
    }

    describe("after receiving an error reply from the message broker") {
      it("should resend the message") {
        val channel: Channel = maybeChannel.getOrElse(fail())
        channel.basicPublish(
          "items",
          "stores",
          AMQP
            .BasicProperties
            .Builder()
            .contentType("application/json")
            .deliveryMode(2)
            .priority(0)
            .correlationId(maybeCorrelationId.getOrElse(fail()))
            .build(),
          ErrorResponseEntity(TestError).toJson.compactPrint.getBytes(StandardCharsets.UTF_8)
        )
        val itemsMessage: Map[String, String] = itemsQueue.poll(10, TimeUnit.SECONDS)
        itemsMessage("exchange") shouldBe "stores"
        itemsMessage("routingKey") shouldBe "items"
        itemsMessage("contentType") shouldBe "application/json"
        itemsMessage(
          "correlationId"
        ) should fullyMatch regex "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"
        itemsMessage("replyTo") shouldBe "stores"
        itemsMessage("body")
          .parseJson
          .convertTo[ResultResponseEntity[CatalogItemLiftingRegisteredEvent]]
          .result shouldBe catalogItemLiftingEvent
      }
    }
  }
}

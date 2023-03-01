/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.application.actors

import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.ForkJoinPool

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.jdk.CollectionConverters.MapHasAsJava
import scala.util.*

import akka.actor.typed.*
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import com.rabbitmq.client.*
import com.typesafe.config.Config
import spray.json.DefaultJsonProtocol.StringJsonFormat
import spray.json.JsObject
import spray.json.JsString
import spray.json.JsValue
import spray.json.JsonFormat
import spray.json.enrichAny
import spray.json.enrichString

import stores.Validated
import stores.application.RequestProcessingFailed
import stores.application.Serializers.given
import stores.application.actors.commands.MessageBrokerCommand.{CatalogItemLiftingRegistered, ItemReturned}
import stores.application.actors.commands.RootCommand.Startup
import stores.application.actors.commands.{MessageBrokerCommand, RootCommand}
import stores.application.routes.entities.Entity
import stores.application.routes.entities.Entity.{ErrorResponseEntity, ResultResponseEntity}
import stores.store.services.ItemStateHandlers

object MessageBrokerActor {

  private def publish[A <: Entity: JsonFormat](channel: Channel, response: A, replyTo: String, correlationId: String): Unit =
    channel.basicPublish(
      "stores",
      replyTo,
      AMQP
        .BasicProperties
        .Builder()
        .contentType("application/json")
        .deliveryMode(2)
        .priority(0)
        .correlationId(correlationId)
        .build(),
      response.toJson.compactPrint.getBytes(StandardCharsets.UTF_8)
    )

  private def publishValidated[A: JsonFormat](
    channel: Channel,
    value: Validated[A],
    replyTo: String,
    correlationId: String
  ): Unit =
    value.fold(
      t => publish(channel, ErrorResponseEntity(t), replyTo, correlationId),
      _ => publish(channel, ResultResponseEntity(()), replyTo, correlationId)
    )

    @SuppressWarnings(Array("org.wartremover.warts.ToString"))
    def apply(
      root: ActorRef[RootCommand],
      messageBrokerConfig: Config,
      repositoryConfig: Config
    ): Behavior[MessageBrokerCommand] =
      Behaviors.setup[MessageBrokerCommand] { ctx =>
        Try {
          val factory: ConnectionFactory = ConnectionFactory()
          factory.setUsername(messageBrokerConfig.getString("username"))
          factory.setPassword(messageBrokerConfig.getString("password"))
          factory.setVirtualHost(messageBrokerConfig.getString("virtualHost"))
          factory.setHost(messageBrokerConfig.getString("hostName"))
          factory.setPort(messageBrokerConfig.getInt("portNumber"))
          factory.newConnection()
        }.flatMap { c =>
          val channel: Channel = c.createChannel()
          channel.addReturnListener((r: Return) => {
            ctx.system.deadLetters[String] ! String(r.getBody, StandardCharsets.UTF_8)
            channel.basicPublish(
              "dead_letters",
              "dead_letters",
              AMQP
                .BasicProperties
                .Builder()
                .contentType("application/json")
                .deliveryMode(2)
                .priority(0)
                .build(),
              r.getBody
            )
          })
          Try {
            channel.exchangeDeclare("dead_letters", BuiltinExchangeType.FANOUT, true)
            channel.queueDeclare("dead_letters", true, false, false, Map.empty.asJava)
            channel.queueBind("dead_letters", "dead_letters", "")
            val couples: Seq[(String, String)] = Seq(
              "shopping" -> "items",
              "carts" -> "items",
              "stores" -> "items"
            )
            val queueArgs: Map[String, String] = Map("x-dead-letter-exchange" -> "dead_letters")
            couples.flatMap(Seq(_, _)).distinct.foreach(e => channel.exchangeDeclare(e, BuiltinExchangeType.TOPIC, true))
            couples
              .flatMap((b1, b2) => Seq(b1 + "_" + b2, b2 + "_" + b1))
              .foreach(q => channel.queueDeclare(q, true, false, false, queueArgs.asJava))
            couples
              .flatMap((b1, b2) => Seq((b1, b1 + "_" + b2, b2), (b2, b2 + "_" + b1, b1)))
              .foreach((e, q, r) => channel.queueBind(q, e, r))
            (c, channel)
          }
        }.map { (co, ch) =>
          root ! Startup(true)
          Behaviors
            .receiveMessage[MessageBrokerCommand] {
              case ItemReturned(event) =>
                publish(ch, ResultResponseEntity(event), "items", UUID.randomUUID().toString)
                publish(ch, ResultResponseEntity(event), "shopping", UUID.randomUUID().toString)
                Behaviors.same[MessageBrokerCommand]
              case CatalogItemLiftingRegistered(event) =>
                publish(ch, ResultResponseEntity(event), "items", UUID.randomUUID().toString)
                Behaviors.same[MessageBrokerCommand]
            }
            .receiveSignal {
              case (_, PostStop) =>
                ch.close()
                co.close()
                Behaviors.same[MessageBrokerCommand]
            }
        }.getOrElse {
          root ! Startup(false)
          Behaviors.stopped[MessageBrokerCommand]
        }
      }

}

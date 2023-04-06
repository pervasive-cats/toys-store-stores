/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.application.actors

import java.util.concurrent.ForkJoinPool

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import akka.actor.typed.*
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import com.typesafe.config.Config
import com.zaxxer.hikari.HikariDataSource
import io.getquill.JdbcContextConfig

import stores.application.actors.commands.{DittoCommand, MessageBrokerCommand, RootCommand}
import stores.application.actors.commands.RootCommand.Startup

object RootActor {

  def apply(config: Config): Behavior[RootCommand] =
    Behaviors.setup { ctx =>
      val messageBrokerActor: ActorRef[MessageBrokerCommand] = ctx.spawn(
        MessageBrokerActor(ctx.self, config.getConfig("messageBroker")),
        name = "message_broker_actor"
      )
      Behaviors.receiveMessage {
        case Startup(true) =>
          val dataSource: HikariDataSource = JdbcContextConfig(config.getConfig("repository")).dataSource
          ctx.spawn(
            DittoActor(ctx.self, messageBrokerActor, dataSource, config.getConfig("ditto")),
            name = "ditto_actor"
          )
          Behaviors.receiveMessage {
            case Startup(true) => Behaviors.empty[RootCommand]
            case _ => Behaviors.stopped[RootCommand]
          }
        case Startup(false) => Behaviors.stopped[RootCommand]
      }
    }
}

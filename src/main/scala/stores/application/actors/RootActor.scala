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

import stores.application.actors.commands.{DittoCommand, MessageBrokerCommand, RootCommand, StoreServerCommand}
import stores.application.actors.commands.RootCommand.Startup
import stores.application.routes.Routes

object RootActor {

  def apply(config: Config): Behavior[RootCommand] =
    Behaviors.setup { ctx =>
      val messageBrokerActor: ActorRef[MessageBrokerCommand] = ctx.spawn(
        MessageBrokerActor(ctx.self, config.getConfig("messageBroker")),
        name = "message_broker_actor"
      )
      Behaviors.receiveMessage {
        case Startup(true) =>
          val dittoActor: ActorRef[DittoCommand] = ctx.spawn(
            DittoActor(ctx.self, messageBrokerActor, config.getConfig("ditto")),
            name = "ditto_actor"
          )
          val serverConfig: Config = config.getConfig("server")
          Behaviors.receiveMessage {
            case Startup(true) =>
              Behaviors.same[RootCommand] // TODO awaitservers
            case Startup(false) => Behaviors.stopped[RootCommand]
          }
        case Startup(false) => Behaviors.stopped[RootCommand]
      }
    }

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def awaitServers(
    storeServer: ActorRef[StoreServerCommand],
    serverConfig: Config,
    count: Int
  ): Behavior[RootCommand] = Behaviors.receive { (ctx, msg) =>
    msg match {
      case Startup(true) if count < 0 =>
        awaitServers(storeServer, serverConfig, count + 1)
      case Startup(true) =>
        given ActorSystem[_] = ctx.system
        val httpServer: Future[Http.ServerBinding] =
          Http()
            .newServerAt(serverConfig.getString("hostName"), serverConfig.getInt("portNumber"))
            .bind(Routes(storeServer))
        Behaviors.receiveSignal {
          case (_, PostStop) =>
            given ExecutionContext = ExecutionContext.fromExecutor(ForkJoinPool.commonPool())
            httpServer.flatMap(_.unbind()).onComplete(_ => println("Server has stopped"))
            Behaviors.same[RootCommand]
        }
      case Startup(false) => Behaviors.stopped[RootCommand]
    }
  }
}

/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.application.actors

import java.util.concurrent.ForkJoinPool

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import com.typesafe.config.Config

import stores.application.actors.commands.RootCommand.Startup
import stores.application.actors.commands.{DittoCommand, RootCommand, StoreServerCommand}

object StoreServerActor {

  private given Timeout = 30.seconds

  case object OperationRejected extends ValidationError {

    override val message: String = "The requested operation could not be performed"
  }

  def apply(
    root: ActorRef[RootCommand],
    dittoActor: ActorRef[DittoCommand],
    repositoryConfig: Config
  ): Behavior[StoreServerCommand] =
    Behaviors.setup { ctx =>
      given ActorSystem[_] = ctx.system
      given ExecutionContext = ExecutionContext.fromExecutor(ForkJoinPool.commonPool())
      root ! Startup(success = true)
      Behaviors.receiveMessage {
        case _ => Behaviors.same[StoreServerCommand]
          // TODO StoreServerCommands
      }
    }
}

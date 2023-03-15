/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.application.routes

import scala.concurrent.duration.DurationInt
import scala.util.Failure
import scala.util.Success

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.*
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.util.Timeout
import spray.json.JsonWriter

import stores.application.routes.entities.Entity.ErrorResponseEntity
import stores.application.routes.entities.Response
import stores.application.actors.commands.StoreServerCommand

object Routes extends Directives with SprayJsonSupport {

  case object RequestFailed extends ValidationError {

    override val message: String = "An error has occurred while processing the request"
  }

  case class DeserializationFailed(message: String) extends ValidationError

  private val rejectionHandler: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handle {
        case MalformedRequestContentRejection(msg, _) =>
          complete(StatusCodes.BadRequest, ErrorResponseEntity(DeserializationFailed(msg)))
      }
      .result()

  private given timeout: Timeout = 30.seconds

  private def route[A: FromRequestUnmarshaller, B, C <: Response[D], D: JsonWriter](
    server: ActorRef[B],
    request: A => ActorRef[C] => B,
    responseHandler: C => Route
  )(
    using
    ActorSystem[_]
  ): Route =
    entity(as[A]) { e =>
      onComplete(server ? request(e)) {
        case Failure(_) => complete(StatusCodes.InternalServerError, ErrorResponseEntity(RequestFailed))
        case Success(value) => responseHandler(value)
      }
    }

  @SuppressWarnings(Array("org.wartremover.warts.TripleQuestionMark"))
  def apply(storeServer: ActorRef[StoreServerCommand])(using ActorSystem[_]): Route = ???
}

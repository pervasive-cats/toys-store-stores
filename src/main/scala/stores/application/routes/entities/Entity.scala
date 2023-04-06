/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.application.routes.entities

import io.github.pervasivecats.ValidationError

import spray.json.DefaultJsonProtocol
import spray.json.DefaultJsonProtocol.StringJsonFormat
import spray.json.JsArray
import spray.json.JsNull
import spray.json.JsObject
import spray.json.JsString
import spray.json.JsValue
import spray.json.JsonFormat
import spray.json.RootJsonFormat
import spray.json.deserializationError
import spray.json.enrichAny

import stores.application.Serializers.StringJsonFormat
import stores.application.RequestProcessingFailed
import stores.store.Repository.{RepositoryOperationFailed, StoreNotFound}
import stores.store.valueobjects.CatalogItem.WrongCatalogItemIdFormat
import stores.store.valueobjects.Count.WrongCountFormat
import stores.store.valueobjects.ItemId.WrongItemIdFormat
import stores.store.valueobjects.ItemsRowId.WrongItemsRowId
import stores.store.valueobjects.ShelfId.WrongShelfIdFormat
import stores.store.valueobjects.ShelvingGroupId.WrongShelvingGroupIdFormat
import stores.store.valueobjects.ShelvingId.WrongShelvingIdFormat
import stores.store.valueobjects.StoreId.WrongStoreIdFormat

trait Entity

object Entity {

  case class ResultResponseEntity[A](result: A) extends Entity

  given [A: JsonFormat]: RootJsonFormat[ResultResponseEntity[A]] with {

    override def read(json: JsValue): ResultResponseEntity[A] = json.asJsObject.getFields("result", "error") match {
      case Seq(result, JsNull) => ResultResponseEntity(result.convertTo[A])
      case _ => deserializationError(msg = "Json format was not valid")
    }

    override def write(response: ResultResponseEntity[A]): JsValue = JsObject(
      "result" -> response.result.toJson,
      "error" -> JsNull
    )
  }

  given [A: JsonFormat]: RootJsonFormat[Validated[A]] with {

    override def read(json: JsValue): Validated[A] = json.asJsObject.getFields("result", "error") match {
      case Seq(JsObject(_), JsNull) => Right[ValidationError, A](json.convertTo[ResultResponseEntity[A]].result)
      case Seq(JsNull, JsObject(_)) => Left[ValidationError, A](json.convertTo[ErrorResponseEntity].error)
    }

    override def write(validated: Validated[A]): JsValue = validated match {
      case Left(error) => ErrorResponseEntity(error).toJson
      case Right(value) => ResultResponseEntity(value).toJson
    }
  }

  case class ErrorResponseEntity(error: ValidationError) extends Entity

  given RootJsonFormat[ErrorResponseEntity] with {

    override def read(json: JsValue): ErrorResponseEntity = json.asJsObject.getFields("result", "error") match {
      case Seq(JsNull, error) =>
        error.asJsObject.getFields("type", "message") match {
          case Seq(JsString(tpe), JsString(message)) =>
            ErrorResponseEntity(tpe match {
              case "StoreNotFound" => StoreNotFound
              case "WrongCountFormat" => WrongCountFormat
              case "WrongCatalogItemIdFormat" => WrongCatalogItemIdFormat
              case "WrongItemsRowId" => WrongItemsRowId
              case "WrongItemIdFormat" => WrongItemIdFormat
              case "WrongShelfIdFormat" => WrongShelfIdFormat
              case "WrongShelvingIdFormat" => WrongShelvingIdFormat
              case "WrongShelvingGroupIdFormat" => WrongShelvingGroupIdFormat
              case "WrongStoreIdFormat" => WrongStoreIdFormat
              case "RepositoryOperationFailed" => RepositoryOperationFailed
              case "RequestProcessingFailed" => RequestProcessingFailed
              case _ => deserializationError(msg = "Json format was not valid")
            })
          case _ => deserializationError(msg = "Json format was not valid")
        }
      case _ => deserializationError(msg = "Json format was not valid")
    }

    override def write(response: ErrorResponseEntity): JsValue = JsObject(
      "result" -> JsNull,
      "error" -> JsObject(
        "type" -> response.error.getClass.getSimpleName.replace("$", "").toJson,
        "message" -> response.error.message.toJson
      )
    )
  }
}

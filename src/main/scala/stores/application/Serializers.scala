/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.application

import eu.timepit.refined.auto.given
import spray.json.DefaultJsonProtocol
import spray.json.JsBoolean
import spray.json.JsNull
import spray.json.JsNumber
import spray.json.JsObject
import spray.json.JsString
import spray.json.JsValue
import spray.json.JsonFormat
import spray.json.deserializationError
import spray.json.enrichAny

import stores.store.domainevents.{CatalogItemLifted, CatalogItemLiftingRegistered, ItemReturned}
import stores.store.valueobjects.*

object Serializers extends DefaultJsonProtocol {

  private def longSerializer[A](extractor: A => Long, builder: Long => Validated[A]): JsonFormat[A] = new JsonFormat[A] {

    override def read(json: JsValue): A = json match {
      case JsNumber(value) if value.isValidLong =>
        builder(value.longValue).fold(e => deserializationError(e.message), identity)
      case _ => deserializationError(msg = "Json format is not valid")
    }

    override def write(obj: A): JsValue = extractor(obj).toJson
  }

  given JsonFormat[CatalogItem] = longSerializer(_.id, CatalogItem.apply)

  given JsonFormat[Item] with {

    override def read(json: JsValue): Item =
      json.asJsObject.getFields("catalogItem", "id") match {
        case Seq(JsNumber(catalogItem), JsNumber(id)) if catalogItem.isValidLong && id.isValidLong =>
          (for {
            c <- CatalogItem(catalogItem.longValue)
            i <- ItemId(id.longValue)
          } yield Item(c, i)).fold(e => deserializationError(e.message), identity)
        case _ => deserializationError(msg = "Json format is not valid")
      }

    override def write(item: Item): JsValue = JsObject(
      "catalogItem" -> item.catalogItem.toJson,
      "id" -> item.id.toJson
    )
  }

  given JsonFormat[ItemId] = longSerializer(_.value, ItemId.apply)

  given JsonFormat[ItemsRowId] = longSerializer(_.value, ItemsRowId.apply)

  given JsonFormat[ShelfId] = longSerializer(_.value, ShelfId.apply)

  given JsonFormat[ShelvingGroupId] = longSerializer(_.value, ShelvingGroupId.apply)

  given JsonFormat[ShelvingId] = longSerializer(_.value, ShelvingId.apply)

  given JsonFormat[StoreId] = longSerializer(_.value, StoreId.apply)

  given JsonFormat[Currency] with {

    override def read(json: JsValue): Currency = json match {
      case JsString(value) => Currency.withNameEither(value).fold(e => deserializationError(e.getMessage), identity)
      case _ => deserializationError(msg = "Json format is not valid")
    }

    override def write(currency: Currency): JsValue = JsString(currency.entryName)
  }

  given JsonFormat[CatalogItemLifted] with {

    override def read(json: JsValue): CatalogItemLifted = json.asJsObject.getFields("id", "store") match {
      case Seq(JsNumber(catalogItem), JsNumber(storeId)) if catalogItem.isValidLong && storeId.isValidLong =>
        (for {
          c <- CatalogItem(catalogItem.longValue)
          s <- StoreId(storeId.longValue)
        } yield CatalogItemLifted(c, s)).fold(e => deserializationError(e.message), identity)
      case _ => deserializationError(msg = "Json format is not valid")
    }

    override def write(obj: CatalogItemLifted): JsValue = JsObject(
      "type" -> "CatalogItemLifted".toJson,
      "id" -> obj.catalogItem.toJson,
      "store" -> obj.storeId.toJson
    )
  }

  given JsonFormat[ItemReturned] with {

    override def read(json: JsValue): ItemReturned = json.asJsObject.getFields("kind", "id", "store") match {
      case Seq(JsNumber(catalogItem), JsNumber(itemId), JsNumber(storeId))
           if catalogItem.isValidLong && itemId.isValidLong && storeId.isValidLong =>
        (for {
          c <- CatalogItem(catalogItem.longValue)
          i <- ItemId(itemId.longValue)
          s <- StoreId(storeId.longValue)
        } yield ItemReturned(c, i, s)).fold(e => deserializationError(e.message), identity)
      case _ => deserializationError(msg = "Json format is not valid")
    }

    override def write(obj: ItemReturned): JsValue = JsObject(
      "type" -> "ItemReturned".toJson,
      "kind" -> obj.catalogItem.toJson,
      "id" -> obj.itemId.toJson,
      "store" -> obj.storeId.toJson
    )
  }
}

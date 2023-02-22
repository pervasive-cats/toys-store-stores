/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.app

import stores.Validated

import eu.timepit.refined.auto.given
import io.github.pervasivecats.stores.store.valueobjects.{CatalogItem, Item, ItemId, ItemsRowId, ShelfId, ShelvingGroupId, ShelvingId, StoreId}
import spray.json.{DefaultJsonProtocol, JsBoolean, JsNull, JsNumber, JsObject, JsString, JsValue, JsonFormat, deserializationError, enrichAny}

object Serializers extends DefaultJsonProtocol {

  private def stringSerializer[A](extractor: A => String, builder: String => Validated[A]): JsonFormat[A] = new JsonFormat[A] {

    override def read(json: JsValue): A = json match {
      case JsString(value) => builder(value).fold(e => deserializationError(e.message), identity)
      case _ => deserializationError(msg = "Json format is not valid")
    }

    override def write(obj: A): JsValue = extractor(obj).toJson
  }

  private def longSerializer[A](extractor: A => Long, builder: Long => Validated[A]): JsonFormat[A] = new JsonFormat[A] {

    override def read(json: JsValue): A = json match {
      case JsNumber(value) if value.isValidLong =>
        builder(value.longValue).fold(e => deserializationError(e.message), identity)
      case _ => deserializationError(msg = "Json format is not valid")
    }

    override def write(obj: A): JsValue = extractor(obj).toJson
  }

  given JsonFormat[CatalogItem] = longSerializer(_.id, CatalogItem.apply)

  /*given JsonFormat[Item] with {

    override def read(json: JsValue): Item =
      json.asJsObject.getFields("catalogItem", "id") match {
        case Seq(JsNumber(catalogItem), JsNumber(id)) if catalogItem.isValidLong && id.isValidLong =>
          for {
            c <- CatalogItem(catalogItem.longValue)
            i <- ItemId(id.longValue)
          } yield Item(c, i)
        case _ => deserializationError(msg = "Json format is not valid")
      }

    override def write(item: Item): JsValue = JsObject(
      "catalogItem" -> item.catalogItem.toJson,
      "id" -> item.id.toJson
    )
  }*/

  given JsonFormat[ItemId] = longSerializer(_.value, ItemId.apply)

  given JsonFormat[ItemsRowId] = longSerializer(_.value, ItemsRowId.apply)

  given JsonFormat[ShelfId] = longSerializer(_.value, ShelfId.apply)

  given JsonFormat[ShelvingGroupId] = longSerializer(_.value, ShelvingGroupId.apply)

  given JsonFormat[ShelvingId] = longSerializer(_.value, ShelvingId.apply)

  given JsonFormat[StoreId] = longSerializer(_.value, StoreId.apply)



}

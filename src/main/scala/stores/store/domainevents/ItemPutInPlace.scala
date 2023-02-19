/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.domainevents

import stores.store.valueobjects.{CatalogItem, ItemId, StoreId}

trait ItemPutInPlace {

  val storeId: StoreId

  val catalogItem: CatalogItem

  val itemId: ItemId
}

object ItemPutInPlace {

  private case class ItemPutInPlaceImpl(storeId: StoreId, catalogItem: CatalogItem, itemId: ItemId) extends ItemPutInPlace

  def apply(storeId: StoreId, catalogItem: CatalogItem, itemId: ItemId): ItemPutInPlace =
    ItemPutInPlaceImpl(storeId, catalogItem, itemId)
}

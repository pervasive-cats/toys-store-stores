/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.domainevents

import stores.store.valueobjects.{CatalogItem, Item, ItemId, StoreId}

trait ItemInsertedInDropSystem {

  val catalogItem: CatalogItem

  val itemId: ItemId

  val storeId: StoreId
}

object ItemInsertedInDropSystem {

  private case class ItemInsertedInDropSystemImpl(catalogItem: CatalogItem, itemId: ItemId, storeId: StoreId)
    extends ItemInsertedInDropSystem

  def apply(catalogItem: CatalogItem, itemId: ItemId, storeId: StoreId): ItemInsertedInDropSystem =
    ItemInsertedInDropSystemImpl(catalogItem, itemId, storeId)
}

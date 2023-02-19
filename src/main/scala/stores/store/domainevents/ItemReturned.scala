/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.domainevents

import stores.store.valueobjects.{CatalogItem, ItemId, StoreId}

trait ItemReturned {

  val catalogItem: CatalogItem

  val itemId: ItemId

  val storeId: StoreId
}

object ItemReturned {

  private case class ItemReturnedImpl(catalogItem: CatalogItem, itemId: ItemId, storeId: StoreId) extends ItemReturned

  def apply(catalogItem: CatalogItem, itemId: ItemId, storeId: StoreId): ItemReturned = ItemReturnedImpl(catalogItem, itemId, storeId)
}
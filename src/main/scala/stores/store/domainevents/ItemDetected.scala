/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.domainevents

import stores.store.valueobjects.{CatalogItem, ItemId, StoreId}

trait ItemDetected {

  val itemId: ItemId

  val catalogItem: CatalogItem

  val storeId: StoreId
}

object ItemDetected {

  private case class ItemDetectedImpl(itemId: ItemId, catalogItem: CatalogItem, storeId: StoreId) extends ItemDetected

  def apply(itemId: ItemId, catalogItem: CatalogItem, storeId: StoreId): ItemDetected =
    ItemDetectedImpl(itemId, catalogItem, storeId)
}

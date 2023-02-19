/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.domainevents

import stores.store.valueobjects.{CatalogItem, Item, StoreId}

trait ItemDetected {

  val item: Item

  val catalogItem: CatalogItem

  val storeId: StoreId
}

object ItemDetected {

  private case class ItemDetectedImpl(item: Item, catalogItem: CatalogItem, storeId: StoreId) extends ItemDetected

  def apply(item: Item, catalogItem: CatalogItem, storeId: StoreId): ItemDetected = ItemDetectedImpl(item, catalogItem, storeId)
}
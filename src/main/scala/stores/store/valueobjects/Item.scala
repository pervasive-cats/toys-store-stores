/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

trait Item {

  val catalogItem: CatalogItem

  val id: ItemId
}

object Item {

  final case class ItemImpl(catalogItem: CatalogItem, id: ItemId) extends Item

  def apply(catalogItem: CatalogItem, id: ItemId): Item = ItemImpl(catalogItem, id)
}

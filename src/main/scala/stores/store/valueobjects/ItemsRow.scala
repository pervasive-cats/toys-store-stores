/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

import eu.timepit.refined.api.RefType.applyRef
import eu.timepit.refined.auto.autoUnwrap

import stores.{Validated, ValidationError}

trait ItemsRow {

  val itemsRowId: ItemsRowId

  val catalogItem: CatalogItem

  val count: Count
}

object ItemsRow {

  final private case class ItemsRowImpl(itemsRowId: ItemsRowId, catalogItem: CatalogItem, count: Count) extends ItemsRow

  given ItemsRowOps[ItemsRow] with {

    override def updated(itemsRow: ItemsRow, catalogItem: CatalogItem, count: Count): ItemsRow =
      ItemsRowImpl(itemsRow.itemsRowId, catalogItem, count)
  }

  def apply(itemsRowId: ItemsRowId, catalogItem: CatalogItem, count: Count): ItemsRow =
    ItemsRowImpl(itemsRowId, catalogItem, count)
}

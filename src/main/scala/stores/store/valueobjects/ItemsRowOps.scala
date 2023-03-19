/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

trait ItemsRowOps[A <: ItemsRow] {

  def updated(itemsRow: ItemsRow, catalogItem: CatalogItem, count: Count): A
}

object ItemsRowOps {

  extension [A <: ItemsRow: ItemsRowOps](itemsRow: A) {

    def updated(catalogItem: CatalogItem, count: Count) = implicitly[ItemsRowOps[A]].updated(itemsRow, catalogItem, count)
  }
}

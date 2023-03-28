/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

trait ShelfOps[A <: Shelf] {

  def addItemsRow(shelf: A, itemsRow: ItemsRow): A

  def removeItemsRow(shelf: A, itemsRowId: ItemsRowId): A

  def updateItemsRow(shelf: A, itemsRow: ItemsRow): A
}

object ShelfOps {

  extension [A <: Shelf: ShelfOps](shelf: A) {

    def addItemsRow(itemsRow: ItemsRow): A = implicitly[ShelfOps[A]].addItemsRow(shelf, itemsRow)

    def removeItemsRow(itemsRowId: ItemsRowId): A = implicitly[ShelfOps[A]].removeItemsRow(shelf, itemsRowId)

    def updateItemsRow(itemsRow: ItemsRow): A = implicitly[ShelfOps[A]].updateItemsRow(shelf, itemsRow)
  }
}

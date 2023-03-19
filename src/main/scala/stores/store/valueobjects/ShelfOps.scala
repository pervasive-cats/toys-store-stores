/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

trait ShelfOps[A <: Shelf] {

  def addItemsRow(shelf: Shelf, itemsRow: ItemsRow): Shelf

  def removeItemsRow(shelf: Shelf, itemsRowId: ItemsRowId): Shelf

  def updateItemsRow(shelf: Shelf, itemsRow: ItemsRow): Shelf
}

object ShelfOps {
  
  extension [A <: Shelf: ShelfOps](shelf: A) {
    
    def addItemsRow(itemsRow: ItemsRow) = implicitly[ShelfOps[A]].addItemsRow(shelf, itemsRow)

    def removeItemsRow(itemsRowId: ItemsRowId) = implicitly[ShelfOps[A]].removeItemsRow(shelf, itemsRowId)

    def updateItemsRow(itemsRow: ItemsRow) = implicitly[ShelfOps[A]].updateItemsRow(shelf, itemsRow)
  }
}
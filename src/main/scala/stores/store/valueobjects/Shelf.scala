/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

import AnyOps.*
import stores.store.valueobjects.ShelfOps.*

trait Shelf {

  val shelfId: ShelfId

  val itemsRows: List[ItemsRow]
}

object Shelf {
  
  final private case class ShelfImpl(shelfId: ShelfId, itemsRows: List[ItemsRow]) extends Shelf

  given ShelfOps[Shelf] with {

    override def addItemsRow(shelf: Shelf, itemsRow: ItemsRow): Shelf =
      ShelfImpl(shelf.shelfId, shelf.itemsRows ++ List[ItemsRow](itemsRow))

    override def removeItemsRow(shelf: Shelf, itemsRowId: ItemsRowId): Shelf =
      ShelfImpl(shelf.shelfId, shelf.itemsRows.filter(_.itemsRowId !== itemsRowId))

    override def updateItemsRow(shelf: Shelf, itemsRow: ItemsRow): Shelf =
      shelf
        .removeItemsRow(itemsRow.itemsRowId)
        .addItemsRow(itemsRow)
  }

  def apply(shelfId: ShelfId, itemsRows: List[ItemsRow]): Shelf = ShelfImpl(shelfId, itemsRows)
}

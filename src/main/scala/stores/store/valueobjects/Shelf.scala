/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

trait Shelf {

  val shelfId: ShelfId

  val itemsRows: List[ItemsRow]
}

object Shelf {
  
  final private case class ShelfImpl(shelfId: ShelfId, itemsRows: List[ItemsRow]) extends Shelf
  
  def apply(shelfId: ShelfId, itemsRows: List[ItemsRow]): Shelf = ShelfImpl(shelfId, itemsRows)
}

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

  def addItemsRow(itemsRow: ItemsRow): Shelf

  def removeItemsRow(itemsRowId: ItemsRowId): Shelf

  def updateItemsRow(itemsRow: ItemsRow): Shelf
}

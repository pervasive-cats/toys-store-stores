/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

trait Shelving {

  val shelvingId: ShelvingId
  val shelves: List[Shelf]

  def addShelf(shelf: Shelf): Shelving

  def removeShelf(shelfId: ShelfId): Shelving

  def updateShelf(shelf: Shelf): Shelving
}

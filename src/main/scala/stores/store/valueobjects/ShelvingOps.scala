/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

trait ShelvingOps[A <: Shelving] {

  def addShelf(shelving: Shelving, shelf: Shelf): Shelving

  def removeShelf(shelving: Shelving, shelfId: ShelfId): Shelving

  def updateShelf(shelving: Shelving, shelf: Shelf): Shelving
}

object ShelvingOps {

  extension [A <: Shelving: ShelvingOps](shelving: A) {

    def addShelf(shelf: Shelf) = implicitly[ShelvingOps[A]].addShelf(shelving, shelf)

    def removeShelf(shelfId: ShelfId) = implicitly[ShelvingOps[A]].removeShelf(shelving, shelfId)

    def updateShelf(shelf: Shelf) = implicitly[ShelvingOps[A]].updateShelf(shelving, shelf)
  }
}
/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

trait ShelvingOps[A <: Shelving] {

  def addShelf(shelving: A, shelf: Shelf): A

  def removeShelf(shelving: A, shelfId: ShelfId): A

  def updateShelf(shelving: A, shelf: Shelf): A
}

object ShelvingOps {

  extension [A <: Shelving: ShelvingOps](shelving: A) {

    def addShelf(shelf: Shelf): A = implicitly[ShelvingOps[A]].addShelf(shelving, shelf)

    def removeShelf(shelfId: ShelfId): A = implicitly[ShelvingOps[A]].removeShelf(shelving, shelfId)

    def updateShelf(shelf: Shelf): A = implicitly[ShelvingOps[A]].updateShelf(shelving, shelf)
  }
}

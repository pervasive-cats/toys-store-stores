/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

import AnyOps.*
import stores.store.valueobjects.ShelvingOps.*

trait Shelving {

  val shelvingId: ShelvingId

  val shelves: List[Shelf]
}

object Shelving {

  private case class ShelvingImpl(shelvingId: ShelvingId, shelves: List[Shelf]) extends Shelving
  
  given ShelvingOps[Shelving] with {

    override def addShelf(shelving: Shelving, shelf: Shelf): Shelving =
      ShelvingImpl(shelving.shelvingId, shelving.shelves ++ List[Shelf](shelf))

    override def removeShelf(shelving: Shelving, shelfId: ShelfId): Shelving =
      ShelvingImpl(shelving.shelvingId, shelving.shelves.filter(_.shelfId !== shelfId))

    override def updateShelf(shelving: Shelving, shelf: Shelf): Shelving =
      shelving
        .removeShelf(shelf.shelfId)
        .addShelf(shelf)
  }

  def apply(shelvingId: ShelvingId, shelves: List[Shelf]): Shelving = ShelvingImpl(shelvingId, shelves)
}
/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

trait ShelvingOps[A <: Shelving] {

  def addShelf(shelf: Shelf): A

  def removeShelf(shelfId: ShelfId): A

  def updateShelf(shelf: Shelf): A
}

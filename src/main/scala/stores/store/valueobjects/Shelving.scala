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
}

object Shelving {

  private case class ShelvingImpl(shelvingId: ShelvingId, shelves: List[Shelf]) extends Shelving

  def apply(shelvingId: ShelvingId, shelves: List[Shelf]): Shelving = ShelvingImpl(shelvingId, shelves)
}
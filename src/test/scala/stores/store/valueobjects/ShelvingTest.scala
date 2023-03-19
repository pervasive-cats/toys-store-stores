/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.shouldBe
import stores.store.valueobjects.ShelvingOps.*

class ShelvingTest extends AnyFunSpec {

  val shelvingId: ShelvingId = ShelvingId(1).getOrElse(fail())
  val itemsRowId: ItemsRowId = ItemsRowId(2).getOrElse(fail())
  val catalogItem: CatalogItem = CatalogItem(3).getOrElse(fail())
  val count: Count = Count(4).getOrElse(fail())
  val itemsRow: ItemsRow = ItemsRow(itemsRowId, catalogItem, count)
  val shelfId: ShelfId = ShelfId(5).getOrElse(fail())
  val shelf: Shelf = Shelf(shelfId, List[ItemsRow](itemsRow))

  describe("A Shelving") {
    describe("when created with a shelving id and a list of shelf") {
      it("should contain them") {
        val shelving: Shelving = Shelving(shelvingId, List[Shelf](shelf))
        shelving.shelvingId shouldBe shelvingId
        shelving.shelves shouldBe List[Shelf](shelf)
      }
    }

    describe("when add a shelf") {
      it("should be added") {
        val shelving: Shelving = Shelving(shelvingId, List[Shelf](shelf))

        val shelfIdB: ShelfId = ShelfId(15).getOrElse(fail())
        val shelfB: Shelf = Shelf(shelfIdB, List[ItemsRow](itemsRow))
        val shelvingB: Shelving = shelving.addShelf(shelfB)

        shelvingB.shelvingId shouldBe shelvingId
        shelvingB.shelves shouldBe List[Shelf](shelf, shelfB)
      }
    }

    describe("when remove a shelf") {
      it("should be removed") {
        val shelving: Shelving = Shelving(shelvingId, List[Shelf](shelf))

        val shelfIdB: ShelfId = ShelfId(15).getOrElse(fail())
        val shelfB: Shelf = Shelf(shelfIdB, List[ItemsRow](itemsRow))
        val shelvingB: Shelving = shelving.addShelf(shelfB)

        val shelvingC: Shelving = shelvingB.removeShelf(shelf.shelfId)

        shelvingC.shelvingId shouldBe shelvingId
        shelvingC.shelves shouldBe List[Shelf](shelfB)
      }
    }

    describe("when update a shelf") {
      it("should be updated") {
        val shelving: Shelving = Shelving(shelvingId, List[Shelf](shelf))

        val itemsRowIdB: ItemsRowId = ItemsRowId(12).getOrElse(fail())
        val itemsRowB: ItemsRow = ItemsRow(itemsRowIdB, catalogItem, count)
        val shelfB: Shelf = Shelf(shelfId, List[ItemsRow](itemsRowB))
        val shelvingB: Shelving = shelving.updateShelf(shelfB)

        shelvingB.shelvingId shouldBe shelvingId
        shelvingB.shelves shouldBe List[Shelf](shelfB)
      }
    }
  }
}

package io.github.pervasivecats
package stores.store.valueobjects

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.shouldBe

import stores.store.valueobjects.ShelfOps.*

class ShelfTest extends AnyFunSpec {

  val shelfId: ShelfId = ShelfId(1).getOrElse(fail())
  val itemsRowId: ItemsRowId = ItemsRowId(2).getOrElse(fail())
  val catalogItem: CatalogItem = CatalogItem(3).getOrElse(fail())
  val count: Count = Count(4).getOrElse(fail())
  val itemsRow: ItemsRow = ItemsRow(itemsRowId, catalogItem, count)

  describe("A Shelf") {
    describe("when created with a shelf id and a list of items rows") {
      it("should contain them") {
        val shelf: Shelf = Shelf(shelfId, List[ItemsRow](itemsRow))
        shelf.shelfId shouldBe shelfId
        shelf.itemsRows shouldBe List[ItemsRow](itemsRow)
      }
    }

    describe("when add a new items row") {
      it("should be added") {
        val shelf: Shelf = Shelf(shelfId, List[ItemsRow](itemsRow))

        val itemsRowIdB: ItemsRowId = ItemsRowId(12).getOrElse(fail())
        val itemsRowB: ItemsRow = ItemsRow(itemsRowIdB, catalogItem, count)

        val shelfB = shelf.addItemsRow(itemsRowB)

        shelfB.shelfId shouldBe shelfId
        shelfB.itemsRows shouldBe List[ItemsRow](itemsRow) ++ List[ItemsRow](itemsRowB)
      }
    }

    describe("when remove an items row") {
      it("should be removed") {
        val shelf: Shelf = Shelf(shelfId, List[ItemsRow](itemsRow))

        val itemsRowIdB: ItemsRowId = ItemsRowId(12).getOrElse(fail())
        val itemsRowB: ItemsRow = ItemsRow(itemsRowIdB, catalogItem, count)

        val shelfB = shelf.addItemsRow(itemsRowB)

        val shelfC = shelfB.removeItemsRow(itemsRow.itemsRowId)

        shelfC.shelfId shouldBe shelfId
        shelfC.itemsRows shouldBe List[ItemsRow](itemsRowB)
      }
    }

    describe("when update an items row") {
      it("should be updated") {
        val shelf: Shelf = Shelf(shelfId, List[ItemsRow](itemsRow))

        val catalogItemB: CatalogItem = CatalogItem(13).getOrElse(fail())
        val countB: Count = Count(14).getOrElse(fail())
        val itemsRowB: ItemsRow = ItemsRow(itemsRowId, catalogItemB, countB)

        val shelfB = shelf.updateItemsRow(itemsRowB)

        shelfB.shelfId shouldBe shelfId
        shelfB.itemsRows shouldBe List[ItemsRow](itemsRowB)
      }
    }
  }

}

package io.github.pervasivecats
package stores.store.valueobjects

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.shouldBe

class ShelfTest extends AnyFunSpec {

  val shelfId: ShelfId = ShelfId(14).getOrElse(fail())
  val itemsRowId: ItemsRowId = ItemsRowId(1000).getOrElse(fail())
  val catalogItem: CatalogItem = CatalogItem(781).getOrElse(fail())
  val count: Count = Count(112).getOrElse(fail())
  val itemsRow: ItemsRow = ItemsRow(itemsRowId, catalogItem, count)

  describe("A Shelf"){
    describe("when created with a shelf id and a list of items rows"){
      it("should contain them"){
        val shelf: Shelf = Shelf(shelfId, List[ItemsRow](itemsRow))
        shelf.shelfId shouldBe shelfId
        shelf.itemsRows shouldBe List[ItemsRow](itemsRow)
      }
    }
  }
}

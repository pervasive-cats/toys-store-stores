package io.github.pervasivecats
package stores.store.valueobjects

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.shouldBe

class ShelvingTest extends AnyFunSpec {

  val shelvingId: ShelvingId = ShelvingId(110).getOrElse(fail())
  val itemsRowId: ItemsRowId = ItemsRowId(923).getOrElse(fail())
  val catalogItem: CatalogItem = CatalogItem(1023).getOrElse(fail())
  val count: Count = Count(231).getOrElse(fail())
  val itemsRow: ItemsRow = ItemsRow(itemsRowId, catalogItem, count)
  val shelfId: ShelfId = ShelfId(900).getOrElse(fail())
  val shelf: Shelf = Shelf(shelfId, List[ItemsRow](itemsRow))

  describe("A Shelving"){
    describe("when created with a shelving id and a list of shelf"){
      it("should contain them"){
        val shelving: Shelving = Shelving(shelvingId, List[Shelf](shelf))
        shelving.shelvingId shouldBe shelvingId
        shelving.shelves shouldBe List[Shelf](shelf)
      }
    }
  }
}

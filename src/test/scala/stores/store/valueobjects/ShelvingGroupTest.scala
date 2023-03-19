package io.github.pervasivecats
package stores.store.valueobjects

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.shouldBe

class ShelvingGroupTest extends AnyFunSpec{

  val shelvingGroupId: ShelvingGroupId = ShelvingGroupId(1000).getOrElse(fail())
  val shelvingId: ShelvingId = ShelvingId(110).getOrElse(fail())
  val itemsRowId: ItemsRowId = ItemsRowId(923).getOrElse(fail())
  val catalogItem: CatalogItem = CatalogItem(1023).getOrElse(fail())
  val count: Count = Count(231).getOrElse(fail())
  val itemsRow: ItemsRow = ItemsRow(itemsRowId, catalogItem, count)
  val shelfId: ShelfId = ShelfId(900).getOrElse(fail())
  val shelf: Shelf = Shelf(shelfId, List[ItemsRow](itemsRow))
  val shelving: Shelving = Shelving(shelvingId, List[Shelf](shelf))

  describe("A Shelving Group"){
    describe("when created with a shelving group id and a list of shelvings"){
      it("should contain them"){
        val shelvingGroup: ShelvingGroup = ShelvingGroup(shelvingGroupId, List[Shelving](shelving))
        shelvingGroup.shelvingGroupId shouldBe shelvingGroupId
        shelvingGroup.shelvings shouldBe List[Shelving](shelving)
      }
    }
  }
}

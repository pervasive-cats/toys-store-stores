package io.github.pervasivecats
package stores.store.valueobjects

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.shouldBe

import stores.store.valueobjects.ShelvingGroupOps.*

class ShelvingGroupTest extends AnyFunSpec {

  val shelvingGroupId: ShelvingGroupId = ShelvingGroupId(1000).getOrElse(fail())
  val shelvingId: ShelvingId = ShelvingId(1).getOrElse(fail())
  val itemsRowId: ItemsRowId = ItemsRowId(2).getOrElse(fail())
  val catalogItem: CatalogItem = CatalogItem(3).getOrElse(fail())
  val count: Count = Count(4).getOrElse(fail())
  val itemsRow: ItemsRow = ItemsRow(itemsRowId, catalogItem, count)
  val shelfId: ShelfId = ShelfId(5).getOrElse(fail())
  val shelf: Shelf = Shelf(shelfId, List[ItemsRow](itemsRow))
  val shelving: Shelving = Shelving(shelvingId, List[Shelf](shelf))

  describe("A Shelving Group") {
    describe("when created with a shelving group id and a list of shelvings") {
      it("should contain them") {
        val shelvingGroup: ShelvingGroup = ShelvingGroup(shelvingGroupId, List[Shelving](shelving))
        shelvingGroup.shelvingGroupId shouldBe shelvingGroupId
        shelvingGroup.shelvings shouldBe List[Shelving](shelving)
      }
    }

    describe("when add a shelving") {
      it("should be added") {
        val shelvingGroup: ShelvingGroup = ShelvingGroup(shelvingGroupId, List[Shelving](shelving))

        val shelvingIdB: ShelvingId = ShelvingId(11).getOrElse(fail())
        val shelvingB: Shelving = Shelving(shelvingIdB, List[Shelf](shelf))
        val shelvingGroupB: ShelvingGroup = shelvingGroup.addShelving(shelvingB)

        shelvingGroupB.shelvingGroupId shouldBe shelvingGroupId
        shelvingGroupB.shelvings shouldBe List[Shelving](shelving, shelvingB)
      }
    }

    describe("when remove a shelving") {
      it("should be removed") {
        val shelvingGroup: ShelvingGroup = ShelvingGroup(shelvingGroupId, List[Shelving](shelving))

        val shelvingIdB: ShelvingId = ShelvingId(11).getOrElse(fail())
        val shelvingB: Shelving = Shelving(shelvingIdB, List[Shelf](shelf))
        val shelvingGroupB: ShelvingGroup = shelvingGroup.addShelving(shelvingB)

        val shelvingGroupC: ShelvingGroup = shelvingGroupB.removeShelving(shelvingId)

        shelvingGroupC.shelvingGroupId shouldBe shelvingGroupId
        shelvingGroupC.shelvings shouldBe List[Shelving](shelvingB)
      }
    }

    describe("when update a shelving") {
      it("should be updated") {
        val shelvingGroup: ShelvingGroup = ShelvingGroup(shelvingGroupId, List[Shelving](shelving))

        val shelfIdB: ShelfId = ShelfId(55).getOrElse(fail())
        val shelfB: Shelf = Shelf(shelfIdB, List[ItemsRow](itemsRow))
        val shelvingB: Shelving = Shelving(shelvingId, List[Shelf](shelfB))
        val shelvingGroupB = shelvingGroup.updateShelving(shelvingB)

        shelvingGroupB.shelvingGroupId shouldBe shelvingGroupId
        shelvingGroupB.shelvings shouldBe List[Shelving](shelvingB)
      }
    }
  }
}

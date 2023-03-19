package io.github.pervasivecats
package stores.store.entities

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.shouldBe

import stores.store.valueobjects.{
  CatalogItem,
  Count,
  ItemsRow,
  ItemsRowId,
  Shelf,
  ShelfId,
  Shelving,
  ShelvingGroup,
  ShelvingGroupId,
  ShelvingId,
  StoreId
}
import stores.store.entities.StoreOps.*

class StoreTest extends AnyFunSpec {

  val storeId: StoreId = StoreId(1).getOrElse(fail())
  val shelvingGroupId: ShelvingGroupId = ShelvingGroupId(2).getOrElse(fail())
  val shelvingId: ShelvingId = ShelvingId(3).getOrElse(fail())
  val itemsRowId: ItemsRowId = ItemsRowId(4).getOrElse(fail())
  val catalogItem: CatalogItem = CatalogItem(5).getOrElse(fail())
  val count: Count = Count(6).getOrElse(fail())
  val itemsRow: ItemsRow = ItemsRow(itemsRowId, catalogItem, count)
  val shelfId: ShelfId = ShelfId(7).getOrElse(fail())
  val shelf: Shelf = Shelf(shelfId, List[ItemsRow](itemsRow))
  val shelving: Shelving = Shelving(shelvingId, List[Shelf](shelf))
  val shelvingGroup: ShelvingGroup = ShelvingGroup(shelvingGroupId, List[Shelving](shelving))

  describe("A store") {
    describe("when created with a store id and a layout") {
      it("should contain them") {
        val store: Store = Store(storeId, List[ShelvingGroup](shelvingGroup))
        store.storeId shouldBe storeId
        store.layout shouldBe List[ShelvingGroup](shelvingGroup)
      }
    }

    describe("when add a new shelving group") {
      it("should be added") {
        val store: Store = Store(storeId, List[ShelvingGroup](shelvingGroup))
        val shelvingGroupIdB: ShelvingGroupId = ShelvingGroupId(12).getOrElse(fail())
        val shelvingGroupB: ShelvingGroup = ShelvingGroup(shelvingGroupIdB, List[Shelving](shelving))
        val newStore = store.addShelvingGroup(shelvingGroupB)
        newStore.layout shouldBe List[ShelvingGroup](shelvingGroup, shelvingGroupB)
      }
    }

    describe("when remove a shelving group") {
      it("should be removed") {
        val store: Store = Store(storeId, List[ShelvingGroup](shelvingGroup))
        val shelvingGroupIdB: ShelvingGroupId = ShelvingGroupId(12).getOrElse(fail())
        val shelvingGroupB: ShelvingGroup = ShelvingGroup(shelvingGroupIdB, List[Shelving](shelving))
        val storeB = store.addShelvingGroup(shelvingGroupB)
        val storeC = storeB.removeShelvingGroup(shelvingGroupId)
        storeC.layout shouldBe List[ShelvingGroup](shelvingGroupB)
      }
    }

    describe("when remove a non existent shelving group") {
      it("should do nothing") {
        val store: Store = Store(storeId, List[ShelvingGroup](shelvingGroup))
        val shelvingGroupIdB: ShelvingGroupId = ShelvingGroupId(12).getOrElse(fail())
        val shelvingGroupB: ShelvingGroup = ShelvingGroup(shelvingGroupIdB, List[Shelving](shelving))
        val storeC = store.removeShelvingGroup(shelvingGroupIdB)
        storeC.layout shouldBe List[ShelvingGroup](shelvingGroup)
      }
    }

    describe("when update a shelving group") {
      it("should be updated") {
        val store: Store = Store(storeId, List[ShelvingGroup](shelvingGroup))

        val shelvingIdB: ShelvingId = ShelvingId(13).getOrElse(fail())
        val shelvingB: Shelving = Shelving(shelvingIdB, List[Shelf](shelf))
        val shelvingGroupB: ShelvingGroup = ShelvingGroup(shelvingGroupId, List[Shelving](shelvingB))

        val storeC = store.updateShelvingGroup(shelvingGroupB)

        storeC.layout shouldBe List[ShelvingGroup](shelvingGroupB)
      }
    }

    /*describe("when update a non existing shelving group"){
      it("should do nothing"){
        val store: Store = Store(storeId, List[ShelvingGroup](shelvingGroup))

        val shelvingGroupIdB: ShelvingGroupId = ShelvingGroupId(12).getOrElse(fail())
        val shelvingIdB: ShelvingId = ShelvingId(13).getOrElse(fail())
        val shelvingB: Shelving = Shelving(shelvingIdB, List[Shelf](shelf))
        val shelvingGroupB: ShelvingGroup = ShelvingGroup(shelvingGroupIdB, List[Shelving](shelvingB))

        val storeC = store.updateShelvingGroup(shelvingGroupB)

        storeC.layout shouldBe List[ShelvingGroup](shelvingGroup)
      }
    }*/

  }
}

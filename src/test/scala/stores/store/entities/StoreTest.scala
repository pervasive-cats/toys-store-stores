/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.entities

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*

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

  private val storeId: StoreId = StoreId(1).getOrElse(fail())
  private val shelvingGroupId: ShelvingGroupId = ShelvingGroupId(2).getOrElse(fail())
  private val shelvingId: ShelvingId = ShelvingId(3).getOrElse(fail())
  private val itemsRowId: ItemsRowId = ItemsRowId(4).getOrElse(fail())
  private val catalogItem: CatalogItem = CatalogItem(5).getOrElse(fail())
  private val count: Count = Count(6).getOrElse(fail())
  private val itemsRow: ItemsRow = ItemsRow(itemsRowId, catalogItem, count)
  private val shelfId: ShelfId = ShelfId(7).getOrElse(fail())
  private val shelf: Shelf = Shelf(shelfId, Seq[ItemsRow](itemsRow))
  private val shelving: Shelving = Shelving(shelvingId, Seq[Shelf](shelf))
  private val shelvingGroup: ShelvingGroup = ShelvingGroup(shelvingGroupId, Seq[Shelving](shelving))
  private val store: Store = Store(storeId, Seq[ShelvingGroup](shelvingGroup))

  describe("A store") {
    describe("when created with a store id and a layout") {
      it("should contain them") {
        store.storeId shouldBe storeId
        store.layout shouldBe Seq[ShelvingGroup](shelvingGroup)
      }
    }

    describe("when add a new shelving group") {
      it("should be added") {
        val shelvingGroupIdB: ShelvingGroupId = ShelvingGroupId(12).getOrElse(fail())
        val shelvingGroupB: ShelvingGroup = ShelvingGroup(shelvingGroupIdB, Seq[Shelving](shelving))
        val newStore = store.addShelvingGroup(shelvingGroupB)
        newStore.layout shouldBe Seq[ShelvingGroup](shelvingGroup, shelvingGroupB)
      }
    }

    describe("when remove a shelving group") {
      it("should be removed") {
        val shelvingGroupIdB: ShelvingGroupId = ShelvingGroupId(12).getOrElse(fail())
        val shelvingGroupB: ShelvingGroup = ShelvingGroup(shelvingGroupIdB, Seq[Shelving](shelving))
        val storeB = store.addShelvingGroup(shelvingGroupB)
        val storeC = storeB.removeShelvingGroup(shelvingGroupId)
        storeC.layout shouldBe Seq[ShelvingGroup](shelvingGroupB)
      }
    }

    describe("when a shelving group is updated") {
      it("should be updated") {
        val shelvingIdB: ShelvingId = ShelvingId(13).getOrElse(fail())
        val shelvingB: Shelving = Shelving(shelvingIdB, Seq[Shelf](shelf))
        val shelvingGroupB: ShelvingGroup = ShelvingGroup(shelvingGroupId, Seq[Shelving](shelvingB))
        val storeC = store.updateShelvingGroup(shelvingGroupB)
        storeC.layout shouldBe Seq[ShelvingGroup](shelvingGroupB)
      }
    }

    val shelvingGroupIdB: ShelvingGroupId = ShelvingGroupId(12).getOrElse(fail())
    val shelvingGroupB: ShelvingGroup = ShelvingGroup(shelvingGroupIdB, Seq[Shelving](shelving))
    val storeB = store.addShelvingGroup(shelvingGroupB)
    val storeC = storeB.removeShelvingGroup(shelvingGroupId)

    describe("when compared with another identical store") {
      it("should be equal following the symmetrical property") {
        store shouldEqual storeB
        storeB shouldEqual store
      }

      it("should be equal following the transitive property") {
        store shouldEqual storeB
        storeB shouldEqual storeC
        store shouldEqual storeC
      }

      it("should be equal following the reflexive property") {
        store shouldEqual store
      }

      it("should have the same hash code as the other") {
        store.## shouldEqual storeB.##
      }
    }

    describe("when compared with anything else") {
      it("should not be equal") {
        store should not equal 1.0
      }
    }
  }
}

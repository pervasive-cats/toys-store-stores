package io.github.pervasivecats
package stores.store.valueobjects

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.shouldBe

import stores.store.valueobjects.ItemsRowOps.*

class ItemsRowTest extends AnyFunSpec {

  val itemsRowId: ItemsRowId = ItemsRowId(1000).getOrElse(fail())
  val catalogItem: CatalogItem = CatalogItem(1).getOrElse(fail())
  val count: Count = Count(2).getOrElse(fail())

  describe("An Items row") {
    describe("when created with an items row id, a catalog item and a count") {
      it("should contain them") {
        val itemsRow: ItemsRow = ItemsRow(itemsRowId, catalogItem, count)
        itemsRow.itemsRowId shouldBe itemsRowId
        itemsRow.catalogItem shouldBe catalogItem
        itemsRow.count shouldBe count
      }
    }

    describe("when update with a new catalog item and a count") {
      it("should contain them") {
        val itemsRow: ItemsRow = ItemsRow(itemsRowId, catalogItem, count)

        val catalogItemB: CatalogItem = CatalogItem(11).getOrElse(fail())
        val countB: Count = Count(12).getOrElse(fail())

        val itemsRowB: ItemsRow = itemsRow.updated(catalogItemB, countB)
        itemsRowB.itemsRowId shouldBe itemsRowId
        itemsRowB.catalogItem shouldBe catalogItemB
        itemsRowB.count shouldBe countB
      }
    }
  }

}

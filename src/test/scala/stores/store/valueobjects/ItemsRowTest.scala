package io.github.pervasivecats
package stores.store.valueobjects

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.shouldBe

class ItemsRowTest extends AnyFunSpec {

  val itemsRowId: ItemsRowId = ItemsRowId(1000).getOrElse(fail())
  val catalogItem: CatalogItem = CatalogItem(781).getOrElse(fail())
  val count: Count = Count(112).getOrElse(fail())

  describe("An Items row"){
    describe("when created with an items row id, a catalog item and a count"){
      it("should contain them"){
        val itemsRow: ItemsRow = ItemsRow(itemsRowId, catalogItem, count)
        itemsRow.itemsRowId shouldBe itemsRowId
        itemsRow.catalogItem shouldBe catalogItem
        itemsRow.count shouldBe count
      }
    }
  }
}

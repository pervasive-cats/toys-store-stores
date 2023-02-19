package io.github.pervasivecats
package stores.store.valueobjects

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*

import scala.language.postfixOps

class ItemsRowTest extends AnyFunSpec {

  describe("An items row"){
    describe("when created with an id, a catalog item and a count"){
      it("should contain them"){
        val itemsRowId: ItemsRowId = ItemsRowId(9000).getOrElse(fail())
        val catalogItem: CatalogItem = CatalogItem(8541).getOrElse(fail())
        val count: Count = Count(150).getOrElse(fail())
        val itemsRow: ItemsRow = ItemsRow(itemsRowId, catalogItem, count)
        itemsRow.itemsRowId shouldBe itemsRowId
        itemsRow.catalogItem shouldBe catalogItem
        itemsRow.count shouldBe count
      }
    }

    describe("when updated with a new count"){
      val itemsRowId: ItemsRowId = ItemsRowId(9000).getOrElse(fail())
      val catalogItem: CatalogItem = CatalogItem(8541).getOrElse(fail())
      val count: Count = Count(150).getOrElse(fail())
      val itemsRow: ItemsRow = ItemsRow(itemsRowId, catalogItem, count)
      itemsRow.itemsRowId shouldBe itemsRowId
      itemsRow.catalogItem shouldBe catalogItem
      itemsRow.count shouldBe count

      val updatedCount: Count = Count(110).getOrElse(fail())
      val updatedItemsRow: ItemsRow = ItemsRow(itemsRowId, catalogItem, updatedCount)
      updatedItemsRow.itemsRowId shouldBe itemsRowId
      updatedItemsRow.catalogItem shouldBe catalogItem
      updatedItemsRow.count shouldBe updatedCount
    }
  }
}

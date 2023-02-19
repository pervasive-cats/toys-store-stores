package io.github.pervasivecats
package stores.store.domainevents

import stores.store.valueobjects.{CatalogItem, Item, ItemId, StoreId}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.shouldBe

class ItemDetectedTest extends AnyFunSpec {

  describe("A catalog item lifted") {
    describe("when created with a catalog item and a store id") {
      it("should be contain them") {
        val catalogItem: CatalogItem = CatalogItem(9000).getOrElse(fail())
        val itemId: ItemId = ItemId(9231).getOrElse(fail())
        val item: Item = Item(catalogItem, itemId)
        val storeId: StoreId = StoreId(8140).getOrElse(fail())
        val itemDetected: ItemDetected = ItemDetected(item, catalogItem, storeId)
        itemDetected.item shouldBe item
        itemDetected.catalogItem shouldBe catalogItem
        itemDetected.storeId shouldBe storeId
      }
    }
  }
}

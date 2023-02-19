package io.github.pervasivecats
package stores.store.domainevents

import stores.store.valueobjects.{CatalogItem, ItemId, StoreId}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*

class ItemReturnedTest extends AnyFunSpec {

  describe("An item returned") {
    describe("when created with a catalog item, an item id and a store id") {
      it("should be contain them") {
        val catalogItem: CatalogItem = CatalogItem(9000).getOrElse(fail())
        val itemId: ItemId = ItemId(9231).getOrElse(fail())
        val storeId: StoreId = StoreId(8140).getOrElse(fail())
        val itemReturned: ItemReturned = ItemReturned(catalogItem, itemId, storeId)
        itemReturned.catalogItem shouldBe catalogItem
        itemReturned.itemId shouldBe itemId
        itemReturned.storeId shouldBe storeId
      }
    }
  }
}

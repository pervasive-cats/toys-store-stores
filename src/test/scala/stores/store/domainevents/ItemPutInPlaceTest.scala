package io.github.pervasivecats
package stores.store.domainevents

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*

import stores.store.valueobjects.{CatalogItem, ItemId, StoreId}

class ItemPutInPlaceTest extends AnyFunSpec {

  describe("An item put in place") {
    describe("when created with a store id, a catalog item and an item id") {
      it("should be contain them") {
        val storeId: StoreId = StoreId(8140).getOrElse(fail())
        val catalogItem: CatalogItem = CatalogItem(9000).getOrElse(fail())
        val itemId: ItemId = ItemId(9231).getOrElse(fail())
        val itemPutInPlace: ItemPutInPlace = ItemPutInPlace(storeId, catalogItem, itemId)
        itemPutInPlace.storeId shouldBe storeId
        itemPutInPlace.catalogItem shouldBe catalogItem
        itemPutInPlace.itemId shouldBe itemId
      }
    }
  }
}

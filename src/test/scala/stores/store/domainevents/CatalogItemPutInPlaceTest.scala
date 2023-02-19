package io.github.pervasivecats
package stores.store.domainevents

import io.github.pervasivecats.stores.store.valueobjects.{CatalogItem, StoreId}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*

class CatalogItemPutInPlaceTest extends AnyFunSpec{

  describe("A catalog item put in place") {
    describe("when created with a catalog item and a store id") {
      it("should be contain them") {
        val catalogItem: CatalogItem = CatalogItem(9000).getOrElse(fail())
        val storeId: StoreId = StoreId(8140).getOrElse(fail())
        val catalogItemPutInPlace: CatalogItemPutInPlace = CatalogItemPutInPlace(catalogItem, storeId)
        catalogItemPutInPlace.catalogItem shouldBe catalogItem
        catalogItemPutInPlace.storeId shouldBe storeId
      }
    }
  }
}

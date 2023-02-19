package io.github.pervasivecats
package stores.store.domainevents

import stores.store.valueobjects.{CatalogItem, StoreId}

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*

class CatalogItemLiftedTest extends AnyFunSpec{

  describe("A catalog item lifted"){
    describe("when created with a catalog item and a store id") {
      it("should be contain them"){
        val catalogItem: CatalogItem = CatalogItem(9000).getOrElse(fail())
        val storeId: StoreId = StoreId(8140).getOrElse(fail())
        val catalogItemLifted: CatalogItemLifted = CatalogItemLifted(catalogItem, storeId)
        catalogItemLifted.catalogItem shouldBe catalogItem
        catalogItemLifted.storeId shouldBe storeId
      }
    }
  }
}

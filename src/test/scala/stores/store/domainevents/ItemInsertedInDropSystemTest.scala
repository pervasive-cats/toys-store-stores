/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.domainevents

import io.github.pervasivecats.stores.store.valueobjects.CatalogItem
import io.github.pervasivecats.stores.store.valueobjects.Item
import io.github.pervasivecats.stores.store.valueobjects.ItemId
import io.github.pervasivecats.stores.store.valueobjects.StoreId

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*

class ItemInsertedInDropSystemTest extends AnyFunSpec {

  describe("An item inserted in drop system") {
    describe("when created with a catalog item, an item id and a store id") {
      it("should contain them") {
        val catalogItem: CatalogItem = CatalogItem(9000).getOrElse(fail())
        val itemId: ItemId = ItemId(9231).getOrElse(fail())
        val storeId: StoreId = StoreId(8140).getOrElse(fail())
        val itemDetected: ItemInsertedInDropSystem = ItemInsertedInDropSystem(catalogItem, itemId, storeId)
        itemDetected.itemId shouldBe itemId
        itemDetected.catalogItem shouldBe catalogItem
        itemDetected.storeId shouldBe storeId
      }
    }
  }
}

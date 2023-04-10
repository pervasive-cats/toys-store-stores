/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.domainevents

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*

import stores.store.valueobjects.{CatalogItem, Item, ItemId, StoreId}

class ItemDetectedTest extends AnyFunSpec {

  describe("An item detected") {
    describe("when created with an item, a catalog item and a store id") {
      it("should contain them") {
        val catalogItem: CatalogItem = CatalogItem(9000).getOrElse(fail())
        val itemId: ItemId = ItemId(9231).getOrElse(fail())
        val storeId: StoreId = StoreId(8140).getOrElse(fail())
        val itemDetected: ItemDetected = ItemDetected(itemId, catalogItem, storeId)
        itemDetected.itemId shouldBe itemId
        itemDetected.catalogItem shouldBe catalogItem
        itemDetected.storeId shouldBe storeId
      }
    }
  }
}

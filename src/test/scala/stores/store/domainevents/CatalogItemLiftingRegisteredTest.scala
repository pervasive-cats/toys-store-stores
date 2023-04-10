/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.domainevents

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*

import stores.store.valueobjects.{CatalogItem, ItemsRowId, ShelfId, ShelvingGroupId, ShelvingId, StoreId}

class CatalogItemLiftingRegisteredTest extends AnyFunSpec {

  describe("A catalog item lifting registered") {
    describe("when created with a store id, a shelving group id, a shelving id, a shelf id and an items row id") {
      it("should contain them") {
        val storeId: StoreId = StoreId(8140).getOrElse(fail())
        val shelvingGroupId: ShelvingGroupId = ShelvingGroupId(9001).getOrElse(fail())
        val shelvingId: ShelvingId = ShelvingId(9002).getOrElse(fail())
        val shelfId: ShelfId = ShelfId(9003).getOrElse(fail())
        val itemsRowId: ItemsRowId = ItemsRowId(9004).getOrElse(fail())
        val catalogItemLiftingRegistered: CatalogItemLiftingRegistered =
          CatalogItemLiftingRegistered(storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId)
        catalogItemLiftingRegistered.storeId shouldBe storeId
        catalogItemLiftingRegistered.shelvingGroupId shouldBe shelvingGroupId
        catalogItemLiftingRegistered.shelvingId shouldBe shelvingId
        catalogItemLiftingRegistered.shelfId shouldBe shelfId
        catalogItemLiftingRegistered.itemsRowId shouldBe itemsRowId
      }
    }
  }
}

/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store

import io.github.pervasivecats.stores.store.Repository.RepositoryOperationFailed
import io.github.pervasivecats.stores.store.valueobjects.{CatalogItem, ItemId, ItemsRowId, ShelfId, ShelvingGroupId, ShelvingId, StoreId}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.EitherValues.given
import scala.language.postfixOps

class RepositoryTest extends AnyFunSpec {

  describe("A Store") {
    describe("after being added") {
      it("should be present") {
        val repository: Repository = Repository()
        val storeId: StoreId = StoreId(1).getOrElse(fail())
        val shelvingGroupId: ShelvingGroupId = ShelvingGroupId(2).getOrElse(fail())
        val shelvingId: ShelvingId = ShelvingId(3).getOrElse(fail())
        val shelfId: ShelfId = ShelfId(4).getOrElse(fail())
        val itemsRowId: ItemsRowId = ItemsRowId(5).getOrElse(fail())
        val catalogItem: CatalogItem = CatalogItem(6).getOrElse(fail())
        val itemId: ItemId = ItemId(7).getOrElse(fail())
        repository.putItem(storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId, catalogItem, itemId)
        repository.findItem(storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId).value shouldBe (catalogItem, itemId)
      }
    }

    describe("if never added") {
      it("should not be present") {
        val repository: Repository = Repository()
        val storeId: StoreId = StoreId(1).getOrElse(fail())
        val shelvingGroupId: ShelvingGroupId = ShelvingGroupId(2).getOrElse(fail())
        val shelvingId: ShelvingId = ShelvingId(3).getOrElse(fail())
        val shelfId: ShelfId = ShelfId(4).getOrElse(fail())
        val itemsRowId: ItemsRowId = ItemsRowId(5).getOrElse(fail())
        repository.findItem(storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId).left.value shouldBe RepositoryOperationFailed
      }
    }
  }
}

/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store

import io.github.pervasivecats.stores.Validated
import io.github.pervasivecats.stores.ValidationError

import stores.store.valueobjects.{CatalogItem, ItemId, ItemsRowId, ShelfId, ShelvingGroupId, ShelvingId, StoreId}

trait Repository {

  def findItem(
    storeId: StoreId,
    shelvingGroupId: ShelvingGroupId,
    shelvingId: ShelvingId,
    shelfId: ShelfId,
    itemsRowId: ItemsRowId
  ): Validated[(CatalogItem, ItemId)]

  def putItem(
    storeId: StoreId,
    shelvingGroupId: ShelvingGroupId,
    shelvingId: ShelvingId,
    shelfId: ShelfId,
    itemsRowId: ItemsRowId,
    catalogItem: CatalogItem,
    itemId: ItemId
  ): Validated[Unit]
}

object Repository {

  case object RepositoryOperationFailed extends ValidationError {

    override val message: String = "The operation on the repository was not correctly performed"
  }

  private class RepositoryImpl() extends Repository {

    @SuppressWarnings(Array("org.wartremover.warts.Var", "scalafix:DisableSyntax.var"))
    private var items: Map[(StoreId, ShelvingGroupId, ShelvingId, ShelfId, ItemsRowId), (CatalogItem, ItemId)] =
      Map.empty[(StoreId, ShelvingGroupId, ShelvingId, ShelfId, ItemsRowId), (CatalogItem, ItemId)]

    override def findItem(
      storeId: StoreId,
      shelvingGroupId: ShelvingGroupId,
      shelvingId: ShelvingId,
      shelfId: ShelfId,
      itemsRowId: ItemsRowId
    ): Validated[(CatalogItem, ItemId)] =
      if (items.contains((storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId)))
        Right[ValidationError, (CatalogItem, ItemId)](items((storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId)))
      else
        Left[ValidationError, (CatalogItem, ItemId)](RepositoryOperationFailed)

    override def putItem(
      storeId: StoreId,
      shelvingGroupId: ShelvingGroupId,
      shelvingId: ShelvingId,
      shelfId: ShelfId,
      itemsRowId: ItemsRowId,
      catalogItem: CatalogItem,
      itemId: ItemId
    ): Validated[Unit] =
      if (items.contains((storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId)))
        Left[ValidationError, Unit](RepositoryOperationFailed)
      else
        Right[ValidationError, Unit]((items = items ++ Map((storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId) -> (catalogItem, itemId))))
  }

  def apply(): Repository = RepositoryImpl()
}

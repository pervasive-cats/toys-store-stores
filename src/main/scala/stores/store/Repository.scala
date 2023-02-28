/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store

import stores.{Validated, ValidationError}
import stores.store.valueobjects.*

trait Repository {

  def findStore(
    storeId: StoreId,
    shelvingGroupId: ShelvingGroupId,
    shelvingId: ShelvingId,
    shelfId: ShelfId,
    itemsRowId: ItemsRowId
  ): Validated[(CatalogItem, ItemId)]

  def putStore(
    storeId: StoreId,
    shelvingGroupId: ShelvingGroupId,
    shelvingId: ShelvingId,
    shelfId: ShelfId,
    itemsRowId: ItemsRowId,
    catalogItem: CatalogItem,
    itemId: ItemId
  ): Validated[Unit]

  def removeStore(
   storeId: StoreId,
   shelvingGroupId: ShelvingGroupId,
   shelvingId: ShelvingId,
   shelfId: ShelfId,
   itemsRowId: ItemsRowId): Validated[Unit]
}

object Repository {

  case object RepositoryOperationFailed extends ValidationError {

    override val message: String = "The operation on the repository was not correctly performed"
  }

  private class RepositoryImpl() extends Repository {

    @SuppressWarnings(Array("org.wartremover.warts.Var", "scalafix:DisableSyntax.var"))
    private var items: Map[(StoreId, ShelvingGroupId, ShelvingId, ShelfId, ItemsRowId), (CatalogItem, ItemId)] =
      Map.empty[(StoreId, ShelvingGroupId, ShelvingId, ShelfId, ItemsRowId), (CatalogItem, ItemId)]

    override def findStore(
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

    override def putStore(
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

    override def removeStore(storeId: StoreId, shelvingGroupId: ShelvingGroupId, shelvingId: ShelvingId, shelfId: ShelfId, itemsRowId: ItemsRowId): Validated[Unit] =
      if (items.contains((storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId)))
        Right[ValidationError, Unit]((items = items - {(storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId)}))
      else
        Left[ValidationError, Unit](RepositoryOperationFailed)
  }

  def apply(): Repository = RepositoryImpl()
}

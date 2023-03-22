/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store

import stores.{Validated, ValidationError}
import stores.store.valueobjects.*

trait RepositoryOld {

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
    itemsRowId: ItemsRowId
  ): Validated[Unit]
}

object RepositoryOld {

  case object RepositoryOperationFailed extends ValidationError {

    override val message: String = "The operation on the repository was not correctly performed"
  }

  private class RepositoryOldImpl() extends RepositoryOld {

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
        items += ((storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId) -> (catalogItem, itemId))
        Right[ValidationError, Unit](())

    override def removeStore(
      storeId: StoreId,
      shelvingGroupId: ShelvingGroupId,
      shelvingId: ShelvingId,
      shelfId: ShelfId,
      itemsRowId: ItemsRowId
    ): Validated[Unit] =
      if (items.contains((storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId)))
        items -= (storeId, shelvingGroupId, shelvingId, shelfId, itemsRowId)
        Right[ValidationError, Unit](())
      else Left[ValidationError, Unit](RepositoryOperationFailed)
  }

  def apply(): RepositoryOld = RepositoryOldImpl()
}

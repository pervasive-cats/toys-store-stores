/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.domainevents

import stores.store.valueobjects.{ItemsRowId, ShelfId, ShelvingGroupId, ShelvingId, StoreId}

trait CatalogItemLiftingRegistered {

  val storeId: StoreId

  val shelvingGroupId: ShelvingGroupId

  val shelvingId: ShelvingId

  val shelfId: ShelfId

  val itemsRowId: ItemsRowId
}

object CatalogItemLiftingRegistered {

  private case class CatalogItemLiftingRegisteredImpl(
                                                       storeId: StoreId,
                                                       shelvingGroupId: ShelvingGroupId,
                                                       shelvingId: ShelvingId,
                                                       shelfId: ShelfId,
                                                       itemsRowId: ItemsRowId) extends CatalogItemLiftingRegistered

  def apply(storeId: StoreId,
            shelvingGroupId: ShelvingGroupId,
            shelvingId: ShelvingId,
            shelfId: ShelfId,
            itemsRowId: ItemsRowId): CatalogItemLiftingRegistered = CatalogItemLiftingRegisteredImpl(
    storeId,
    shelvingGroupId,
    shelvingId,
    shelfId,
    itemsRowId
  )
}

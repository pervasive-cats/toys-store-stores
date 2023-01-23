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
  val itemsRowId:ItemsRowId
}

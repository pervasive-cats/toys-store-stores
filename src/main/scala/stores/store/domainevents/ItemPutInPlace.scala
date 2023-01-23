/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.domainevents

import stores.store.valueobjects.{CatalogItem, ItemId, StoreId}

trait ItemPutInPlace {

  val storeId: StoreId

  val catalogItem: CatalogItem

  val itemId: ItemId
}

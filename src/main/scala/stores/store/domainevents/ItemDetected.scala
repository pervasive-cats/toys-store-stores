/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.domainevents

import stores.store.valueobjects.{CatalogItem, Item, StoreId}

trait ItemDetected {

  val item: Item

  val catalogItem: CatalogItem

  val storeId: StoreId
}

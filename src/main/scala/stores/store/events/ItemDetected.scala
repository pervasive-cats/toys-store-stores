/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.events

import stores.store.valueobjects.{CatalogItem, StoreId}

trait ItemDetected {

  val catalogItem: CatalogItem
  val storeId: StoreId
}

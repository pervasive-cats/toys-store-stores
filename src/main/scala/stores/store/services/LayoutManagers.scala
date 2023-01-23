/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.services

import stores.store.valueobjects.{CatalogItem, Item, StoreId}
import stores.Validated

trait LayoutManagers {

  def existsLayoutWithItem(storeId: StoreId, catalogItem: CatalogItem, item: Item): Validated[Boolean]

  def putCatalogItemInPlace(storeId: StoreId, catalogItem: CatalogItem): Validated[Unit]
}

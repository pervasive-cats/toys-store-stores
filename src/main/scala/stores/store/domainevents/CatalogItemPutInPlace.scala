/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.domainevents

import stores.store.valueobjects.{CatalogItem, StoreId}

trait CatalogItemPutInPlace {

  val catalogItem: CatalogItem

  val storeId: StoreId
}

object CatalogItemPutInPlace {

  private case class CatalogItemPutInPlaceImpl(catalogItem: CatalogItem, storeId: StoreId) extends CatalogItemPutInPlace

  def apply(catalogItem: CatalogItem, storeId: StoreId): CatalogItemPutInPlace = CatalogItemPutInPlaceImpl(catalogItem, storeId)
}

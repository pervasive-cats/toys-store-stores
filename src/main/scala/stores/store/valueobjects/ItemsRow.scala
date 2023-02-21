/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

trait ItemsRow {

  val itemsRowId: ItemsRowId

  val catalogItem: CatalogItem

  val count: Count
}

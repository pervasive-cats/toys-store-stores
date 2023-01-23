/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

trait ShelfOps[A <: Shelf] {

  def addItemsRow(itemsRow: ItemsRow): A

  def removeItemsRow(itemsRowId: ItemsRowId): A

  def updateItemsRow(itemsRow: ItemsRow): A
}

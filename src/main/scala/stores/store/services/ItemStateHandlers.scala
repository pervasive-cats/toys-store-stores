/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.services

import stores.store.domainevents.{CatalogItemLiftingRegistered, ItemDetected, ItemInsertedInDropSystem, ItemReturned}

trait ItemStateHandlers {

  def onItemInserted(event: ItemInsertedInDropSystem): Unit

  def onItemReturned(event: ItemReturned): Unit

  def onCatalogItemLiftingRegistered(event: CatalogItemLiftingRegistered): Unit

  def onItemDetected(event: ItemDetected): Unit
}

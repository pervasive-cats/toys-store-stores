/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.services

import io.github.pervasivecats.stores.Validated
import io.github.pervasivecats.stores.ValidationError

import stores.store.domainevents.{CatalogItemLiftingRegistered, ItemDetected, ItemInsertedInDropSystem, ItemReturned}

trait ItemStateHandlers {

  def onItemInserted(event: ItemInsertedInDropSystem): Validated[Unit]

  def onItemReturned(event: ItemReturned): Validated[Unit]

  def onCatalogItemLiftingRegistered(event: CatalogItemLiftingRegistered): Validated[Unit]

  def onItemDetected(event: ItemDetected): Validated[Unit]
}

object ItemStateHandlers extends ItemStateHandlers {

  override def onItemInserted(event: ItemInsertedInDropSystem): Validated[Unit] = Right[ValidationError, Unit](())

  override def onItemReturned(event: ItemReturned): Validated[Unit] = Right[ValidationError, Unit](())

  override def onCatalogItemLiftingRegistered(event: CatalogItemLiftingRegistered): Validated[Unit] =
    Right[ValidationError, Unit](())

  override def onItemDetected(event: ItemDetected): Validated[Unit] = Right[ValidationError, Unit](())

}

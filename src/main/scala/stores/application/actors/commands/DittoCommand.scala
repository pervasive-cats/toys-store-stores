/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.application.actors.commands

import org.eclipse.ditto.client.DittoClient

import stores.store.entities.Store
import stores.store.valueobjects.{CatalogItem, ItemId, StoreId}

sealed abstract class Currency

object Currency {
  case object EUR extends Currency
  case object BGP extends Currency
  case object USD extends Currency
  case object CHF extends Currency
}

sealed trait DittoCommand

object DittoCommand {

  final case class DittoClientConnected(client: DittoClient) extends DittoCommand

  case object DittoMessagesIncoming extends DittoCommand

  final case class RaiseAlarm(storeId: StoreId) extends DittoCommand

  final case class ItemDetected(store: Store, catalogItem: CatalogItem, itemId: ItemId) extends DittoCommand

  final case class ItemInsertedIntoDropSystem(store: Store, catalogItem: CatalogItem, itemId: ItemId) extends DittoCommand

  final case class ItemReturned(store: Store, catalogItem: CatalogItem, itemId: ItemId) extends DittoCommand

  final case class ShowItemData(store: Store, name: String, description: String, amount: Int, currency: Currency) extends DittoCommand
}

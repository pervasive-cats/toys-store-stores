/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.application.actors.commands

import akka.actor.typed.ActorRef
import org.eclipse.ditto.client.DittoClient

import stores.store.valueobjects.{CatalogItem, Currency, ItemId, ItemsRowId, ShelfId, ShelvingGroupId, ShelvingId, StoreId}

sealed trait DittoCommand

object DittoCommand {

  final case class DittoClientConnected(client: DittoClient) extends DittoCommand

  case object DittoMessagesIncoming extends DittoCommand

  final case class RaiseAlarm(storeId: StoreId) extends DittoCommand

  final case class ItemDetected(storeId: StoreId, catalogItem: CatalogItem, itemId: ItemId) extends DittoCommand

  final case class ItemInsertedIntoDropSystem(storeId: StoreId, catalogItem: CatalogItem, itemId: ItemId) extends DittoCommand

  final case class ItemReturned(storeId: StoreId, catalogItem: CatalogItem, itemId: ItemId) extends DittoCommand

  final case class ShowItemData(storeId: StoreId, name: String, description: String, amount: Double, currency: Currency)
    extends DittoCommand

  final case class CatalogItemLiftingRegistered(
    storeId: StoreId,
    shelvingGroupId: ShelvingGroupId,
    shelvingId: ShelvingId,
    shelfId: ShelfId,
    itemsRowId: ItemsRowId
  ) extends DittoCommand

  final case class AddShelving(
    storeId: StoreId,
    shelvingGroupId: ShelvingGroupId,
    shelvingId: ShelvingId,
    replyTo: ActorRef[Validated[Unit]]
  ) extends DittoCommand

  final case class RemoveShelving(
    storeId: StoreId,
    shelvingGroupId: ShelvingGroupId,
    shelvingId: ShelvingId,
    replyTo: ActorRef[Validated[Unit]]
  ) extends DittoCommand

  final case class AddShelf(
    storeId: StoreId,
    shelvingGroupId: ShelvingGroupId,
    shelvingId: ShelvingId,
    shelfId: ShelfId,
    replyTo: ActorRef[Validated[Unit]]
  ) extends DittoCommand

  final case class RemoveShelf(
    storeId: StoreId,
    shelvingGroupId: ShelvingGroupId,
    shelvingId: ShelvingId,
    shelfId: ShelfId,
    replyTo: ActorRef[Validated[Unit]]
  ) extends DittoCommand

  final case class AddItemsRow(
    storeId: StoreId,
    shelvingGroupId: ShelvingGroupId,
    shelvingId: ShelvingId,
    shelfId: ShelfId,
    itemsRowId: ItemsRowId,
    replyTo: ActorRef[Validated[Unit]]
  ) extends DittoCommand

  final case class RemoveItemsRow(
    storeId: StoreId,
    shelvingGroupId: ShelvingGroupId,
    shelvingId: ShelvingId,
    shelfId: ShelfId,
    itemsRowId: ItemsRowId,
    replyTo: ActorRef[Validated[Unit]]
  ) extends DittoCommand
}

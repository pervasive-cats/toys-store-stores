/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.services

import io.github.pervasivecats.stores.store.valueobjects.CatalogItem

import akka.actor.typed.ActorRef

import stores.application.actors.commands.DittoCommand.RaiseAlarm
import stores.application.actors.commands.MessageBrokerCommand
import stores.store.domainevents.{CatalogItemLiftingRegistered, ItemDetected, ItemInsertedInDropSystem, ItemReturned}
import stores.application.actors.commands.DittoCommand
import stores.store.Repository
import AnyOps.*

trait ItemStateHandlers {

  def onItemInserted(event: ItemInsertedInDropSystem): Unit

  def onItemReturned(event: ItemReturned): Unit

  def onCatalogItemLiftingRegistered(event: CatalogItemLiftingRegistered)(using Repository): Unit

  def onItemDetected(event: ItemDetected): Unit
}

object ItemStateHandlers {

  private class ItemStateHandlersImpl(messageBrokerActor: ActorRef[MessageBrokerCommand], dittoActor: ActorRef[DittoCommand])
    extends ItemStateHandlers {

    override def onItemInserted(event: ItemInsertedInDropSystem): Unit = println(
      s"[event] onItemInserted(storeId=${event.storeId}, catalogItemId=${event.catalogItem}, itemId=${event.itemId})"
    )

    override def onItemReturned(event: ItemReturned): Unit = println(
      s"[event] onItemReturned(storeId=${event.storeId}, catalogItemId=${event.catalogItem}, itemId=${event.itemId})"
    )

    override def onCatalogItemLiftingRegistered(event: CatalogItemLiftingRegistered)(using Repository): Unit = {
      summon[Repository]
        .findById(event.storeId)
        .fold(
          _ => println(s"[eventERROR] onCatalogItemLiftingRegistered(storeId=${event.storeId}"),
          store => {
            for {
              shelvingGroup <- store.layout.find(s => s.shelvingGroupId === event.shelvingGroupId)
              shelving <- shelvingGroup.shelvings.find(s => s.shelvingId === event.shelvingId)
              shelf <- shelving.shelves.find(s => s.shelfId === event.shelfId)
              itemsRow <- shelf.itemsRows.find(i => i.itemsRowId === event.itemsRowId)
            } yield itemsRow.catalogItem

            println(
              s"[event] onCatalogItemLiftingRegistered(storeId=${event.storeId}, shelvingGroupId=${
                  event.shelvingGroupId
                }, shelvingId=${event.shelvingId}, shelfId=${event.shelfId}, itemsRowId=${event.itemsRowId})"
            )
          }
        )
    }

    override def onItemDetected(event: ItemDetected): Unit = {
      println(
        s"[event] Item detected by anti-theft system. (storeId=${event.storeId}), catalogItemId=${event.catalogItem}, itemId=${event.itemId})"
      )
      dittoActor ! RaiseAlarm(event.storeId)
    }
  }

  def apply(messageBrokerActor: ActorRef[MessageBrokerCommand], dittoActor: ActorRef[DittoCommand]): ItemStateHandlers =
    ItemStateHandlersImpl(messageBrokerActor, dittoActor)
}

/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.services

import io.github.pervasivecats.stores.application.actors.commands.DittoCommand.RaiseAlarm

import akka.actor.typed.ActorRef

import stores.application.actors.commands.MessageBrokerCommand
import stores.store.domainevents.{CatalogItemLiftingRegistered, ItemDetected, ItemInsertedInDropSystem, ItemReturned}
import stores.application.actors.commands.DittoCommand

trait ItemStateHandlers {

  def onItemInserted(event: ItemInsertedInDropSystem): Unit

  def onItemReturned(event: ItemReturned): Unit

  def onCatalogItemLiftingRegistered(event: CatalogItemLiftingRegistered): Unit

  def onItemDetected(event: ItemDetected): Unit
}

object ItemStateHandlers {

  private class ItemStateHandlersImpl(messageBrokerActor: ActorRef[MessageBrokerCommand], dittoActor: ActorRef[DittoCommand])
    extends ItemStateHandlers {

    override def onItemReturned(event: ItemReturned): Unit = println("[event] onItemReturned")

    override def onItemInserted(event: ItemInsertedInDropSystem): Unit = println("[event] onItemInserted")

    override def onCatalogItemLiftingRegistered(event: CatalogItemLiftingRegistered): Unit = println(
      "[event] onCatalogItemLiftingRegistered"
    )

    override def onItemDetected(event: ItemDetected): Unit = {
      println("[event] Item detected by anti-theft system.")
      dittoActor ! RaiseAlarm(event.storeId)
    }
  }

  def apply(messageBrokerActor: ActorRef[MessageBrokerCommand], dittoActor: ActorRef[DittoCommand]): ItemStateHandlers =
    ItemStateHandlersImpl(messageBrokerActor, dittoActor)
}

/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.application.actors.commands

import stores.store.domainevents.{
  ItemReturned as ItemReturnedEvent,
  CatalogItemLiftingRegistered as CatalogItemLiftingRegisteredEvent,
  CatalogItemLifted as CatalogItemLiftedEvent
}

sealed trait MessageBrokerCommand

object MessageBrokerCommand {

  final case class ItemReturned(event: ItemReturnedEvent, replyTo: String, correlationId: String) extends MessageBrokerCommand

  final case class CatalogItemLiftingRegistered(event: CatalogItemLiftingRegisteredEvent, replyTo: String, correlationId: String)
    extends MessageBrokerCommand

  final case class CatalogItemLifted(event: CatalogItemLiftedEvent, replyTo: String, correlationId: String)
    extends MessageBrokerCommand
}

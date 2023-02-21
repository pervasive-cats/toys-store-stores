/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

import eu.timepit.refined.api.RefType.applyRef

import stores.{Id, Validated, ValidationError}

trait ItemId {

  val value: Id
}

object ItemId {

  private case class ItemIdImpl(value: Id) extends ItemId

  case object WrongItemIdFormat extends ValidationError {

    override val message: String = "The item id is a negative value"
  }

  def apply(value: Long): Validated[ItemId] = applyRef[Id](value) match {
    case Left(_) => Left[ValidationError, ItemId](WrongItemIdFormat)
    case Right(value) => Right[ValidationError, ItemId](ItemIdImpl(value))
  }
}

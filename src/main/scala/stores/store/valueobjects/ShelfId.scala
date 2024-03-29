/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

import io.github.pervasivecats.ValidationError

import eu.timepit.refined.api.RefType.applyRef

import stores.Id

trait ShelfId {

  val value: Id
}

object ShelfId {

  private case class ShelfIdImpl(value: Id) extends ShelfId

  case object WrongShelfIdFormat extends ValidationError {

    override val message: String = "The shelf id is a negative value"
  }

  def apply(value: Long): Validated[ShelfId] = applyRef[Id](value) match {
    case Left(_) => Left[ValidationError, ShelfId](WrongShelfIdFormat)
    case Right(value) => Right[ValidationError, ShelfId](ShelfIdImpl(value))
  }
}

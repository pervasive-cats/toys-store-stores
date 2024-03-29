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

trait ShelvingId {

  val value: Id
}

object ShelvingId {

  private case class ShelvingIdImpl(value: Id) extends ShelvingId

  case object WrongShelvingIdFormat extends ValidationError {

    override val message: String = "The shelving id is a negative value"
  }

  def apply(value: Long): Validated[ShelvingId] = applyRef[Id](value) match {
    case Left(_) => Left[ValidationError, ShelvingId](WrongShelvingIdFormat)
    case Right(value) => Right[ValidationError, ShelvingId](ShelvingIdImpl(value))
  }
}

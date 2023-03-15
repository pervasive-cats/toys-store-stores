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

trait ShelvingGroupId {

  val value: Id
}

object ShelvingGroupId {

  private case class ShelvingGroupIdImpl(value: Id) extends ShelvingGroupId

  case object WrongShelvingGroupIdFormat extends ValidationError {

    override val message: String = "The shelving group id is a negative value"
  }

  def apply(value: Long): Validated[ShelvingGroupId] = applyRef[Id](value) match {
    case Left(_) => Left[ValidationError, ShelvingGroupId](WrongShelvingGroupIdFormat)
    case Right(value) => Right[ValidationError, ShelvingGroupId](ShelvingGroupIdImpl(value))
  }
}

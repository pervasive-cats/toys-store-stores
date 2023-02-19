/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

import eu.timepit.refined.api.RefType.applyRef
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.NonNegative
import io.github.pervasivecats.stores.{Validated, ValidationError}

type CountInt = Int Refined NonNegative

trait Count {

  val value: CountInt
}

object Count {

  final private case class CountImpl(value: CountInt) extends Count

  case object WrongCountFormat extends ValidationError {

    override val message: String = "The count is a non positive value"
  }

  def apply(value: Int): Validated[Count] = applyRef[CountInt](value) match {
    case Left(_) => Left[ValidationError, Count](WrongCountFormat)
    case Right(value) => Right[ValidationError, Count](CountImpl(value))
  }
}
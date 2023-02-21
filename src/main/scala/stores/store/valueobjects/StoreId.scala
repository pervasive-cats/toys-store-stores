/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

import eu.timepit.refined.api.RefType.applyRef

import stores.{Id, Validated, ValidationError}

trait StoreId {

  val value: Id
}

object StoreId {

  private case class StoreIdImpl(value: Id) extends StoreId

  case object WrongStoreIdFormat extends ValidationError {

    override val message: String = "The store id format is a negative value"
  }

  def apply(value: Long): Validated[StoreId] = applyRef[Id](value) match {
    case Left(_) => Left[ValidationError, StoreId](WrongStoreIdFormat)
    case Right(value) => Right[ValidationError, StoreId](StoreIdImpl(value))
  }
}

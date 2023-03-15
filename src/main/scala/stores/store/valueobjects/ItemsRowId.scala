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

trait ItemsRowId {

  val value: Id
}

object ItemsRowId {

  final private case class ItemsRowIdImpl(value: Id) extends ItemsRowId

  case object WrongItemsRowId extends ValidationError {

    override val message: String = "The items row id is a negative value"
  }

  def apply(value: Long): Validated[ItemsRowId] = applyRef[Id](value) match {
    case Left(_) => Left[ValidationError, ItemsRowId](WrongItemsRowId)
    case Right(value) => Right[ValidationError, ItemsRowId](ItemsRowIdImpl(value))
  }
}

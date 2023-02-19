/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

import eu.timepit.refined.api.RefType.applyRef

import stores.{Id, Validated, ValidationError}

trait CatalogItem {

  val id: Id
}

object CatalogItem {

  final private case class CatalogItemImpl(id: Id) extends CatalogItem

  case object WrongCatalogItemIdFormat extends ValidationError {

    override val message: String = "The catalog item id is a negative value"
  }

  def apply(value: Long): Validated[CatalogItem] = applyRef[Id](value) match {
    case Left(_) => Left[ValidationError, CatalogItem](WrongCatalogItemIdFormat)
    case Right(value) => Right[ValidationError, CatalogItem](CatalogItemImpl(value))
  }
}

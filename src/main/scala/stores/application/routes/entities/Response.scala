/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.application.routes.entities

import stores.store.entities.Store

sealed trait Response[A] {

  val result: Validated[A]
}

object Response {

  final case class StoreResponse(result: Validated[Store]) extends Response[Store]

  final case class EmptyResponse(result: Validated[Unit]) extends Response[Unit]
}

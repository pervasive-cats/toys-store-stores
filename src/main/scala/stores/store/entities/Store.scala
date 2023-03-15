/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.entities

import stores.store.valueobjects.{ShelvingGroup, StoreId}

trait Store {

  val storeId: StoreId

  val layout: List[ShelvingGroup]
}

object Store {

  private case class StoreImpl(storeId: StoreId, layout: List[ShelvingGroup]) extends Store

  @SuppressWarnings(Array("org.wartremover.warts.DefaultArguments", "scalafix:DisableSyntax.defaultArgs"))
  def apply(storeId: StoreId, layout: List[ShelvingGroup] = List.empty): Store = StoreImpl(storeId, layout)
}

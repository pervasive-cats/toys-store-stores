/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.entities

import AnyOps.*
import stores.store.entities.StoreOps.*
import stores.store.valueobjects.{ShelvingGroup, ShelvingGroupId, StoreId}

trait Store {

  val storeId: StoreId

  val layout: List[ShelvingGroup]
}

object Store {

  final private case class StoreImpl(storeId: StoreId, layout: List[ShelvingGroup]) extends Store

  given StoreOps[Store] with {

    override def addShelvingGroup(store: Store, shelvingGroup: ShelvingGroup): Store =
      StoreImpl(store.storeId, store.layout ++ List[ShelvingGroup](shelvingGroup))

    override def removeShelvingGroup(store: Store, shelvingGroupId: ShelvingGroupId): Store =
      StoreImpl(store.storeId, store.layout.filter(_.shelvingGroupId !== shelvingGroupId))

    override def updateShelvingGroup(store: Store, shelvingGroup: ShelvingGroup): Store =
      store
        .removeShelvingGroup(shelvingGroup.shelvingGroupId)
        .addShelvingGroup(shelvingGroup)
  }

  def apply(storeId: StoreId, layout: List[ShelvingGroup]): Store = StoreImpl(storeId, layout)
}
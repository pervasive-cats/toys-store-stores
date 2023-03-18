/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.entities

import stores.store.entities.Store
import stores.store.valueobjects.{ShelvingGroup, ShelvingGroupId}

trait StoreOps[A <: Store] {

  def addShelvingGroup(store: Store, shelvingGroup: ShelvingGroup): Store

  def removeShelvingGroup(store: Store, shelvingGroupId: ShelvingGroupId): Store

  def updateShelvingGroup(store: Store, shelvingGroup: ShelvingGroup): Store
}

object StoreOps {

  extension [A <: Store: StoreOps](store: A) {

    def addShelvingGroup(shelvingGroup: ShelvingGroup): Store = implicitly[StoreOps[A]].addShelvingGroup(store, shelvingGroup)

    def removeShelvingGroup(shelvingGroupId: ShelvingGroupId): Store = implicitly[StoreOps[A]].removeShelvingGroup(store, shelvingGroupId)

    def updateShelvingGroup(shelvingGroup: ShelvingGroup): Store = implicitly[StoreOps[A]].updateShelvingGroup(store, shelvingGroup)
  }
}
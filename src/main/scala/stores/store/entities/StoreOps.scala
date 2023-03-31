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

  def addShelvingGroup(store: A, shelvingGroup: ShelvingGroup): A

  def removeShelvingGroup(store: A, shelvingGroupId: ShelvingGroupId): A

  def updateShelvingGroup(store: A, shelvingGroup: ShelvingGroup): A
}

object StoreOps {

  extension [A <: Store: StoreOps](store: A) {

    def addShelvingGroup(shelvingGroup: ShelvingGroup): A = implicitly[StoreOps[A]].addShelvingGroup(store, shelvingGroup)

    def removeShelvingGroup(shelvingGroupId: ShelvingGroupId): A =
      implicitly[StoreOps[A]].removeShelvingGroup(store, shelvingGroupId)

    def updateShelvingGroup(shelvingGroup: ShelvingGroup): A =
      implicitly[StoreOps[A]].updateShelvingGroup(store, shelvingGroup)
  }
}

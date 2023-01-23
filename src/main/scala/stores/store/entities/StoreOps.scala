/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.entities

import stores.store.valueobjects.{ShelvingGroup, ShelvingGroupId}

trait StoreOps {

  def addShelvingGroup(shelvingGroup: ShelvingGroup): Store

  def removeShelvingGroup(shelvingGroupId: ShelvingGroupId): Store

  def updateShelvingGroup(shelvingGroup: ShelvingGroup): Store
}

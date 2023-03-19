/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

trait ShelvingGroupOps[A <: ShelvingGroup] {

  def addShelving(shelvingGroup: ShelvingGroup, shelving: Shelving): ShelvingGroup

  def removeShelving(shelvingGroup: ShelvingGroup,shelvingId: ShelvingId): ShelvingGroup

  def updateShelving(shelvingGroup: ShelvingGroup,shelving: Shelving): ShelvingGroup
}

object ShelvingGroupOps{

  extension [A <: ShelvingGroup: ShelvingGroupOps](shelvingGroup: A){

    def addShelving(shelving: Shelving): ShelvingGroup = implicitly[ShelvingGroupOps[A]].addShelving(shelvingGroup, shelving)

    def removeShelving(shelvingId: ShelvingId): ShelvingGroup = implicitly[ShelvingGroupOps[A]].removeShelving(shelvingGroup, shelvingId)

    def updateShelving(shelving: Shelving): ShelvingGroup = implicitly[ShelvingGroupOps[A]].updateShelving(shelvingGroup, shelving)
  }
}
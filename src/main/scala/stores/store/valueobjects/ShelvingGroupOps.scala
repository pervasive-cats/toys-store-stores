/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

trait ShelvingGroupOps[A <: ShelvingGroup] {

  def addShelving(shelvingGroup: A, shelving: Shelving): A

  def removeShelving(shelvingGroup: A, shelvingId: ShelvingId): A

  def updateShelving(shelvingGroup: A, shelving: Shelving): A
}

object ShelvingGroupOps {

  extension [A <: ShelvingGroup: ShelvingGroupOps](shelvingGroup: A) {

    def addShelving(shelving: Shelving): A = implicitly[ShelvingGroupOps[A]].addShelving(shelvingGroup, shelving)

    def removeShelving(shelvingId: ShelvingId): A =
      implicitly[ShelvingGroupOps[A]].removeShelving(shelvingGroup, shelvingId)

    def updateShelving(shelving: Shelving): A =
      implicitly[ShelvingGroupOps[A]].updateShelving(shelvingGroup, shelving)
  }
}

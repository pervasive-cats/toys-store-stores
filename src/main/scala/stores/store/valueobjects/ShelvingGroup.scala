/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

trait ShelvingGroup {

  val shelvingGroupId: ShelvingGroupId
  val shelvings: List[Shelving]

  def addShelving(shelving: Shelving): ShelvingGroup

  def removeShelving(shelvingId: ShelvingId): ShelvingGroup

  def updateShelving(shelving: Shelving): ShelvingGroup
}

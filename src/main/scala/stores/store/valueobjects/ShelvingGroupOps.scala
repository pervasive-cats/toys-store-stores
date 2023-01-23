/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

trait ShelvingGroupOps[A <: ShelvingGroup] {

  def addShelving(shelving: Shelving): A

  def removeShelving(shelvingId: ShelvingId): A

  def updateShelving(shelving: Shelving): A
}

/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

import AnyOps.*
import stores.store.valueobjects.ShelvingGroupOps.*

trait ShelvingGroup {

  val shelvingGroupId: ShelvingGroupId

  val shelvings: List[Shelving]
}

object ShelvingGroup {
  
  final private case class ShelvingGroupImpl(shelvingGroupId: ShelvingGroupId, shelvings: List[Shelving]) extends ShelvingGroup

  given ShelvingGroupOps[ShelvingGroup] with {

    override def addShelving(shelvingGroup: ShelvingGroup, shelving: Shelving): ShelvingGroup =
      ShelvingGroupImpl(shelvingGroup.shelvingGroupId, shelvingGroup.shelvings ++ List[Shelving](shelving))

    override def removeShelving(shelvingGroup: ShelvingGroup, shelvingId: ShelvingId): ShelvingGroup =
      ShelvingGroupImpl(shelvingGroup.shelvingGroupId, shelvingGroup.shelvings.filter(_.shelvingId !== shelvingId))

    override def updateShelving(shelvingGroup: ShelvingGroup, shelving: Shelving): ShelvingGroup =
      shelvingGroup
        .removeShelving(shelving.shelvingId)
        .addShelving(shelving)
  }

  def apply(shelvingGroupId: ShelvingGroupId, shelvings: List[Shelving]): ShelvingGroup = ShelvingGroupImpl(shelvingGroupId, shelvings)
}

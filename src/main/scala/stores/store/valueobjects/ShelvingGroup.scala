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
}

object ShelvingGroup {
  
  final private case class ShelvingGroupImpl(shelvingGroupId: ShelvingGroupId, shelvings: List[Shelving]) extends ShelvingGroup
  
  def apply(shelvingGroupId: ShelvingGroupId, shelvings: List[Shelving]): ShelvingGroup = ShelvingGroupImpl(shelvingGroupId, shelvings)
}

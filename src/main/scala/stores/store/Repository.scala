/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store

import stores.store.valueobjects.{ShelvingGroup, StoreId}
import stores.Validated
import stores.store.entities.Store

trait Repository {

  def findById(storeId: StoreId): Validated[Store]

  def updateLayout(store: Store, layout: List[ShelvingGroup]): Validated[Unit]
}

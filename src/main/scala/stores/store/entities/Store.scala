/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.entities

import stores.store.valueobjects.{ShelvingGroup, StoreId}

trait Store {

  val storeId: StoreId

  val layout: List[ShelvingGroup]
}

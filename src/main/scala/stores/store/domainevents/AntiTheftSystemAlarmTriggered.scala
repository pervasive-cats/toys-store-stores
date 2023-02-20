/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.domainevents

import stores.store.valueobjects.StoreId

trait AntiTheftSystemAlarmTriggered {

  val store: StoreId
}

object AntiTheftSystemAlarmTriggered {

  private case class AntiTheftSystemAlarmTriggeredImpl(store: StoreId) extends AntiTheftSystemAlarmTriggered

  def apply(storeId: StoreId): AntiTheftSystemAlarmTriggered = AntiTheftSystemAlarmTriggeredImpl(storeId)
}

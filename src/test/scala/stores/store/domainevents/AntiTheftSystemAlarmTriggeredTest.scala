package io.github.pervasivecats
package stores.store.domainevents

import io.github.pervasivecats.stores.store.valueobjects.StoreId

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*

class AntiTheftSystemAlarmTriggeredTest extends AnyFunSpec {

  describe("An anti theft system alarm triggered") {
    describe("when created with a store id") {
      it("should be contain them") {
        val storeId: StoreId = StoreId(9000).getOrElse(fail())
        val antiTheftSystemAlarmTriggered: AntiTheftSystemAlarmTriggered = AntiTheftSystemAlarmTriggered(storeId)
        antiTheftSystemAlarmTriggered.store shouldBe storeId
      }
    }
  }
}

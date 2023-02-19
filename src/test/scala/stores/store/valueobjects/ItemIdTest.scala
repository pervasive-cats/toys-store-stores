package io.github.pervasivecats
package stores.store.valueobjects

import org.scalatest.funspec.AnyFunSpec
import eu.timepit.refined.auto.given
import stores.store.valueobjects.ItemId.WrongItemIdFormat
import org.scalatest.EitherValues.given
import org.scalatest.matchers.should.Matchers.*

class ItemIdTest extends AnyFunSpec {

  describe("An item id") {
    describe("when created with a negative value") {
      it("should not be valid") {
        ItemId(-9000).left.value shouldBe WrongItemIdFormat
      }
    }

    describe("when created with a positive value") {
      it("should be valid") {
        val id: Long = 9000

        (ItemId(id).value.value: Long) shouldBe id
      }
    }

    describe("when created with a value of 0") {
      it("should be valid") {
        val id: Long = 0

        (ItemId(id).value.value: Long) shouldBe id
      }
    }
  }
}

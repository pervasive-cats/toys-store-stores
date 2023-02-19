package io.github.pervasivecats
package stores.store.valueobjects

import eu.timepit.refined.auto.given
import org.scalatest.EitherValues.given
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*

import stores.store.valueobjects.ShelvingGroupId.WrongShelvingGroupIdFormat

class ShelvingGroupIdTest extends AnyFunSpec {

  describe("A shelving group id id") {
    describe("when created with a negative value") {
      it("should not be valid") {
        ShelvingGroupId(-9000).left.value shouldBe WrongShelvingGroupIdFormat
      }
    }

    describe("when created with a positive value") {
      it("should be valid") {
        val id: Long = 9000
        (ShelvingGroupId(id).value.value: Long) shouldBe id
      }
    }

    describe("when created with a value of 0") {
      it("should be valid") {
        val id: Long = 0
        (ShelvingGroupId(id).value.value: Long) shouldBe id
      }
    }
  }
}

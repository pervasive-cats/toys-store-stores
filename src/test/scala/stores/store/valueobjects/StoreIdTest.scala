package io.github.pervasivecats
package stores.store.valueobjects

import org.scalatest.funspec.AnyFunSpec
import eu.timepit.refined.auto.given
import stores.store.valueobjects.StoreId.WrongStoreIdFormat

import org.scalatest.EitherValues.given
import org.scalatest.matchers.should.Matchers.*

class StoreIdTest extends AnyFunSpec {

  describe("A shelf id") {
    describe("when created with a negative value") {
      it("should not be valid") {
        StoreId(-9000).left.value shouldBe WrongStoreIdFormat
      }
    }

    describe("when created with a positive value") {
      it("should be valid") {
        val id: Long = 9000
        (StoreId(id).value.value: Long) shouldBe id
      }
    }

    describe("when created with a value of 0") {
      it("should be valid") {
        val id: Long = 0
        (StoreId(id).value.value: Long) shouldBe id
      }
    }
  }
}

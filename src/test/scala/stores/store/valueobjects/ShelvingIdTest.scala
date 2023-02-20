/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

import eu.timepit.refined.auto.given
import org.scalatest.EitherValues.given
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*

import stores.store.valueobjects.ShelvingId.WrongShelvingIdFormat

class ShelvingIdTest extends AnyFunSpec {

  describe("A shelf id") {
    describe("when created with a negative value") {
      it("should not be valid") {
        ShelvingId(-9000).left.value shouldBe WrongShelvingIdFormat
      }
    }

    describe("when created with a positive value") {
      it("should be valid") {
        val id: Long = 9000
        (ShelvingId(id).value.value: Long) shouldBe id
      }
    }

    describe("when created with a value of 0") {
      it("should be valid") {
        val id: Long = 0
        (ShelvingId(id).value.value: Long) shouldBe id
      }
    }
  }
}

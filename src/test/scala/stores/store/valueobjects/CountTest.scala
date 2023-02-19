/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

import io.github.pervasivecats.stores.ValidationError

import eu.timepit.refined.auto.given
import org.scalatest.EitherValues.given
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.shouldBe

import stores.store.valueobjects.Count
import stores.store.valueobjects.Count.WrongCountFormat

class CountTest extends AnyFunSpec {

  private val positiveValue: Int = 9000

  describe("A count value") {
    describe("when created with a negative value") {
      it("should not be valid") {
        Count(-9000).left.value shouldBe WrongCountFormat
      }
    }

    describe("when created with a positive value") {
      it("should be valid") {
        (Count(positiveValue).value.value: Int) shouldBe positiveValue
      }
    }

    describe("when created with the 0 value") {
      it("should be valid") {
        (Count(0).value.value: Int) shouldBe 0
      }
    }
  }
}

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

import stores.store.valueobjects.ItemsRowId.WrongItemsRowId

class ItemsRowIdTest extends AnyFunSpec {

  describe("An Items row id") {
    describe("when created with a negative value") {
      it("should not be valid") {
        ItemsRowId(-9000).left.value shouldBe WrongItemsRowId
      }
    }

    describe("when created with a positive value") {
      it("should be valid") {
        val id: Long = 9000
        (ItemsRowId(id).value.value: Long) shouldBe id
      }
    }

    describe("when created with a value of 0") {
      it("should be valid") {
        val id: Long = 0
        (ItemsRowId(id).value.value: Long) shouldBe id
      }
    }
  }
}

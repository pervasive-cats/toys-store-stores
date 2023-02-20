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

class ItemTest extends AnyFunSpec {

  describe("An Item") {
    describe("when created with an id and a catalog item") {
      it("should contain them") {
        val catalogItem: CatalogItem = CatalogItem(9000).getOrElse(fail())
        val itemId: ItemId = ItemId(8501).getOrElse(fail())
        val item: Item = Item(catalogItem, itemId)
        item.catalogItem shouldBe catalogItem
        item.id shouldBe itemId
      }
    }
  }
}

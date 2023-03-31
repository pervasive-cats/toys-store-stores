/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store

import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.FiniteDuration

import com.dimafeng.testcontainers.JdbcDatabaseContainer.CommonParams
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import io.getquill.JdbcContextConfig
import org.scalatest.EitherValues.given
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*
import org.testcontainers.utility.DockerImageName

import stores.store.entities.Store
import stores.store.valueobjects.*
import stores.store.Repository.{RepositoryOperationFailed, StoreNotFound}
import stores.store.entities.StoreOps.updateShelvingGroup

class RepositoryTest extends AnyFunSpec with TestContainerForAll {

  private val timeout: FiniteDuration = 300.seconds

  override val containerDef: PostgreSQLContainer.Def = PostgreSQLContainer.Def(
    dockerImageName = DockerImageName.parse("postgres:15.1"),
    databaseName = "stores",
    username = "test",
    password = "test",
    commonJdbcParams = CommonParams(timeout, timeout, Some("stores.sql"))
  )

  @SuppressWarnings(Array("org.wartremover.warts.Var", "scalafix:DisableSyntax.var"))
  private var maybeRepository: Option[Repository] = None

  private val storeId: StoreId = StoreId(1).getOrElse(fail())
  private val shelvingGroupId: ShelvingGroupId = ShelvingGroupId(2).getOrElse(fail())
  private val shelvingId: ShelvingId = ShelvingId(3).getOrElse(fail())
  private val shelfId: ShelfId = ShelfId(4).getOrElse(fail())
  private val itemsRowId: ItemsRowId = ItemsRowId(5).getOrElse(fail())
  private val catalogItem: CatalogItem = CatalogItem(6).getOrElse(fail())
  private val count: Count = Count(7).getOrElse(fail())
  private val wrongStoreId: StoreId = StoreId(2).getOrElse(fail())

  private val store: Store = Store(
    storeId,
    Seq(
      ShelvingGroup(
        shelvingGroupId,
        Seq(Shelving(shelvingId, Seq(Shelf(shelfId, Seq(ItemsRow(itemsRowId, catalogItem, count))))))
      )
    )
  )

  override def afterContainersStart(containers: Containers): Unit = {
    val repository: Repository = Repository(
      JdbcContextConfig(
        ConfigFactory
          .load()
          .getConfig("repository")
          .withValue(
            "dataSource.portNumber",
            ConfigValueFactory.fromAnyRef(containers.container.getFirstMappedPort.intValue())
          )
      ).dataSource
    )
    maybeRepository = Some(repository)
  }

  describe("The default store") {
    describe("after being added at database startup") {
      it("should be present") {
        maybeRepository.getOrElse(fail()).findById(storeId).value shouldBe store
      }
    }

    describe("if searched with the wrong identifier") {
      it("should not be present") {
        maybeRepository
          .getOrElse(fail())
          .findById(wrongStoreId)
          .left
          .value shouldBe StoreNotFound
      }
    }

    val updatedStore: Store = store.updateShelvingGroup(
      ShelvingGroup(
        shelvingGroupId,
        Seq(Shelving(shelvingId, Seq(Shelf(shelfId, Seq(ItemsRow(itemsRowId, catalogItem, Count(100).getOrElse(fail())))))))
      )
    )

    describe("after updating its store layout") {
      it("should show the update") {
        val repository: Repository = maybeRepository.getOrElse(fail())
        repository.updateLayout(updatedStore, updatedStore.layout).value shouldBe ()
        repository.findById(storeId).value shouldBe updatedStore
      }
    }

    describe("after updating its store layout with the wrong identifier") {
      it("should not be allowed") {
        val wrongStore: Store = Store(
          wrongStoreId,
          Seq(
            ShelvingGroup(
              shelvingGroupId,
              Seq(Shelving(shelvingId, Seq(Shelf(shelfId, Seq(ItemsRow(itemsRowId, catalogItem, count))))))
            )
          )
        )
        maybeRepository
          .getOrElse(fail())
          .updateLayout(wrongStore, updatedStore.layout)
          .left
          .value shouldBe RepositoryOperationFailed
      }
    }
  }
}

/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store

import javax.sql.DataSource

import scala.util.Try

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import eu.timepit.refined.auto.given
import io.getquill.*
import stores.store.valueobjects.*
import stores.store.entities.Store
import AnyOps.*

trait Repository {

  def findById(storeId: StoreId): Validated[Store]

  def updateLayout(store: Store, layout: Seq[ShelvingGroup]): Validated[Unit]
}

object Repository {

  case object StoreNotFound extends ValidationError {

    override val message: String = "No store found for the id that was provided"
  }

  case object RepositoryOperationFailed extends ValidationError {

    override val message: String = "The operation on the repository was not correctly performed"
  }

  private class PostgresRepository(ctx: PostgresJdbcContext[SnakeCase]) extends Repository {

    import ctx.*

    private case class ItemsRows(
      storeId: Long,
      shelvingGroupId: Long,
      shelvingId: Long,
      shelfId: Long,
      itemsRowId: Long,
      catalogItem: Long,
      count: Int
    )

    private def protectFromException[A](f: => Validated[A]): Validated[A] =
      Try(f).getOrElse(Left[ValidationError, A](RepositoryOperationFailed))

    override def findById(storeId: StoreId): Validated[Store] = protectFromException {
      ctx
        .run(query[ItemsRows].filter(_.storeId === lift[Long](storeId.value)))
        .map(i =>
          for {
            itemsRowId <- ItemsRowId(i.itemsRowId)
            catalogItem <- CatalogItem(i.catalogItem)
            count <- Count(i.count)
            shelfId <- ShelfId(i.shelfId)
            shelvingId <- ShelvingId(i.shelvingId)
            shelvingGroupId <- ShelvingGroupId(i.shelvingGroupId)
          } yield (shelvingGroupId, shelvingId, shelfId, itemsRowId, catalogItem, count)
        )
        .foldLeft[Either[ValidationError, Seq[(ShelvingGroupId, ShelvingId, ShelfId, ItemsRowId, CatalogItem, Count)]]](
          Left[ValidationError, Seq[(ShelvingGroupId, ShelvingId, ShelfId, ItemsRowId, CatalogItem, Count)]](StoreNotFound)
        ) {
          case (Right(s), Right(i)) =>
            Right[ValidationError, Seq[(ShelvingGroupId, ShelvingId, ShelfId, ItemsRowId, CatalogItem, Count)]](s :+ i)
          case (Right(_), Left(v)) =>
            Left[ValidationError, Seq[(ShelvingGroupId, ShelvingId, ShelfId, ItemsRowId, CatalogItem, Count)]](v)
          case (Left(StoreNotFound), Right(i)) =>
            Right[ValidationError, Seq[(ShelvingGroupId, ShelvingId, ShelfId, ItemsRowId, CatalogItem, Count)]](Seq(i))
          case (Left(v), _) =>
            Left[ValidationError, Seq[(ShelvingGroupId, ShelvingId, ShelfId, ItemsRowId, CatalogItem, Count)]](v)
        }
        .map(s =>
          Store(
            storeId,
            s.groupBy(_._1)
              .toSeq
              .map((shelvingGroupId, shelvings) =>
                ShelvingGroup(
                  shelvingGroupId,
                  shelvings
                    .groupBy(_._2)
                    .toSeq
                    .map((shelvingId, shelves) =>
                      Shelving(
                        shelvingId,
                        shelves
                          .groupBy(_._3)
                          .toSeq
                          .map((shelfId, itemsRows) => Shelf(shelfId, itemsRows.map(i => ItemsRow(i._4, i._5, i._6))))
                      )
                    )
                )
              )
          )
        )
    }

    override def updateLayout(store: Store, layout: Seq[ShelvingGroup]): Validated[Unit] = protectFromException {
      ctx
        .transaction {
          if (ctx.run(query[ItemsRows].filter(_.storeId === lift[Long](store.storeId.value)).delete) <= 0)
            Left[ValidationError, Unit](RepositoryOperationFailed)
          else if (
            ctx
              .run(
                liftQuery(
                  for {
                    shelvingGroup <- layout
                    shelving <- shelvingGroup.shelvings
                    shelf <- shelving.shelves
                    itemsRow <- shelf.itemsRows
                  } yield ItemsRows(
                    store.storeId.value,
                    shelvingGroup.shelvingGroupId.value,
                    shelving.shelvingId.value,
                    shelf.shelfId.value,
                    itemsRow.itemsRowId.value,
                    itemsRow.catalogItem.id,
                    itemsRow.count.value
                  )
                ).foreach(i => query[ItemsRows].insertValue(i))
              )
              .forall(_ === 1L)
          )
            Right[ValidationError, Unit](())
          else
            Left[ValidationError, Unit](RepositoryOperationFailed)
        }
    }
  }

  def apply(dataSource: DataSource): Repository = PostgresRepository(PostgresJdbcContext[SnakeCase](SnakeCase, dataSource))
}

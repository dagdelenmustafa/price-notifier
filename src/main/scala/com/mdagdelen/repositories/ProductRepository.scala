/*
 * Copyright 2022 com.dagdelenmustafa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mdagdelen.repositories

import cats.effect.kernel.Async
import cats.implicits._
import com.mdagdelen.exceptions.Exceptions
import com.mdagdelen.models.{Product, ProductEntity}
import com.mdagdelen.types.Types.ProductId
import io.circe.generic.auto._
import mongo4cats.bson.ObjectId
import mongo4cats.circe._
import mongo4cats.collection.MongoCollection
import mongo4cats.database.MongoDatabase
import mongo4cats.operations.Filter

trait ProductRepository[F[_]] {
  def getById(id: ObjectId): F[Product]
  def getByExternalId(externalId: String, marketplace: String): F[Product]
  def insert(product: Product): F[ProductId]
  def insertIfNotExist(product: Product): F[ProductId]
}

final private class ProductRepositoryImpl[F[_]: Async](private val collection: MongoCollection[F, ProductEntity])
    extends ProductRepository[F] {
  override def getById(id: ObjectId): F[Product] =
    collection
      .find(Filter.eq("_id", id))
      .first
      .flatMap(mProduct => Async[F].fromOption(mProduct.map(_.asProduct), Exceptions.EntityDoesNotExist("product", id)))

  override def insert(product: Product): F[ProductId] =
    collection.insertOne(product.asProductEntity).map(_ => product.id)

  def getByExternalId(externalId: String, marketplace: String): F[Product] =
    collection
      .find(Filter.eq("externalId", externalId) && Filter.eq("marketplace", marketplace))
      .first
      .flatMap(mProduct => Async[F].fromOption(mProduct.map(_.asProduct), Exceptions.ProductNotFound()))

  override def insertIfNotExist(product: Product): F[ProductId] = for {
    maybeProduct <- getByExternalIdOpt(product.externalId, product.marketplace)
    productId <- maybeProduct match {
      case Some(value) => Async[F].pure(value.id)
      case None        => insert(product)
    }
  } yield productId

  private def getByExternalIdOpt(externalId: String, marketplace: String): F[Option[Product]] =
    collection
      .find(Filter.eq("externalId", externalId) && Filter.eq("marketplace", marketplace))
      .first
      .map(_.map(_.asProduct))
}

object ProductRepository {
  final private val COLLECTION_NAME: String = "product"

  def make[F[_]: Async](db: MongoDatabase[F]): F[ProductRepository[F]] = {
    db.getCollectionWithCodec[ProductEntity](COLLECTION_NAME).map(col => new ProductRepositoryImpl[F](col))
  }
}

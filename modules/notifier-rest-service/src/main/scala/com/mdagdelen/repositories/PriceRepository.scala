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
import com.mdagdelen.models.{ProductPriceEntity, ProductPriceModel}
import com.mdagdelen.types.Types.{ProductId, ProductPriceId}
import mongo4cats.bson.ObjectId
import mongo4cats.circe._
import mongo4cats.collection.MongoCollection
import mongo4cats.database.MongoDatabase
import mongo4cats.operations.Filter

trait PriceRepository[F[_]] {
  def getById(id: ObjectId): F[ProductPriceModel]
  def getLatestPriceOfProduct(productId: ProductId): F[Option[ProductPriceModel]]
  def insert(price: ProductPriceModel): F[ProductPriceId]
}

final private class PriceRepositoryImpl[F[_]: Async](private val collection: MongoCollection[F, ProductPriceEntity])
    extends PriceRepository[F] {
  override def getById(id: ObjectId): F[ProductPriceModel] =
    collection
      .find(Filter.eq("_id", id))
      .first
      .flatMap(mPrice => Async[F].fromOption(mPrice.map(_.asModel), Exceptions.EntityDoesNotExist("price", id)))

  override def insert(price: ProductPriceModel): F[ProductPriceId] =
    collection.insertOne(price.asEntity).map(_ => price.id)

  override def getLatestPriceOfProduct(productId: ProductId): F[Option[ProductPriceModel]] =
    collection
      .find(Filter.eq("productId", productId))
      .sortByDesc("_id")
      .first
      .map(_.map(_.asModel))
}

object PriceRepository {
  import com.mdagdelen.models.ProductPriceDerivation._
  final private val COLLECTION_NAME: String = "price"

  def make[F[_]: Async](db: MongoDatabase[F]): F[PriceRepository[F]] = {
    db.getCollectionWithCodec[ProductPriceEntity](COLLECTION_NAME).map(col => new PriceRepositoryImpl[F](col))
  }
}

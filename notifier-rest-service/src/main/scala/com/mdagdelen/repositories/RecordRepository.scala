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
import com.mdagdelen.models.{Record, RecordEntity}
import com.mdagdelen.types.Types.{Email, ProductId, RecordId}
import io.circe.generic.auto._
import mongo4cats.bson.ObjectId
import mongo4cats.circe._
import mongo4cats.collection.MongoCollection
import mongo4cats.database.MongoDatabase
import mongo4cats.operations.Filter

trait RecordRepository[F[_]] {
  def getById(id: ObjectId): F[Record]
  def insert(record: Record): F[RecordId]
  def productRecordByEmail(email: Email, productId: ProductId): F[Option[Record]]
}

final private class RecordRepositoryImpl[F[_]: Async](private val collection: MongoCollection[F, RecordEntity])
    extends RecordRepository[F] {
  override def getById(id: ObjectId): F[Record] =
    collection
      .find(Filter.eq("_id", id))
      .first
      .flatMap(mRecord => Async[F].fromOption(mRecord.map(_.asRecord), Exceptions.EntityDoesNotExist("record", id)))

  override def insert(record: Record): F[RecordId] = collection.insertOne(record.asRecordEntity).map(_ => record.id)

  override def productRecordByEmail(email: Email, productId: ProductId): F[Option[Record]] = {
    collection
      .find(
        Filter.eq("productId", productId) &&
          Filter.eq("email", email) &&
          Filter.gte("expiredAt", System.currentTimeMillis)
      )
      .first
      .map(_.map(_.asRecord))
  }
}

object RecordRepository {
  final private val COLLECTION_NAME: String = "record"
  final private type ENCODER_TYPE = RecordEntity

  def make[F[_]: Async](db: MongoDatabase[F]): F[RecordRepository[F]] = {
    db.getCollectionWithCodec[ENCODER_TYPE](COLLECTION_NAME).map(col => new RecordRepositoryImpl[F](col))
  }
}

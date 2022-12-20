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

package com.mdagdelen.services

import cats.effect.Sync
import cats.implicits._
import com.mdagdelen.models.{CreateRecordRequest, CreateRecordResponse, Email, Record}
import com.mdagdelen.repositories.{PriceRepository, ProductRepository, RecordRepository}
import mongo4cats.bson.ObjectId

trait RecordService[F[_]] {
  def getProductRecord(id: ObjectId): F[Record]
  def storeRecord(createRecordRequest: CreateRecordRequest): F[CreateRecordResponse]
}

object RecordService {
  def make[F[_]: Sync](
    productRepository: ProductRepository[F],
    recordRepository: RecordRepository[F],
    priceRepository: PriceRepository[F]
  ): RecordService[F] =
    new RecordService[F] {
      override def getProductRecord(id: ObjectId): F[Record] = recordRepository.getById(id)

      override def storeRecord(createRecordRequest: CreateRecordRequest): F[CreateRecordResponse] = for {
        maybeRecord <- recordRepository
          .productRecordByEmail(Email.from(createRecordRequest.email), createRecordRequest.productId)
        recordId <- maybeRecord match {
          case Some(value) =>
            Sync[F].pure(CreateRecordResponse(isNew = false, isVerified = value.isVerified, id = value.id))
          case None =>
            for {
              product <- productRepository.getById(ObjectId(createRecordRequest.productId))
              price   <- priceRepository.getLatestPriceOfProduct(product.id)
              recordId <- recordRepository.insert(
                createRecordRequest.asRecord(product.marketplace, price.sellingPrice)
              )
            } yield CreateRecordResponse(isNew = true, isVerified = false, id = recordId)
        }
      } yield recordId
    }
}

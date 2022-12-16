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

package com.mdagdelen.models

import com.mdagdelen.types.IdType
import com.mdagdelen.types.Types.{ProductId, RecordId}
import mongo4cats.bson.ObjectId

object RecordId extends IdType[RecordId]

case class Record(
  id: RecordId,
  marketplace: String,
  productId: String,
  priceAtQT: Float,
  createdAt: Long,
  expiredAt: Long
) {
  def asRecordEntity: RecordEntity =
    RecordEntity(ObjectId(id), marketplace, productId, priceAtQT, createdAt, expiredAt)
}

case class CreateRecordRequest(productId: ProductId, expireAt: Long) {
  def asRecord(marketplace: String, externalProductId: String, priceAtQT: Float): Record =
    Record(RecordId(ObjectId()), marketplace, externalProductId, priceAtQT, System.currentTimeMillis(), expireAt)
}

case class RecordEntity(
  _id: ObjectId,
  marketplace: String,
  productId: String,
  priceAtQT: Float,
  createdAt: Long,
  expiredAt: Long
) {
  def asRecord: Record =
    Record(RecordId(_id), marketplace, productId, priceAtQT, System.currentTimeMillis(), expiredAt)
}

case class CreateRecordResponse(id: RecordId)

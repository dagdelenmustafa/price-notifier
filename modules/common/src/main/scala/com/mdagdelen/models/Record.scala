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
import com.mdagdelen.types.Types.{Email, EmailRefined, ProductId, RecordId, VerificationId}
import mongo4cats.bson.ObjectId

object RecordId extends IdType[RecordId]

case class EmailVerification(isVerified: Boolean, verificationId: VerificationId, numberOfResend: Int = 0)

case class Record(
  id: RecordId,
  marketplace: String,
  productId: String,
  email: Email,
  verification: EmailVerification,
  priceAtQT: Float,
  createdAt: Long,
  expiredAt: Long
) {
  def asRecordEntity: RecordEntity =
    RecordEntity(
      _id = ObjectId(id),
      marketplace = marketplace,
      productId = productId,
      email = email,
      verification = verification,
      priceAtQT = priceAtQT,
      createdAt = createdAt,
      expiredAt = expiredAt
    )
}

case class CreateRecordRequest(productId: ProductId, expireAt: Long, email: EmailRefined) {
  def asRecord(marketplace: String, priceAtQT: Float, verificationId: VerificationId): Record =
    Record(
      id = RecordId(ObjectId()),
      marketplace = marketplace,
      productId = productId,
      email = Email.from(email),
      verification = EmailVerification(isVerified = false, verificationId),
      priceAtQT = priceAtQT,
      createdAt = System.currentTimeMillis(),
      expiredAt = expireAt
    )
}

case class RecordEntity(
  _id: ObjectId,
  marketplace: String,
  productId: String,
  email: Email,
  verification: EmailVerification,
  priceAtQT: Float,
  createdAt: Long,
  expiredAt: Long
) {
  def asRecord: Record =
    Record(
      id = RecordId(_id),
      marketplace = marketplace,
      productId = productId,
      email = email,
      verification = verification,
      priceAtQT = priceAtQT,
      createdAt = createdAt,
      expiredAt = expiredAt
    )
}

case class CreateRecordResponse(isNew: Boolean, isVerified: Boolean, id: RecordId)
case class ResendVerificationEmailRequest(id: RecordId)

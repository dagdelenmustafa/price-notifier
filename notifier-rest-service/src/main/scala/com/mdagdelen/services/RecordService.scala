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
import com.mdagdelen.exceptions.Exceptions
import com.mdagdelen.gateways.{RabbitMQGateway, RabbitQueues}
import com.mdagdelen.models._
import com.mdagdelen.repositories.{PriceRepository, ProductRepository, RecordRepository}
import com.mdagdelen.types.Types.{RecordId, VerificationId}
import mongo4cats.bson.ObjectId

import java.util.UUID

trait RecordService[F[_]] {
  def getProductRecord(id: ObjectId): F[Record]
  def storeRecord(createRecordRequest: CreateRecordRequest): F[CreateRecordResponse]
  def verifyEmail(verificationUUID: VerificationId): F[Unit]
  def resendVerificationEmail(recordId: RecordId): F[Unit]
}

object RecordService {
  def make[F[_]: Sync](
    productRepository: ProductRepository[F],
    recordRepository: RecordRepository[F],
    priceRepository: PriceRepository[F],
    rabbitMQGateway: RabbitMQGateway[F]
  ): RecordService[F] =
    new RecordService[F] {
      override def getProductRecord(id: ObjectId): F[Record] = recordRepository.getById(id)

      override def storeRecord(createRecordRequest: CreateRecordRequest): F[CreateRecordResponse] = for {
        maybeRecord <- recordRepository
          .productRecordByEmail(Email.from(createRecordRequest.email), createRecordRequest.productId)
        recordId <- maybeRecord match {
          case Some(value) =>
            Sync[F].pure(CreateRecordResponse(isNew = false, isVerified = value.verification.isVerified, id = value.id))
          case None =>
            for {
              product <- productRepository.getById(ObjectId(createRecordRequest.productId))
              price   <- priceRepository.getLatestPriceOfProduct(product.id)
              record = createRecordRequest.asRecord(product.marketplace, price.sellingPrice)
              _ <- recordRepository.insert(record)
              _ <- rabbitMQGateway.publish(
                RabbitQueues.VERIFICATION_QUEUE,
                VerificationQueueMessage(
                  UUID.randomUUID(),
                  Email.from(createRecordRequest.email),
                  record.verification.verificationId
                )
              )
            } yield CreateRecordResponse(isNew = true, isVerified = false, id = record.id)
        }
      } yield recordId

      override def verifyEmail(verificationUUID: UUID): F[Unit] = for {
        updated <- recordRepository.verifyRecordByVerificationId(verificationUUID)
        _       <- if (updated == 0) Sync[F].raiseError(Exceptions.AlreadyVerifiedException) else Sync[F].unit
      } yield ()

      override def resendVerificationEmail(recordId: RecordId): F[Unit] = for {
        record <- recordRepository.getById(ObjectId(recordId))
        _ <-
          if (record.verification.isVerified) Sync[F].raiseError(Exceptions.AlreadyVerifiedException) else Sync[F].unit
        _ <-
          if (record.verification.numberOfResend > 2) Sync[F].raiseError(Exceptions.TooManyResendRequestException)
          else Sync[F].unit
        _ <- rabbitMQGateway.publish(
          RabbitQueues.VERIFICATION_QUEUE,
          VerificationQueueMessage(
            UUID.randomUUID(),
            record.email,
            record.verification.verificationId
          )
        )
        _ <- recordRepository.incrementNumberOfEmailVerificationRequest(ObjectId(record.id))
      } yield ()
    }
}

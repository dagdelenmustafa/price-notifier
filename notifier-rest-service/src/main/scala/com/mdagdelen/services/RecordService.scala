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

import cats.data.EitherT
import cats.effect.Sync
import cats.implicits._
import com.mdagdelen.exceptions.{BaseError, Exceptions}
import com.mdagdelen.gateways.{RabbitMQGateway, RabbitQueues}
import com.mdagdelen.models._
import com.mdagdelen.repositories.{PriceRepository, ProductRepository, RecordRepository}
import com.mdagdelen.types.Types.{RecordId, VerificationId}
import mongo4cats.bson.ObjectId

import java.util.UUID

trait RecordService[F[_]] {
  def getProductRecord(id: ObjectId): F[Either[BaseError, Record]]
  def storeRecord(createRecordRequest: CreateRecordRequest): F[Either[BaseError, CreateRecordResponse]]
  def verifyEmail(verificationUUID: VerificationId): F[Either[BaseError, Unit]]
  def resendVerificationEmail(recordId: RecordId): F[Either[BaseError, Unit]]
}

object RecordService {
  def make[F[_]: Sync](
    productRepository: ProductRepository[F],
    recordRepository: RecordRepository[F],
    priceRepository: PriceRepository[F],
    rabbitMQGateway: RabbitMQGateway[F]
  ): RecordService[F] =
    new RecordService[F] {
      override def getProductRecord(id: ObjectId): F[Either[BaseError, Record]] =
        EitherT
          .fromOptionF[F, BaseError, Record](recordRepository.getById(id), Exceptions.EntityDoesNotExist("record", id))
          .value

      override def storeRecord(createRecordRequest: CreateRecordRequest): F[Either[BaseError, CreateRecordResponse]] =
        (for {
          maybeRecord <- EitherT.right[BaseError](
            recordRepository.productRecordByEmail(Email.from(createRecordRequest.email), createRecordRequest.productId)
          )
          recordId <- maybeRecord match {
            case Some(value) =>
              EitherT.rightT[F, BaseError](
                CreateRecordResponse(isNew = false, isVerified = value.verification.isVerified, id = value.id)
              )
            case None =>
              for {
                id <- EitherT.rightT[F, BaseError](ObjectId(createRecordRequest.productId))
                product <- EitherT.fromOptionF(
                  productRepository.getById(id),
                  Exceptions.EntityDoesNotExist("product", id)
                )
                price <- EitherT.fromOptionF(
                  priceRepository.getLatestPriceOfProduct(product.id),
                  Exceptions.PriceNotFound(product.id)
                )
                record = createRecordRequest.asRecord(product.marketplace, price.sellingPrice)
                _ <- EitherT.right[BaseError](recordRepository.insert(record))
                _ <- EitherT.right[BaseError](
                  rabbitMQGateway.publish(
                    RabbitQueues.VERIFICATION_QUEUE,
                    VerificationQueueMessage(
                      UUID.randomUUID(),
                      Email.from(createRecordRequest.email),
                      record.verification.verificationId
                    )
                  )
                )
              } yield CreateRecordResponse(isNew = true, isVerified = false, id = record.id)
          }
        } yield recordId).value

      override def verifyEmail(verificationUUID: UUID): F[Either[BaseError, Unit]] = (for {
        // TODO: update with findOneAndUpdate and check if there is an existing record with the given verification id.
        updated <- EitherT.liftF[F, BaseError, Long](recordRepository.verifyRecordByVerificationId(verificationUUID))
        _ <- EitherT.fromEither[F](
          Either.cond[BaseError, Unit](updated != 0, (), Exceptions.AlreadyVerifiedError)
        )
      } yield ()).value

      override def resendVerificationEmail(recordId: RecordId): F[Either[BaseError, Unit]] = (for {
        record <- EitherT.fromOptionF[F, BaseError, Record](
          recordRepository.getById(ObjectId(recordId)),
          Exceptions.EntityDoesNotExist("record", ObjectId(recordId))
        )
        _ <- EitherT.cond[F](!record.verification.isVerified, (), Exceptions.AlreadyVerifiedError)
        _ <- EitherT.cond[F](!(record.verification.numberOfResend > 2), (), Exceptions.TooManyResendRequestError)

        _ <- EitherT.right[BaseError](
          rabbitMQGateway.publish(
            RabbitQueues.VERIFICATION_QUEUE,
            VerificationQueueMessage(
              UUID.randomUUID(),
              record.email,
              record.verification.verificationId
            )
          )
        )
        _ <- EitherT.right[BaseError](
          recordRepository.incrementNumberOfEmailVerificationRequest(ObjectId(record.id))
        )
      } yield ()).value
    }
}

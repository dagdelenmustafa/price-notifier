package com.mdagdelen.services

import cats.effect.Sync
import cats.implicits._
import com.mdagdelen.models.{CreateRecordRequest, Record}
import com.mdagdelen.repositories.{ProductRepository, RecordRepository}
import com.mdagdelen.types.Types.RecordId
import mongo4cats.bson.ObjectId

trait RecordService[F[_]] {
  def getProductRecord(id: ObjectId): F[Record]
  def storeRecord(createRecordRequest: CreateRecordRequest): F[RecordId]
}

object RecordService {
  def make[F[_]: Sync](productService: ProductService[F],
                       productRepository: ProductRepository[F],
                       recordRepository: RecordRepository[F]
  ): RecordService[F] =
    new RecordService[F] {
      override def getProductRecord(id: ObjectId): F[Record] = recordRepository.getById(id)

      override def storeRecord(createRecordRequest: CreateRecordRequest): F[RecordId] = for {
        productInfo <- productService.productLookUp(createRecordRequest.url)
        product     <- Sync[F].pure(productInfo.asProduct)
        _           <- productRepository.insert(product)
        recordId <- recordRepository.insert(
          createRecordRequest.asRecord(product.marketplace, product.externalId, product.price.sellingPrice)
        )
      } yield recordId
    }
}

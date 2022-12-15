package com.mdagdelen.models

import com.mdagdelen.types.IdType
import com.mdagdelen.types.Types.RecordId
import mongo4cats.bson.ObjectId

object RecordId extends IdType[RecordId]

case class Record(id: RecordId,
                  marketplace: String,
                  productId: String,
                  priceAtQT: Float,
                  createdAt: Long,
                  expiredAt: Long
) {
  def asRecordEntity: RecordEntity =
    RecordEntity(ObjectId(id), marketplace, productId, priceAtQT, createdAt, expiredAt)
}

case class CreateRecordRequest(url: String, expireAt: Long) {
  def asRecord(marketplace: String, productId: String, priceAtQT: Float): Record =
    Record(RecordId(ObjectId()), marketplace, productId, priceAtQT, System.currentTimeMillis(), expireAt)
}

case class RecordEntity(_id: ObjectId,
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

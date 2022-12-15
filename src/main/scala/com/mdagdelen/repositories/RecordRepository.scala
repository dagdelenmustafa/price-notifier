package com.mdagdelen.repositories

import cats.effect.kernel.Async
import cats.implicits._
import com.mdagdelen.exceptions.Exceptions
import com.mdagdelen.models.{Record, RecordEntity}
import com.mdagdelen.types.Types.RecordId
import io.circe.generic.auto._
import mongo4cats.bson.ObjectId
import mongo4cats.circe._
import mongo4cats.collection.MongoCollection
import mongo4cats.database.MongoDatabase
import mongo4cats.operations.Filter

trait RecordRepository[F[_]] {
  def getById(id: ObjectId): F[Record]
  def insert(record: Record): F[RecordId]
}

final private class RecordRepositoryImpl[F[_]: Async](private val collection: MongoCollection[F, RecordEntity])
    extends RecordRepository[F] {
  override def getById(id: ObjectId): F[Record] =
    collection
      .find(Filter.eq("_id", id))
      .first
      .flatMap(mRecord => Async[F].fromOption(mRecord.map(_.asRecord), Exceptions.EntityDoesNotExist("record", id)))

  override def insert(record: Record): F[RecordId] = collection.insertOne(record.asRecordEntity).map(_ => record.id)
}

object RecordRepository {
  final private val COLLECTION_NAME: String = "record"
  final private type ENCODER_TYPE = RecordEntity

  def make[F[_]: Async](db: MongoDatabase[F]): F[RecordRepository[F]] = {
    db.getCollectionWithCodec[ENCODER_TYPE](COLLECTION_NAME).map(col => new RecordRepositoryImpl[F](col))
  }
}

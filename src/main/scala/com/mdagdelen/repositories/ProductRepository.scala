package com.mdagdelen.repositories

import cats.effect.kernel.Async
import cats.implicits._
import com.mdagdelen.exceptions.Exceptions
import com.mdagdelen.models.{Product, ProductEntity}
import com.mdagdelen.types.Types.ProductId
import io.circe.generic.auto._
import mongo4cats.bson.ObjectId
import mongo4cats.circe._
import mongo4cats.collection.MongoCollection
import mongo4cats.database.MongoDatabase
import mongo4cats.operations.Filter

trait ProductRepository[F[_]] {
  def getById(id: ObjectId): F[Product]
  def insert(product: Product): F[ProductId]
}

final private class ProductRepositoryImpl[F[_]: Async](private val collection: MongoCollection[F, ProductEntity])
    extends ProductRepository[F] {
  override def getById(id: ObjectId): F[Product] =
    collection
      .find(Filter.eq("_id", id))
      .first
      .flatMap(mProduct => Async[F].fromOption(mProduct.map(_.asProduct), Exceptions.EntityDoesNotExist("product", id)))

  override def insert(product: Product): F[ProductId] =
    collection.insertOne(product.asProductEntity).map(_ => product.id)
}

object ProductRepository {
  final private val COLLECTION_NAME: String = "product"

  def make[F[_]: Async](db: MongoDatabase[F]): F[ProductRepository[F]] = {
    import com.mdagdelen.models.ProductPriceDerivation._
    db.getCollectionWithCodec[ProductEntity](COLLECTION_NAME).map(col => new ProductRepositoryImpl[F](col))
  }
}

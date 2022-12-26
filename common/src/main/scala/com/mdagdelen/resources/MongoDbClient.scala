package com.mdagdelen.resources

import cats.effect.{Async, Resource}
import com.mdagdelen.MongoConfig
import mongo4cats.client.MongoClient
import mongo4cats.database.MongoDatabase

object MongoDbClient {
  def make[F[_]: Async](c: MongoConfig): Resource[F, MongoDatabase[F]] =
    MongoClient.fromConnectionString[F](c.connectionUrl).evalMap(_.getDatabase(c.database))
}

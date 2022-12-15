package com.mdagdelen

import cats.effect.Resource
import cats.effect.kernel.Async
import cats.implicits.catsSyntaxTuple2Semigroupal
import mongo4cats.client.MongoClient
import mongo4cats.database.MongoDatabase
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder

sealed abstract class AppResources[F[_]](val client: Client[F], val mongo: MongoDatabase[F])

object AppResources {

  def make[F[_]: Async](cfg: Config): Resource[F, AppResources[F]] = {

    def mongoResource(c: MongoConfig): Resource[F, MongoDatabase[F]] =
      MongoClient.fromConnectionString[F](c.connectionUrl).evalMap(_.getDatabase(c.database))

    (EmberClientBuilder.default.build, mongoResource(cfg.mongo)).mapN(new AppResources[F](_, _) {})
  }
}

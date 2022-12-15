package com.mdagdelen

import cats.effect.kernel.Async

case class MongoConfig(connectionUrl: String, database: String)

sealed abstract class Config {
  val mongo: MongoConfig
}

object Config {
  def load[F[_]: Async]: F[Config] = Async[F].pure {
    new Config {
      override val mongo: MongoConfig = MongoConfig("mongodb://localhost:27017", "notifier")
    }
  }
}

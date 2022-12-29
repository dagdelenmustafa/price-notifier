package com.mdagdelen

import cats.effect.{IO, IOApp}
import com.mdagdelen.repositories.{ProductRepository, RecordRepository}
import com.mdagdelen.resources.MongoDbClient
import fs2._
import mongo4cats.bson.ObjectId

import scala.concurrent.duration.DurationInt

object PriceCheckerApp extends IOApp.Simple {
  def program(recordRepository: RecordRepository[IO], productRepository: ProductRepository[IO]): Stream[IO, Unit] =
    recordRepository.getAllValidRecords.evalMap(r =>
      for {
        productM <- productRepository.getById(ObjectId(r.productId))
        // TODO: Read marketplace data and compare
      } yield println(productM)
    )

  override def run: IO[Unit] = for {
    config <- Config.load[IO]
    _ <- MongoDbClient.make[IO](config.mongo).use { client =>
      (for {
        recordRepo <- Stream.eval(RecordRepository.make[IO](client))
        priceRepo  <- Stream.eval(ProductRepository.make[IO](client))
        _          <- Stream.awakeDelay[IO](15.seconds).flatMap(_ => program(recordRepo, priceRepo))
      } yield ()).compile.drain
    }
  } yield ()
}

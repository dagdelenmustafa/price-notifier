package com.mdagdelen

import cats.data.EitherT
import cats.effect.{IO, IOApp}
import com.mdagdelen.exceptions.BaseError
import com.mdagdelen.exceptions.Exceptions.{ProductNotFound, UnsupportedMarketplace}
import com.mdagdelen.models.marketplaces.{Marketplace, MarketplaceBuilder}
import com.mdagdelen.models.Product
import com.mdagdelen.repositories.{ProductRepository, RecordRepository}
import com.mdagdelen.resources.MongoDbClient
import fs2._
import mongo4cats.bson.ObjectId
import org.http4s.ember.client.EmberClientBuilder

import scala.concurrent.duration.DurationInt

object PriceCheckerApp extends IOApp.Simple {
  def program(recordRepository: RecordRepository[IO],
              productRepository: ProductRepository[IO],
              marketplaceBuilder: MarketplaceBuilder[IO]
  ): Stream[IO, Unit] =
    recordRepository.getAllValidRecords.evalMap(r =>
      (for {
        product <- EitherT.fromOptionF[IO, BaseError, Product](
          productRepository.getById(ObjectId(r.productId)),
          ProductNotFound
        )
        marketplace <- EitherT.fromOptionF[IO, BaseError, Marketplace[IO]](
          marketplaceBuilder.fromName(product.marketplace),
          UnsupportedMarketplace(product.marketplace)
        )
        priceModel <- EitherT.right[BaseError](
          marketplace.productInfoFromId(product.externalId).map(_.asPrice(product.id))
        )
        diff = r.priceAtQT - priceModel.sellingPrice
        // TODO: Handle error cases
      } yield println(diff)).value.as(())
    )

  override def run: IO[Unit] = for {
    config <- Config.load[IO]
    _ <- MongoDbClient.make[IO](config.mongo).use { mongoClient =>
      EmberClientBuilder.default[IO].build.use { client =>
        (for {
          recordRepo         <- Stream.eval(RecordRepository.make[IO](mongoClient))
          priceRepo          <- Stream.eval(ProductRepository.make[IO](mongoClient))
          marketplaceBuilder <- Stream.eval(IO(MarketplaceBuilder.make[IO](client)))
          _ <- Stream.awakeDelay[IO](15.seconds).flatMap(_ => program(recordRepo, priceRepo, marketplaceBuilder))
        } yield ()).compile.drain
      }
    }
  } yield ()
}

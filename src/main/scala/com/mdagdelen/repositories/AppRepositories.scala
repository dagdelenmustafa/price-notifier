package com.mdagdelen.repositories

import cats.effect.kernel.Async
import cats.implicits._
import com.mdagdelen.AppResources

sealed abstract class AppRepositories[F[_]](
  val recordRepository: RecordRepository[F],
  val productRepository: ProductRepository[F]
)

object AppRepositories {
  def make[F[_]: Async](appResources: AppResources[F]): F[AppRepositories[F]] =
    (
      RecordRepository.make[F](appResources.mongo),
      ProductRepository.make[F](appResources.mongo)
    ).mapN(new AppRepositories[F](_, _) {})
}

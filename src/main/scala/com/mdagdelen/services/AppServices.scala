package com.mdagdelen.services

import cats.effect.Async
import com.mdagdelen.AppResources
import com.mdagdelen.repositories.AppRepositories

sealed trait AppServices[F[_]] {
  val recordService: RecordService[F]
  val marketplaceService: MarketplaceService[F]
  val productService: ProductService[F]
}

object AppServices {
  def make[F[_]: Async](appRepositories: AppRepositories[F], appResources: AppResources[F]): AppServices[F] =
    new AppServices[F] {
      override val marketplaceService: MarketplaceService[F] =
        MarketplaceService.make[F](appResources.client)
      override val productService: ProductService[F] =
        ProductService.make[F](marketplaceService)
      override val recordService: RecordService[F] =
        RecordService.make[F](productService, appRepositories.productRepository, appRepositories.recordRepository)
    }
}

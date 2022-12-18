/*
 * Copyright 2022 com.dagdelenmustafa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mdagdelen.services

import cats.effect.Async
import com.mdagdelen.AppResources
import com.mdagdelen.models.marketplaces.MarketplaceBuilder
import com.mdagdelen.repositories.AppRepositories

sealed trait AppServices[F[_]] {
  val recordService: RecordService[F]
  val marketplaceBuilder: MarketplaceBuilder[F]
  val productService: ProductService[F]
}

object AppServices {
  def make[F[_]: Async](appRepositories: AppRepositories[F], appResources: AppResources[F]): AppServices[F] =
    new AppServices[F] {
      override val marketplaceBuilder: MarketplaceBuilder[F] =
        MarketplaceBuilder.make[F](appResources.client)
      override val productService: ProductService[F] =
        ProductService.make[F](marketplaceBuilder, appRepositories.productRepository, appRepositories.priceRepository)
      override val recordService: RecordService[F] =
        RecordService
          .make[F](appRepositories.productRepository, appRepositories.recordRepository, appRepositories.priceRepository)
    }
}

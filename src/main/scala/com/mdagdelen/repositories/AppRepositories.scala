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

package com.mdagdelen.repositories

import cats.Parallel
import cats.effect.Async
import cats.implicits._
import com.mdagdelen.AppResources

sealed abstract class AppRepositories[F[_]](
  val recordRepository: RecordRepository[F],
  val productRepository: ProductRepository[F],
  val priceRepository: PriceRepository[F]
)

object AppRepositories {
  def make[F[_]: Async: Parallel](appResources: AppResources[F]): F[AppRepositories[F]] =
    (
      RecordRepository.make[F](appResources.mongo),
      ProductRepository.make[F](appResources.mongo),
      PriceRepository.make[F](appResources.mongo)
    ).parMapN(new AppRepositories[F](_, _, _) {})
}

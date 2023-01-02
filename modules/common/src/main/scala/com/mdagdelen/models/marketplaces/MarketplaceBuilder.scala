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

package com.mdagdelen.models.marketplaces

import cats.effect.Async
import com.mdagdelen.types.Types.Hostname
import org.http4s.client.Client

trait MarketplaceBuilder[F[_]] {
  def fromHostname(hostname: Hostname): F[Option[Marketplace[F]]]
  def fromName(name: String): F[Option[Marketplace[F]]]
}

object MarketplaceBuilder {
  def make[F[_]: Async](client: Client[F]): MarketplaceBuilder[F] = new MarketplaceBuilder[F] {
    val marketPlaces = List(TrendyolMarketplace.make[F](client), HepsiburadaMarketplace.make[F]())

    override def fromHostname(hostname: Hostname): F[Option[Marketplace[F]]] =
      Async[F].pure(marketPlaces.find(_.hostname == hostname))

    override def fromName(name: String): F[Option[Marketplace[F]]] =
      Async[F].pure(marketPlaces.find(_.name == name))
  }
}

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

import cats.data.EitherT
import cats.effect.Sync
import cats.implicits._
import com.google.common.net.InternetDomainName
import com.mdagdelen.exceptions.Exceptions.UnsupportedMarketplace
import com.mdagdelen.exceptions.{BaseError, Exceptions}
import com.mdagdelen.models.marketplaces.{Marketplace, MarketplaceBuilder}
import com.mdagdelen.models.{Product, ProductId}
import com.mdagdelen.repositories.{PriceRepository, ProductRepository}
import com.mdagdelen.types.Types.{Hostname, Path}
import mongo4cats.bson.ObjectId

import java.net.URL

trait ProductService[F[_]] {
  def productLookUp(url: String): F[Either[BaseError, Product]]
}

object ProductService {
  def make[F[_]: Sync](
    marketplaceBuilder: MarketplaceBuilder[F],
    productRepository: ProductRepository[F],
    priceRepository: PriceRepository[F]
  ): ProductService[F] = new ProductService[F] {
    override def productLookUp(url: String): F[Either[BaseError, Product]] = (for {
      (hostname, path) <- extractHostname(url)
      marketplace <- EitherT.fromOptionF[F, BaseError, Marketplace[F]](
        marketplaceBuilder.fromHostname(hostname),
        UnsupportedMarketplace(hostname)
      )
      productInfo <- EitherT.right[BaseError](marketplace.productInfo(path))
      productId = ProductId(ObjectId())
      product   = productInfo.asProduct(productId)
      productIdE <- EitherT.right[BaseError](productRepository.insertIfNotExist(product))
      _          <- EitherT.right[BaseError](priceRepository.insert(productInfo.asPrice(productIdE)))
    } yield product.copy(id = productIdE)).value

    private def extractHostname(urlString: String): EitherT[F, BaseError, (Hostname, Path)] = EitherT(
      (for {
        url <- Sync[F].delay {
          if (List("http://", "https://").exists(urlString.startsWith)) new URL(urlString)
          else new URL(s"https://$urlString")
        }
        hostname = InternetDomainName.from(url.getHost).topPrivateDomain().toString
        path     = s"${url.getPath}?${url.getQuery}"
      } yield (hostname, path).asRight[BaseError]).handleError(_ =>
        Exceptions.MalformedUrlError.asLeft[(Hostname, Path)]
      )
    )
  }
}

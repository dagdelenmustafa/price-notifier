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

import cats.effect.Sync
import com.mdagdelen.models.{HepsiburadaPriceModel, Product, ProductId, ProductPriceId, ProductPriceModel}
import com.mdagdelen.types.Types.{Hostname, ProductId}
import mongo4cats.bson.ObjectId

case class HepsiburadaApiProductResponse(val id: String) extends MarketplaceProductResponse {
  override def asProduct(productId: ProductId = ProductId(ObjectId())): Product =
    Product(
      productId,
      id,
      "product",
      None,
      Some(List("")),
      List("image1", "image2"),
      "",
      None,
      "hepsiburada"
    )

  override def asPrice(productId: ProductId): ProductPriceModel =
    HepsiburadaPriceModel(
      ProductPriceId(ObjectId()),
      ProductId(ObjectId()),
      123.12.toFloat,
      123.12.toFloat,
      System.currentTimeMillis()
    )
}

object HepsiburadaMarketplace {
  def make[F[_]: Sync](): Marketplace[F] = new Marketplace[F] {
    override val hostname: Hostname = "hepsiburada.com"
    override val name: String       = "hepsiburada"

    override def productInfo(path: String): F[_ <: MarketplaceProductResponse] =
      Sync[F].pure(HepsiburadaApiProductResponse("123"))

    override def productInfoFromId(id: String): F[_ <: MarketplaceProductResponse] =
      Sync[F].pure(HepsiburadaApiProductResponse("123"))
  }
}

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

import com.mdagdelen.models.{Product, ProductId, ProductPriceModel}
import com.mdagdelen.types.Types.{Hostname, Path, ProductId}
import mongo4cats.bson.ObjectId

trait MarketplaceProductResponse {
  def asProduct(productId: ProductId = ProductId(ObjectId())): Product
  def asPrice(productId: ProductId): ProductPriceModel
}

trait Marketplace[F[_]] {
  val hostname: Hostname
  val name: String
  def productInfo(path: Path): F[_ <: MarketplaceProductResponse]
  def productInfoFromId(id: String): F[_ <: MarketplaceProductResponse]
}

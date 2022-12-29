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
import cats.implicits._
import com.mdagdelen.exceptions.Exceptions.ProductNotFound
import com.mdagdelen.models.{Product, ProductId, ProductPriceId, ProductPriceModel, TrendyolPriceModel}
import com.mdagdelen.types.Types.{Hostname, ProductId}
import io.circe.generic.auto._
import mongo4cats.bson.ObjectId
import org.http4s.EntityDecoder
import org.http4s.circe._
import org.http4s.client.Client

case class TrendyolPublicApiResponse(result: TrendyolPublicApiResponseResult) extends MarketplaceProductResponse {
  override def asProduct(productId: ProductId = ProductId(ObjectId())): Product = Product(
    productId,
    result.id.toString,
    result.name,
    result.brand.map(_.name),
    result.contentDescriptions.map(_.map(_.description)),
    result.images.map(i => s"https://cdn.dsmcdn.com/$i"),
    result.category.name,
    result.color,
    "trendyol"
  )

  override def asPrice(productId: ProductId): ProductPriceModel =
    TrendyolPriceModel(
      ProductPriceId(ObjectId()),
      productId,
      result.price.sellingPrice.value,
      result.price.discountedPrice.value,
      System.currentTimeMillis()
    )
}
case class TrendyolPublicApiContentDescriptionsResponse(description: String)
case class TrendyolPublicApiPriceResponseStructure(text: String, value: Float)
case class TrendyolPublicApiPriceResponse(
  discountedPrice: TrendyolPublicApiPriceResponseStructure,
  sellingPrice: TrendyolPublicApiPriceResponseStructure
)
case class TrendyolPublicApiBrandResponse(name: String)
case class TrendyolPublicApiCategoryResponse(name: String, hierarchy: String)
case class TrendyolPublicApiResponseResult(
  id: Long,
  brand: Option[TrendyolPublicApiBrandResponse],
  category: TrendyolPublicApiCategoryResponse,
  contentDescriptions: Option[List[TrendyolPublicApiContentDescriptionsResponse]],
  images: List[String],
  price: TrendyolPublicApiPriceResponse,
  color: Option[String],
  name: String
)

object TrendyolMarketplace {
  def make[F[_]: Async](client: Client[F]): Marketplace[F] = new Marketplace[F] {
    implicit val trendyolEntityDecoder: EntityDecoder[F, TrendyolPublicApiResponse] = jsonOf
    override val hostname: Hostname                                                 = "trendyol.com"
    override val name: String                                                       = "trendyol"

    override def productInfo(path: String): F[_ <: MarketplaceProductResponse] = {
      val productPublicApiUrl = s"https://public.trendyol.com/discovery-web-productgw-service/api/productDetail/"

      (
        for {
          productId <- Async[F].delay(path.split("-p-")(1))
          res       <- client.expect[TrendyolPublicApiResponse](s"$productPublicApiUrl$productId")
        } yield res
      ).onError { case _ =>
        Async[F].raiseError(ProductNotFound)
      }
    }
  }
}

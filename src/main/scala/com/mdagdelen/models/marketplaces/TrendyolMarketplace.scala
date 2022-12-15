package com.mdagdelen.models.marketplaces

import cats.implicits._
import cats.effect.Async
import com.mdagdelen.exceptions.Exceptions.ProductNotFound
import com.mdagdelen.models.{Product, ProductId, ProductPrice}
import com.mdagdelen.types.Types.Hostname
import org.http4s.client.Client
import io.circe.generic.auto._
import mongo4cats.bson.ObjectId
import org.http4s.EntityDecoder
import org.http4s.circe._

case class TrendyolPriceModel(sellingPrice: Float, discountedPrice: Float) extends ProductPrice
case class TrendyolPublicApiResponse(result: TrendyolPublicApiResponseResult) extends MarketplaceProductResponse {
  override def asProduct: Product = Product(
    ProductId(ObjectId()),
    result.id.toString,
    result.name,
    result.brand.map(_.name),
    result.contentDescriptions.map(_.map(_.description)),
    TrendyolPriceModel(result.price.sellingPrice.value, result.price.discountedPrice.value),
    result.images.map(i => s"https://cdn.dsmcdn.com/$i"),
    result.category.name,
    result.color,
    "trendyol"
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
        Async[F].raiseError(ProductNotFound(path))
      }
    }
  }
}

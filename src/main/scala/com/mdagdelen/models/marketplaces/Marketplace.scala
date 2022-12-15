package com.mdagdelen.models.marketplaces

import com.mdagdelen.models.Product
import com.mdagdelen.types.Types.{Hostname, Path}
import io.circe.generic.auto._
import cats.syntax.functor._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}

trait MarketplaceProductResponse {
  def asProduct: Product
}

object MarketplaceProductResponseDerivation {
  implicit val encodeMarketplaceProductResponse: Encoder[MarketplaceProductResponse] = Encoder.instance {
    case hepsiburada @ HepsiburadaApiProductResponse(_) => hepsiburada.asJson
    case trendyol @ TrendyolPublicApiResponse(_)        => trendyol.asJson
    case _                                              => "".asJson
  }

  implicit val decodeMarketplaceProductResponse: Decoder[MarketplaceProductResponse] =
    List[Decoder[MarketplaceProductResponse]](
      Decoder[HepsiburadaApiProductResponse].widen,
      Decoder[TrendyolPublicApiResponse].widen
    ).reduceLeft(_ or _)
}

trait Marketplace[F[_]] {
  val hostname: Hostname
  val name: String
  def productInfo(path: Path): F[_ <: MarketplaceProductResponse]
}

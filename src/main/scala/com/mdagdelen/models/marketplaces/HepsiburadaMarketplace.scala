package com.mdagdelen.models.marketplaces

import cats.effect.Sync
import com.mdagdelen.models.{Product, ProductId, ProductPrice}
import com.mdagdelen.types.Types.Hostname
import mongo4cats.bson.ObjectId

case class HepsiburadaPriceModel(sellingPrice: Float, discountedPrice: Float) extends ProductPrice

case class HepsiburadaApiProductResponse(val id: String) extends MarketplaceProductResponse {
  override def asProduct: Product =
    Product(
      ProductId(ObjectId()),
      id,
      "product",
      None,
      Some(List("")),
      HepsiburadaPriceModel(123.12.toFloat, 123.12.toFloat),
      List("image1", "image2"),
      "",
      None,
      "hepsiburada"
    )
}

object HepsiburadaMarketplace {
  def make[F[_]: Sync](): Marketplace[F] = new Marketplace[F] {
    override val hostname: Hostname = "hepsiburada.com"
    override val name: String       = "hepsiburada"

    override def productInfo(path: String): F[_ <: MarketplaceProductResponse] =
      Sync[F].pure(HepsiburadaApiProductResponse("123"))
  }
}

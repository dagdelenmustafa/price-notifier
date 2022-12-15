package com.mdagdelen.models

import com.mdagdelen.models.marketplaces.{HepsiburadaPriceModel, TrendyolPriceModel}
import com.mdagdelen.types.IdType
import com.mdagdelen.types.Types.ProductId
import io.circe.{Decoder, Encoder}
import io.circe.syntax._
import mongo4cats.bson.ObjectId
import io.circe.generic.auto._
import cats.syntax.functor._

trait ProductPrice {
  val sellingPrice: Float
}

object ProductPriceDerivation {
  implicit val encodeProductPrice: Encoder[ProductPrice] = Encoder.instance {
    case hepsiburada @ HepsiburadaPriceModel(_, _) => hepsiburada.asJson
    case trendyol @ TrendyolPriceModel(_, _)       => trendyol.asJson
    case _                                         => "".asJson
  }

  implicit val decodeProductPrice: Decoder[ProductPrice] =
    List[Decoder[ProductPrice]](
      Decoder[HepsiburadaPriceModel].widen,
      Decoder[TrendyolPriceModel].widen
    ).reduceLeft(_ or _)
}

case class ProductLookupRequest(url: String)

object ProductId extends IdType[ProductId]

case class Product(
  id: ProductId,
  externalId: String,
  name: String,
  brand: Option[String],
  description: Option[List[String]],
  price: ProductPrice,
  images: List[String],
  category: String,
  color: Option[String],
  marketplace: String
) {
  def asProductEntity: ProductEntity =
    ProductEntity(ObjectId(id), externalId, name, brand, description, price, images, category, color, marketplace)
}

case class ProductEntity(
  _id: ObjectId,
  externalId: String,
  name: String,
  brand: Option[String],
  description: Option[List[String]],
  price: ProductPrice,
  images: List[String],
  category: String,
  color: Option[String],
  marketplace: String
) {
  def asProduct: Product =
    Product(ProductId(_id), externalId, name, brand, description, price, images, category, color, marketplace)
}

package com.mdagdelen.models

import cats.syntax.functor._
import com.mdagdelen.types.Types.{ProductId, ProductPriceId}
import com.mdagdelen.types.IdType
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import mongo4cats.bson.ObjectId
import mongo4cats.circe._

sealed trait ProductPriceEntity {
  val _id: ObjectId
  val sellingPrice: Float
  val productId: ProductId
  val date: Long
  def asModel: ProductPriceModel
}

sealed trait ProductPriceModel {
  val id: ProductPriceId
  val sellingPrice: Float
  val productId: ProductId
  val date: Long
  def asEntity: ProductPriceEntity
}

object ProductPriceDerivation {
  implicit val encodeProductPrice: Encoder[ProductPriceEntity] = Encoder.instance {
    case hepsiburada @ HepsiburadaPriceModelEntity(_, _, _, _, _) => hepsiburada.asJson
    case trendyol @ TrendyolPriceModelEntity(_, _, _, _, _)       => trendyol.asJson
    case _                                                        => "".asJson
  }

  implicit val decodeProductPrice: Decoder[ProductPriceEntity] =
    List[Decoder[ProductPriceEntity]](
      Decoder[HepsiburadaPriceModelEntity].widen,
      Decoder[TrendyolPriceModelEntity].widen
    ).reduceLeft(_ or _)
}

object ProductPriceId extends IdType[ProductPriceId]

case class TrendyolPriceModelEntity(
  _id: ObjectId,
  productId: ProductId,
  sellingPrice: Float,
  discountedPrice: Float,
  date: Long
) extends ProductPriceEntity {
  override def asModel: TrendyolPriceModel =
    TrendyolPriceModel(ProductPriceId(_id), productId, sellingPrice, discountedPrice, date)
}

case class TrendyolPriceModel(
  id: ProductPriceId,
  productId: ProductId,
  sellingPrice: Float,
  discountedPrice: Float,
  date: Long
) extends ProductPriceModel {
  override def asEntity: TrendyolPriceModelEntity =
    TrendyolPriceModelEntity(ObjectId(id), productId, sellingPrice, discountedPrice, date)
}

case class HepsiburadaPriceModelEntity(
  _id: ObjectId,
  productId: ProductId,
  sellingPrice: Float,
  discountedPrice: Float,
  date: Long
) extends ProductPriceEntity {
  override def asModel: HepsiburadaPriceModel =
    HepsiburadaPriceModel(ProductPriceId(_id), productId, sellingPrice, discountedPrice, date)
}

case class HepsiburadaPriceModel(
  id: ProductPriceId,
  productId: ProductId,
  sellingPrice: Float,
  discountedPrice: Float,
  date: Long
) extends ProductPriceModel {
  override def asEntity: HepsiburadaPriceModelEntity =
    HepsiburadaPriceModelEntity(ObjectId(id), productId, sellingPrice, discountedPrice, date)
}

package com.mdagdelen.types

import io.circe.{Decoder, Encoder}
import mongo4cats.bson.ObjectId

trait IdType[Id] {
  def apply(id: String): Id   = id.asInstanceOf[Id]
  def apply(id: ObjectId): Id = apply(id.toHexString)

  implicit val encoder: Encoder[Id] = Encoder[String].contramap(_.asInstanceOf[String])
  implicit val decoder: Decoder[Id] = Decoder[String].map(apply)
}

object Types {
  type RecordId  = String
  type Hostname  = String
  type Path      = String
  type ProductId = String
}

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

package com.mdagdelen.types

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.MatchesRegex
import io.circe.{Decoder, Encoder}
import mongo4cats.bson.ObjectId

import java.util.UUID

trait IdType[Id] {
  def apply(id: String): Id   = id.asInstanceOf[Id]
  def apply(id: ObjectId): Id = apply(id.toHexString)

  implicit val encoder: Encoder[Id] = Encoder[String].contramap(_.asInstanceOf[String])
  implicit val decoder: Decoder[Id] = Decoder[String].map(apply)
}

trait StringType[Str] {
  def apply(id: String): Str = id.asInstanceOf[Str]

  implicit val encoder: Encoder[Str] = Encoder[String].contramap(_.asInstanceOf[String])
  implicit val decoder: Decoder[Str] = Decoder[String].map(apply)
}

object Types {
  type RecordId       = String
  type Hostname       = String
  type Path           = String
  type ProductId      = String
  type ProductPriceId = String
  type Email          = String
  type VerificationId = UUID
  type EmailRefined   = String Refined MatchesRegex["^[a-zA-Z0-9.]+@[a-zA-Z0-9]+\\.[a-zA-Z]+$"]
}

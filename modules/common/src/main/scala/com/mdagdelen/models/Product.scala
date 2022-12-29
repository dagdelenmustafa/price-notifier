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

package com.mdagdelen.models

import com.mdagdelen.types.Types.ProductId
import com.mdagdelen.types.IdType
import mongo4cats.bson.ObjectId

case class ProductLookupRequest(url: String)

object ProductId extends IdType[ProductId]

case class Product(
  id: ProductId,
  externalId: String,
  name: String,
  brand: Option[String],
  description: Option[List[String]],
  images: List[String],
  category: String,
  color: Option[String],
  marketplace: String
) {
  def asProductEntity: ProductEntity =
    ProductEntity(ObjectId(id), externalId, name, brand, description, images, category, color, marketplace)
}

case class ProductEntity(
  _id: ObjectId,
  externalId: String,
  name: String,
  brand: Option[String],
  description: Option[List[String]],
  images: List[String],
  category: String,
  color: Option[String],
  marketplace: String
) {
  def asProduct: Product =
    Product(ProductId(_id), externalId, name, brand, description, images, category, color, marketplace)
}

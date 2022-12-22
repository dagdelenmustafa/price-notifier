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

package com.mdagdelen.exceptions

import mongo4cats.bson.ObjectId
import com.mdagdelen.types.Types.ProductId

import java.util.UUID

sealed trait BaseException extends Throwable {
  def message: String

  override def getMessage: String = message
}

object Exceptions {
  final case class EntityDoesNotExist(entityName: String, id: ObjectId) extends BaseException {
    override val message: String = s"$entityName with id $id does not exist"
  }

  final case class UnsupportedMarketplace(marketplaceHost: String) extends BaseException {
    override val message: String = s"Unsupported marketplace $marketplaceHost"
  }

  final case class ProductNotFound() extends BaseException {
    override val message: String = s"Couldn't find the product"
  }

  final case class PriceNotFound(productId: ProductId) extends BaseException {
    override val message: String = s"Couldn't find the price with productId: $productId"
  }

  final case class VerificationNotFound(verificationUUID: UUID) extends BaseException {
    override val message: String = s"Couldn't find the verification with id: $verificationUUID"
  }

  final case object AlreadyVerifiedException extends BaseException {
    override val message: String = s"verification has been already verified"
  }

  final case object TooManyResendRequestException extends BaseException {
    override val message: String = s"Too many email verification request"
  }
}

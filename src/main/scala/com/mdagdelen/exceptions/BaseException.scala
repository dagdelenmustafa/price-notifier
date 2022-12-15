package com.mdagdelen.exceptions

import mongo4cats.bson.ObjectId

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

  final case class ProductNotFound(path: String) extends BaseException {
    override val message: String = s"Couldn't find the product in this path: $path"
  }
}

package com.mdagdelen.models

import com.mdagdelen.types.Types.Email
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import cats.syntax.functor._

import java.util.UUID

sealed trait QueueMessage

case class VerificationQueueMessage(id: UUID, email: Email, verificationId: String) extends QueueMessage
case class NotificationQueueMessage(id: UUID, email: Email, message: String)        extends QueueMessage

object QueueMessageDerivation {
  implicit val encodeQueueMessage: Encoder[QueueMessage] = Encoder.instance {
    case verification @ VerificationQueueMessage(_, _, _) => verification.asJson
    case notification @ NotificationQueueMessage(_, _, _) => notification.asJson
    case _                                                => "".asJson
  }

  implicit val decodeQueueMessage: Decoder[QueueMessage] =
    List[Decoder[QueueMessage]](
      Decoder[VerificationQueueMessage].widen,
      Decoder[NotificationQueueMessage].widen
    ).reduceLeft(_ or _)
}

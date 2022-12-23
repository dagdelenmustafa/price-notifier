package com.mdagdelen.models

import cats.syntax.functor._
import com.mdagdelen.types.Types.{Email, VerificationId}
import io.chrisdavenport.fuuid.FUUID
import io.chrisdavenport.fuuid.circe._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._

sealed trait QueueMessage

case class VerificationQueueMessage(id: FUUID, email: Email, verificationId: VerificationId) extends QueueMessage
case class NotificationQueueMessage(id: FUUID, email: Email, message: String)                extends QueueMessage

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

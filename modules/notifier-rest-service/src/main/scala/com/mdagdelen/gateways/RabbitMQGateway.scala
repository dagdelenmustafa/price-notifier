package com.mdagdelen.gateways

import cats.data.Kleisli
import cats.effect.Sync
import cats.implicits._
import com.mdagdelen.RabbitMQConfig
import com.mdagdelen.models.QueueMessage
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.json.Fs2JsonEncoder
import dev.profunktor.fs2rabbit.model.{AmqpMessage, AmqpProperties, ExchangeName, RoutingKey}

import java.nio.charset.StandardCharsets

object RabbitQueues extends Enumeration {
  val NOTIFICATION_QUEUE, VERIFICATION_QUEUE = Value
}

trait RabbitMQGateway[F[_]] {
  def publish(queue: RabbitQueues.Value, data: QueueMessage): F[Unit]
}

object RabbitMQGateway {
  def make[F[_]: Sync](cfg: RabbitMQConfig, client: RabbitClient[F]): RabbitMQGateway[F] = new RabbitMQGateway[F] {
    object ioEncoder extends Fs2JsonEncoder

    val exchangeName: ExchangeName = ExchangeName(cfg.exchangeName)
    implicit val stringMessageEncoder: Kleisli[F, AmqpMessage[String], AmqpMessage[Array[Byte]]] =
      Kleisli[F, AmqpMessage[String], AmqpMessage[Array[Byte]]](s =>
        Sync[F].pure(s.copy(payload = s.payload.getBytes(StandardCharsets.UTF_8)))
      )

    override def publish(queue: RabbitQueues.Value, data: QueueMessage): F[Unit] = {
      client.createConnectionChannel.use { implicit channel =>
        for {
          publisher <- client.createPublisher[AmqpMessage[String]](exchangeName, getQueueRoutingKey(queue))
          _         <- publishMessage(data, publisher)
        } yield ()
      }
    }

    private def getQueueRoutingKey(queue: RabbitQueues.Value): RoutingKey = queue match {
      case RabbitQueues.NOTIFICATION_QUEUE => RoutingKey(cfg.notificationRoutingKey)
      case RabbitQueues.VERIFICATION_QUEUE => RoutingKey(cfg.verificationRoutingKey)
    }

    private def publishMessage(data: QueueMessage, publisher: AmqpMessage[String] => F[Unit]): F[Unit] = {
      import com.mdagdelen.models.QueueMessageDerivation._
      import ioEncoder._

      val message = AmqpMessage(data, AmqpProperties.empty)

      Sync[F].pure(message).map(jsonEncode[QueueMessage]).flatMap(publisher)
    }
  }
}

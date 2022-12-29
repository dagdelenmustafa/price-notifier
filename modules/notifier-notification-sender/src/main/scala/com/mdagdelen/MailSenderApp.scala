package com.mdagdelen

import cats.effect.{IO, IOApp}
import com.mdagdelen.models.VerificationQueueMessage
import com.mdagdelen.resources.RabbitMQClient
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.json.Fs2JsonDecoder
import dev.profunktor.fs2rabbit.model.AckResult._
import dev.profunktor.fs2rabbit.model._
import io.chrisdavenport.fuuid.circe._
import io.circe._
import io.circe.generic.auto._
import fs2._

object MailSenderApp extends IOApp.Simple {

  object ioDecoder extends Fs2JsonDecoder

  val queueName: QueueName = QueueName("verification-queue")

  val jsonPipe: Pipe[IO, AmqpEnvelope[String], (Either[Error, VerificationQueueMessage], DeliveryTag)] = in => {
    import ioDecoder._

    in.map {
      jsonDecode[VerificationQueueMessage]
    }
  }

  val errorSink: Pipe[IO, (Error, DeliveryTag), AckResult] = in =>
    in.evalMap { e =>
      IO.delay(println(s"error: ${e._1}")).as(NAck(e._2))
    }

  val processorSink: Pipe[IO, (VerificationQueueMessage, DeliveryTag), AckResult] = in =>
    in.evalMap { amqpMsg =>
      IO.delay(println(s"Consumed: $amqpMsg")).as(Ack(amqpMsg._2))
    }

  def startProcessing(consumer: Stream[IO, AmqpEnvelope[String]], acker: AckResult => IO[Unit]): Stream[IO, Unit] =
    consumer
      .through(jsonPipe)
      .flatMap {
        case (Left(error), tag) => Stream.eval(IO(error, tag)).through(errorSink).evalMap(acker)
        case (Right(msg), tag)  => Stream.eval(IO((msg, tag))).through(processorSink).evalMap(acker)
      }

  def program(R: RabbitClient[IO]): IO[Unit] = {
    R.createConnectionChannel.use { implicit channel =>
      (for {
        (acker, consumer) <- Stream.eval(R.createAckerConsumer[String](queueName = queueName))
        _                 <- startProcessing(consumer, acker)
      } yield ()).compile.drain
    }
  }

  override def run: IO[Unit] = for {
    cfg <- Config.load[IO]
    _   <- RabbitMQClient.make[IO](cfg.rabbitMQ).use(client => program(client))
  } yield ()
}

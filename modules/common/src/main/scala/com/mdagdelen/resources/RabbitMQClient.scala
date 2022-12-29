package com.mdagdelen.resources

import cats.effect.Resource
import cats.effect.Async
import com.mdagdelen.RabbitMQConfig
import dev.profunktor.fs2rabbit.config.Fs2RabbitConfig
import dev.profunktor.fs2rabbit.interpreter.RabbitClient

import scala.concurrent.duration.{FiniteDuration, SECONDS}

object RabbitMQClient {
  def make[F[_]: Async](c: RabbitMQConfig): Resource[F, RabbitClient[F]] = {
    val config: Fs2RabbitConfig = Fs2RabbitConfig(
      virtualHost = "/",
      host = c.host,
      username = Some(c.username),
      password = Some(c.password),
      port = 5672,
      ssl = false,
      connectionTimeout = FiniteDuration(3, SECONDS),
      requeueOnNack = false,
      internalQueueSize = Some(500),
      requestedHeartbeat = FiniteDuration(3, SECONDS),
      automaticRecovery = true,
      requeueOnReject = false
    )

    RabbitClient.default[F](config).resource
  }
}

package com.mdagdelen

import cats.effect.{Async, ExitCode}
import cats.syntax.all._
import com.comcast.ip4s._
import com.mdagdelen.repositories.AppRepositories
import com.mdagdelen.routes.{NotifierRoutes, ProductRoutes}
import com.mdagdelen.services.AppServices
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.{Logger => MiddlewareLogger}

object NotifierServer {

  def start[F[_]: Async]: F[ExitCode] = for {
    config <- Config.load[F]
    exitCode <- AppResources.make[F](config).use { resources =>
      for {
        repositories <- AppRepositories.make[F](resources)
        services = AppServices.make[F](repositories, resources)
        httpApp = (
          NotifierRoutes
            .recordRoutes[F](services.recordService) <+> ProductRoutes.productRoutes[F](services.productService)
        ).orNotFound
        finalHttpApp = MiddlewareLogger.httpApp(logHeaders = true, logBody = true)(httpApp)
        exitCode <- EmberServerBuilder
          .default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
          .use(_ => Async[F].never[Unit])
          .as(ExitCode.Success)
      } yield exitCode
    }
  } yield exitCode
}

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

package com.mdagdelen

import cats.Parallel
import cats.effect.{Async, ExitCode}
import cats.syntax.all._
import com.comcast.ip4s._
import com.mdagdelen.gateways.AppGateways
import com.mdagdelen.repositories.AppRepositories
import com.mdagdelen.routes.{ProductRoutes, RecordRoutes}
import com.mdagdelen.services.AppServices
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.{Logger => MiddlewareLogger}

object NotifierServer {

  def start[F[_]: Async: Parallel]: F[ExitCode] = for {
    config <- Config.load[F]
    exitCode <- AppResources.make[F](config).use { resources =>
      for {
        repositories <- AppRepositories.make[F](resources)
        gateways     <- AppGateways.make[F](config.rabbitMQ, resources)
        services = AppServices.make[F](repositories, resources, gateways)
        httpApp = (
          RecordRoutes.recordRoutes[F](services.recordService).routes <+>
            ProductRoutes.productRoutes[F](services.productService).routes
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

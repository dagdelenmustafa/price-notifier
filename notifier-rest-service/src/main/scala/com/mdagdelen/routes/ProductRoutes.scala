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

package com.mdagdelen.routes

import cats.effect.Concurrent
import cats.implicits._
import com.mdagdelen.models.ProductLookupRequest
import com.mdagdelen.services.ProductService
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.Http4sDsl

object ProductRoutes {

  def productRoutes[F[_]: Concurrent](productService: ProductService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] { case req @ POST -> Root / "product" / "lookup" =>
      req.decode[ProductLookupRequest] { cLookup =>
        for {
          product <- productService.productLookUp(cLookup.url)
          resp    <- Ok(product.asJson)
        } yield resp
      }
    }
  }
}

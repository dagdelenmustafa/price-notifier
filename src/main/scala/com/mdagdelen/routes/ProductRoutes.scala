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
import com.mdagdelen.models.ProductPriceDerivation._

object ProductRoutes {

  def productRoutes[F[_]: Concurrent](productService: ProductService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] { case req @ POST -> Root / "product" / "lookup" =>
      req.decode[ProductLookupRequest] { cLookup =>
        for {
          productInfo <- productService.productLookUp(cLookup.url)
          resp        <- Ok(productInfo.asProduct.asJson)
        } yield resp
      }
    }
  }
}

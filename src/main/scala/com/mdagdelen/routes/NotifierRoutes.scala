package com.mdagdelen.routes

import cats.effect.Concurrent
import com.mdagdelen.models.{CreateRecordRequest, CreateRecordResponse}
import com.mdagdelen.services.RecordService
import mongo4cats.bson.ObjectId
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder

object NotifierRoutes {

  def recordRoutes[F[_]: Concurrent](recordService: RecordService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / "record" / id =>
        for {
          record <- recordService.getProductRecord(ObjectId(id))
          resp   <- Ok(record.asJson)
        } yield resp

      case req @ POST -> Root / "record" =>
        req.decode[CreateRecordRequest] { cRecord =>
          for {
            id   <- recordService.storeRecord(cRecord)
            resp <- Ok(CreateRecordResponse(id))
          } yield resp
        }
    }
  }
}

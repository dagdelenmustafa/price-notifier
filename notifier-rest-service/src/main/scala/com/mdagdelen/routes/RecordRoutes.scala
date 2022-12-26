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
import com.mdagdelen.models.{CreateRecordRequest, CreateRecordResponse, Record, ResendVerificationEmailRequest}
import com.mdagdelen.services.RecordService
import io.chrisdavenport.fuuid.circe._
import io.chrisdavenport.fuuid.http4s.FUUIDVar
import io.circe.generic.auto._
import io.circe.refined._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder

object RecordRoutes {

  def recordRoutes[F[_]: Concurrent](recordService: RecordService[F]): Route[F] = new Route[F] {
    import dsl._

    override val routes: HttpRoutes[F] = HttpRoutes.of[F] {
      case GET -> Root / "record" / ObjectIdVar(id) =>
        for {
          record <- recordService.getProductRecord(id)
          resp   <- handleResp[Record](record, r => Ok(r.asJson))
        } yield resp

      case req @ POST -> Root / "record" =>
        req.decode[CreateRecordRequest] { cRecord =>
          for {
            record <- recordService.storeRecord(cRecord)
            resp <- handleResp[CreateRecordResponse](
              record,
              r => if (r.isNew) Created(r.asJson) else Ok(r.asJson)
            )
          } yield resp
        }

      case GET -> Root / "record" / "verify" / FUUIDVar(id) =>
        for {
          e    <- recordService.verifyEmail(id)
          resp <- handleResp[Unit](e, _ => Ok())
        } yield resp

      case req @ POST -> Root / "record" / "verify" / "resend" =>
        req.decode[ResendVerificationEmailRequest] { cRecord =>
          for {
            e    <- recordService.resendVerificationEmail(cRecord.id)
            resp <- handleResp[Unit](e, _ => Ok())
          } yield resp
        }
    }
  }
}

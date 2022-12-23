package com.mdagdelen.routes

import cats._
import com.mdagdelen.exceptions.{BadRequestError, BaseError, ConflictError, NotFoundError, TooManyRequestError}
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.{HttpRoutes, Response}
import org.http4s.circe.jsonEncoder
import org.http4s.dsl.Http4sDsl

case class FailedResponseData(status: Boolean = false, message: Option[String] = None)

trait Route[F[_]] extends RouteComponents[F] {
  val routes: HttpRoutes[F]
}

abstract class RouteComponents[F[_]: Applicative] {
  val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
  import dsl._

  def handleResp[A](either: Either[BaseError, A], r: A => F[Response[F]]): F[Response[F]] = {
    either match {
      case Left(err) =>
        err match {
          case e: NotFoundError       => NotFound(FailedResponseData(message = Some(e.getMessage)).asJson)
          case e: BadRequestError     => BadRequest(FailedResponseData(message = Some(e.getMessage)).asJson)
          case e: ConflictError       => Conflict(FailedResponseData(message = Some(e.getMessage)).asJson)
          case e: TooManyRequestError => TooManyRequests(FailedResponseData(message = Some(e.getMessage)).asJson)
        }
      case Right(v) => r(v)
    }
  }
}

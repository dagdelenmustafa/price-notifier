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

import cats.effect.Resource
import cats.effect.kernel.Async
import cats.implicits._
import com.mdagdelen.resources.{MongoDbClient, RabbitMQClient}
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import mongo4cats.database.MongoDatabase
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder

sealed abstract class AppResources[F[_]](
  val client: Client[F],
  val mongo: MongoDatabase[F],
  val rabbitMQ: RabbitClient[F]
)

object AppResources {

  def make[F[_]: Async](cfg: Config): Resource[F, AppResources[F]] = {
    (
      EmberClientBuilder.default.build,
      MongoDbClient.make[F](cfg.mongo),
      RabbitMQClient.make[F](cfg.rabbitMQ)
    ).parMapN(new AppResources[F](_, _, _) {})
  }
}

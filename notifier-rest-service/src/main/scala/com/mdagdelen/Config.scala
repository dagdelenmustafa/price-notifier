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

import cats.effect.kernel.Async

case class MongoConfig(connectionUrl: String, database: String)
case class RabbitMQConfig(
  host: String,
  username: String,
  password: String,
  exchangeName: String,
  verificationRoutingKey: String,
  notificationRoutingKey: String
)

sealed abstract class Config {
  val mongo: MongoConfig
  val rabbitMQ: RabbitMQConfig
}

object Config {
  def load[F[_]: Async]: F[Config] = Async[F].pure {
    new Config {
      override val mongo: MongoConfig = MongoConfig("mongodb://localhost:27017", "notifier")
      override val rabbitMQ: RabbitMQConfig = RabbitMQConfig(
        "127.0.0.1",
        "guest",
        "guest",
        "amq.direct",
        "verification-routing-key",
        "notification-routing-key"
      )
    }
  }
}

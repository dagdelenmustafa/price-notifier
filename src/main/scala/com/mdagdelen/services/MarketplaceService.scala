package com.mdagdelen.services

import cats.effect.Async
import com.mdagdelen.exceptions.Exceptions.UnsupportedMarketplace
import com.mdagdelen.models.marketplaces.{HepsiburadaMarketplace, Marketplace, TrendyolMarketplace}
import com.mdagdelen.types.Types.Hostname
import org.http4s.client.Client

trait MarketplaceService[F[_]] {
  def fromHostname(hostname: Hostname): F[Marketplace[F]]
}

object MarketplaceService {
  def make[F[_]: Async](client: Client[F]): MarketplaceService[F] = new MarketplaceService[F] {
    val marketPlaces = List(TrendyolMarketplace.make[F](client), HepsiburadaMarketplace.make[F]())

    override def fromHostname(hostname: Hostname): F[Marketplace[F]] =
      Async[F].fromOption(marketPlaces.find(_.hostname == hostname), UnsupportedMarketplace(hostname))
  }
}

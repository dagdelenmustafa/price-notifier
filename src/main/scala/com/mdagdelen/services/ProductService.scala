package com.mdagdelen.services

import cats.effect.Sync
import cats.implicits._
import com.google.common.net.InternetDomainName
import com.mdagdelen.models.marketplaces.MarketplaceProductResponse
import com.mdagdelen.types.Types.{Hostname, Path}

import java.net.URL

trait ProductService[F[_]] {
  def productLookUp(url: String): F[MarketplaceProductResponse]
}

object ProductService {
  def make[F[_]: Sync](marketplaceService: MarketplaceService[F]): ProductService[F] = new ProductService[F] {
    override def productLookUp(url: String): F[MarketplaceProductResponse] = for {
      (hostname, path) <- extractHostname(url)
      marketplace      <- marketplaceService.fromHostname(hostname)
      productInfo      <- marketplace.productInfo(path)
      // TODO: Put product info into a in memory cache.
    } yield productInfo

    private def extractHostname(urlString: String): F[(Hostname, Path)] = for {
      url <- Sync[F].delay {
        if (List("http://", "https://").exists(urlString.startsWith)) new URL(urlString)
        else new URL(s"https://$urlString")
      }
      hostname = InternetDomainName.from(url.getHost).topPrivateDomain().toString
      path     = s"${url.getPath}?${url.getQuery}"
      _        = println((hostname, path))
    } yield (hostname, path)
  }
}

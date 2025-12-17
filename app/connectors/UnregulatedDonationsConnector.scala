/*
 * Copyright 2025 HM Revenue & Customs
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

package connectors

import uk.gov.hmrc.http.HttpReads.Implicits.*
import com.google.inject.ImplementedBy
import connectors.HttpResponseOps.*
import org.apache.pekko.actor.ActorSystem
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import play.api.Configuration
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import models.*
import uk.gov.hmrc.http.client.HttpClientV2

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

import javax.inject.Inject
import java.net.URL

@ImplementedBy(classOf[UnregulatedDonationsConnectorImpl])
trait UnregulatedDonationsConnector {

  def getTotalUnregulatedDonations(charityReference: String)(using hc: HeaderCarrier): Future[Option[BigDecimal]]

}

class UnregulatedDonationsConnectorImpl @Inject() (
  http: HttpClientV2,
  configuration: Configuration,
  servicesConfig: ServicesConfig,
  val actorSystem: ActorSystem
)(using
  ExecutionContext
) extends UnregulatedDonationsConnector
    with Retries {

  val baseUrl: String = servicesConfig.baseUrl("charities-claims")

  val retryIntervals: Seq[FiniteDuration] = Retries.getConfIntervals("charities-claims", configuration)

  val contextPath: String = servicesConfig
    .getConfString("charities-claims.context-path", "charities-claims")

  final def getTotalUnregulatedDonations(
    charityReference: String
  )(using hc: HeaderCarrier): Future[Option[BigDecimal]] =
    retry(retryIntervals*)(shouldRetry, retryReason) {
      http
        .get(URL(s"$baseUrl$contextPath/charities/$charityReference/unregulated-donations"))
        .execute[HttpResponse]
    }.flatMap(response =>
      if response.status == 200 then
        response
          .parseJSON[GetTotalUnregulatedDonationsResponse]()
          .fold(
            error => Future.failed(Exception(error)),
            result => Future.successful(Some(result.unregulatedDonationsTotal))
          )
      else if response.status == 404 then Future.successful(None)
      else
        Future.failed(
          Exception(
            s"Request to GET $contextPath/charities/$charityReference/unregulated-donations failed because of $response ${response.body}"
          )
        )
    )

}

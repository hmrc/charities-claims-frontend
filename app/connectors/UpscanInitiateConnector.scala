/*
 * Copyright 2026 HM Revenue & Customs
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
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import models.*
import uk.gov.hmrc.http.client.HttpClientV2
import play.api.Configuration
import play.api.libs.ws.JsonBodyWritables.*
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

import javax.inject.Inject
import java.net.URL

@ImplementedBy(classOf[UpscanInitiateConnectorImpl])
trait UpscanInitiateConnector {

  def initiate(
    claimId: String,
    request: UpscanInitiateRequest
  )(using HeaderCarrier): Future[UpscanInitiateResponse]
}

class UpscanInitiateConnectorImpl @Inject() (
  http: HttpClientV2,
  configuration: Configuration,
  servicesConfig: ServicesConfig,
  val actorSystem: ActorSystem
)(using ExecutionContext)
    extends UpscanInitiateConnector
    with Retries {

  val baseUrl: String = servicesConfig.baseUrl("upscan-initiate")

  val retryIntervals: Seq[FiniteDuration] = Retries.getConfIntervals("upscan-initiate", configuration)

  val contextPath: String       = servicesConfig.getConfString("upscan-initiate.context-path", "upscan")
  val upscanServiceName: String = servicesConfig.getConfString("upscan-initiate.service-name", "charities-claims")

  def callbackUrl(claimId: String): String =
    s"${servicesConfig.baseUrl("charities-claims-validation")}${servicesConfig
        .getConfString("charities-claims-validation.context-path", "charities-claims-validation")}/$claimId/upscan-callback"

  final def initiate(
    claimId: String,
    request: UpscanInitiateRequest
  )(using HeaderCarrier): Future[UpscanInitiateResponse] =
    retry(retryIntervals*)(shouldRetry, retryReason) {
      http
        .post(URL(s"$baseUrl$contextPath/v2/initiate"))
        .withBody(
          Json.toJson(
            request.copy(
              consumingService = Some(upscanServiceName),
              callbackUrl = Some(callbackUrl(claimId))
            )
          )
        )
        .execute[HttpResponse]
    }.flatMap(response =>
      if response.status >= 200 && response.status < 300
      then
        response
          .parseJSON[UpscanInitiateResponse]()
          .fold(
            error => Future.failed(Exception(error)),
            result => Future.successful(result)
          )
      else
        Future.failed(
          Exception(
            s"Request to GET $contextPath/v2/initiate failed because of $response ${response.body}"
          )
        )
    )

}

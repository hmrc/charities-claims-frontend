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

import com.google.inject.ImplementedBy
import uk.gov.hmrc.http.client.HttpClientV2
import javax.inject.Inject
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.Future
import models.GetClaimsResponse
import models.GetClaimsRequest
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import play.api.Configuration
import scala.concurrent.duration.FiniteDuration
import java.net.URL
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.*
import scala.concurrent.ExecutionContext
import uk.gov.hmrc.http.HttpResponse
import HttpResponseOps.*
import org.apache.pekko.actor.ActorSystem
import uk.gov.hmrc.http.HttpReads.Implicits.*

@ImplementedBy(classOf[ClaimsConnectorImpl])
trait ClaimsConnector {

  def retrieveUnsubmittedClaims(using hc: HeaderCarrier): Future[GetClaimsResponse]

}

class ClaimsConnectorImpl @Inject() (
  http: HttpClientV2,
  configuration: Configuration,
  servicesConfig: ServicesConfig,
  val actorSystem: ActorSystem
)(using
  ExecutionContext
) extends ClaimsConnector
    with Retries {

  val baseUrl: String = servicesConfig.baseUrl("charities-claims")

  lazy val contextPath: String                 = servicesConfig
    .getConfString("charities-claims.context-path", "charities-claims")
  lazy val claimUrl: String                    = s"$baseUrl$contextPath/get-claims"
  lazy val retryIntervals: Seq[FiniteDuration] = Retries.getConfIntervals("charities-claims", configuration)

  def retrieveUnsubmittedClaims(using hc: HeaderCarrier): Future[GetClaimsResponse] =
    retry(retryIntervals*)(shouldRetry, retryReason)(
      http
        .post(URL(claimUrl))
        .withBody(Json.toJson(GetClaimsRequest(claimSubmitted = false)))
        .execute[HttpResponse]
    ).flatMap(response =>
      if response.status == 200 then
        response
          .parseJSON[GetClaimsResponse]()
          .fold(error => Future.failed(Exception(error)), Future.successful)
      else
        Future.failed(
          Exception(s"Request to POST $claimUrl failed because of $response ${response.body}")
        )
    )

}

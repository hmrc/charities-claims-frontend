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
import play.api.Configuration
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import models.*
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import play.api.libs.ws.JsonBodyWritables.*
import play.api.libs.json.{Json, Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

import javax.inject.Inject
import java.net.URL
import play.api.libs.json.JsNull

@ImplementedBy(classOf[ClaimsValidationConnectorImpl])
trait ClaimsValidationConnector {

  def createUpoloadTracking(claimId: String, request: CreateUploadTrackingRequest)(using
    hc: HeaderCarrier
  ): Future[Boolean]

  def getUploadSummary(claimId: String)(using hc: HeaderCarrier): Future[GetUploadSummaryResponse]

  def getUploadResult(claimId: String, reference: FileUploadReference)(using
    hc: HeaderCarrier
  ): Future[GetUploadResultResponse]

  def deleteSchedule(claimId: String, reference: FileUploadReference)(using
    hc: HeaderCarrier
  ): Future[DeleteScheduleResponse]

}

class ClaimsValidationConnectorImpl @Inject() (
  http: HttpClientV2,
  configuration: Configuration,
  servicesConfig: ServicesConfig,
  val actorSystem: ActorSystem
)(using
  ExecutionContext
) extends ClaimsValidationConnector
    with Retries {

  val baseUrl: String = servicesConfig.baseUrl("charities-claims-validation")

  val retryIntervals: Seq[FiniteDuration] = Retries.getConfIntervals("charities-claims-validation", configuration)

  val contextPath: String = servicesConfig
    .getConfString("charities-claims-validation.context-path", "charities-claims-validation")

  def createUpoloadTracking(claimId: String, request: CreateUploadTrackingRequest)(using
    hc: HeaderCarrier
  ): Future[Boolean] =
    callValidationBackend[CreateUploadTrackingRequest, SuccessResponse](
      method = "POST",
      url = s"$baseUrl$contextPath/$claimId/create-upload-tracking",
      payload = Some(request)
    ).map(_.success)

  final def getUploadSummary(claimId: String)(using hc: HeaderCarrier): Future[GetUploadSummaryResponse] =
    callValidationBackend[Nothing, GetUploadSummaryResponse](
      method = "GET",
      url = s"$baseUrl$contextPath/$claimId/upload-results"
    )

  final def getUploadResult(claimId: String, reference: FileUploadReference)(using
    hc: HeaderCarrier
  ): Future[GetUploadResultResponse] =
    callValidationBackend[Nothing, GetUploadResultResponse](
      method = "GET",
      url = s"$baseUrl$contextPath/$claimId/upload-results/$reference"
    )

  final def deleteSchedule(claimId: String, reference: FileUploadReference)(using
    hc: HeaderCarrier
  ): Future[DeleteScheduleResponse] =
    callValidationBackend[Nothing, DeleteScheduleResponse](
      method = "DELETE",
      url = s"$baseUrl$contextPath/$claimId/upload-results/$reference"
    ).flatMap { response =>
      if response.success then Future.successful(response)
      else
        Future.failed(
          Exception(
            s"Request to DELETE $contextPath/$claimId/upload-results/$reference returned success: false"
          )
        )
    }

  private def callValidationBackend[I, O](
    method: String,
    url: String,
    payload: Option[I] = None
  )(using
    writes: Writes[I],
    reads: Reads[O],
    hc: HeaderCarrier
  ): Future[O] =
    retry(retryIntervals*)(shouldRetry, retryReason) {
      val request: RequestBuilder = method match {
        case "GET"    => http.get(URL(url))
        case "POST"   => http.post(URL(url))
        case "PUT"    => http.put(URL(url))
        case "DELETE" => http.delete(URL(url))
      }
      payload
        .fold(request)(p => request.withBody(Json.toJson(p)))
        .execute[HttpResponse]
    }.flatMap(response =>
      if response.status == 200 then
        response
          .parseJSON[O]()
          .fold(error => Future.failed(Exception(error)), Future.successful)
      else
        Future.failed(
          Exception(s"Request to $method $url failed because of $response ${response.body}")
        )
    )

  given Writes[Nothing] = Writes.apply(_ => JsNull)

}

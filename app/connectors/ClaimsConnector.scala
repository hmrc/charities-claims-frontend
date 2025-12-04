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
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import models.*
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import play.api.Configuration
import play.api.libs.ws.JsonBodyWritables.*
import play.api.libs.json.{Json, Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

import javax.inject.Inject
import java.net.URL
import play.api.libs.json.JsNull

@ImplementedBy(classOf[ClaimsConnectorImpl])
trait ClaimsConnector {

  type UserId = String

  def retrieveUnsubmittedClaims(using hc: HeaderCarrier): Future[GetClaimsResponse]
  def saveClaim(repaymentClaimDetails: RepaymentClaimDetails)(using hc: HeaderCarrier): Future[UserId]
  def updateClaim(claimId: String, repaymentClaimDetails: RepaymentClaimDetails)(using hc: HeaderCarrier): Future[Unit]
  def deleteClaim(claimId: String)(using hc: HeaderCarrier): Future[Boolean]
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

  val retryIntervals: Seq[FiniteDuration] = Retries.getConfIntervals("charities-claims", configuration)

  val contextPath: String = servicesConfig
    .getConfString("charities-claims.context-path", "charities-claims")

  val retrieveUnsubmittedClaimsUrl: String = s"$baseUrl$contextPath/get-claims"
  val saveClaimUrl: String                 = s"$baseUrl$contextPath/claims"
  val updateClaimUrl: String               = s"$baseUrl$contextPath/claims"
  val deleteClaimUrl: String               = s"$baseUrl$contextPath/claims"

  final def retrieveUnsubmittedClaims(using hc: HeaderCarrier): Future[GetClaimsResponse] =
    callCharitiesClaimsBackend[GetClaimsRequest, GetClaimsResponse](
      method = "POST",
      url = retrieveUnsubmittedClaimsUrl,
      payload = Some(GetClaimsRequest(claimSubmitted = false))
    )

  final def saveClaim(repaymentClaimDetails: RepaymentClaimDetails)(using
    hc: HeaderCarrier
  ): Future[UserId] = {
    val payload = SaveClaimRequest(
      claimingGiftAid = repaymentClaimDetails.claimingGiftAid,
      claimingTaxDeducted = repaymentClaimDetails.claimingTaxDeducted,
      claimingUnderGasds = repaymentClaimDetails.claimingUnderGasds,
      claimReferenceNumber = repaymentClaimDetails.claimReferenceNumber,
      claimingDonationsNotFromCommunityBuilding = repaymentClaimDetails.claimingDonationsNotFromCommunityBuilding,
      claimingDonationsCollectedInCommunityBuildings =
        repaymentClaimDetails.claimingDonationsCollectedInCommunityBuildings,
      connectedToAnyOtherCharities = repaymentClaimDetails.connectedToAnyOtherCharities,
      makingAdjustmentToPreviousClaim = repaymentClaimDetails.makingAdjustmentToPreviousClaim
    )

    callCharitiesClaimsBackend[SaveClaimRequest, SaveClaimResponse](
      method = "POST",
      url = saveClaimUrl,
      payload = Some(payload)
    ).map(_.claimId)
  }

  final def updateClaim(claimId: String, repaymentClaimDetails: RepaymentClaimDetails)(using
    hc: HeaderCarrier
  ): Future[Unit] = {
    val payload = UpdateClaimRequest(
      claimId,
      repaymentClaimDetails = Some(repaymentClaimDetails)
    )
    callCharitiesClaimsBackend[UpdateClaimRequest, UpdateClaimResponse](
      method = "PUT",
      url = updateClaimUrl,
      payload = Some(payload)
    ).map(_ => ())
  }

  final def deleteClaim(claimId: String)(using
    hc: HeaderCarrier
  ): Future[Boolean] =
    callCharitiesClaimsBackend[Nothing, DeleteClaimResponse](
      method = "DELETE",
      url = deleteClaimUrl,
      payload = None
    ).map(r => r.success)

  private def callCharitiesClaimsBackend[I, O](method: String, url: String, payload: Option[I])(using
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

case class MissingRequiredFieldsException(message: String) extends Exception(message)

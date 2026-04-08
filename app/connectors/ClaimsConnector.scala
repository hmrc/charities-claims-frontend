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
import connectors.HttpResponseOps.*
import models.*
import org.apache.pekko.actor.ActorSystem
import play.api.{Configuration, Logging}
import play.api.libs.json.{JsNull, Json, Reads, Writes}
import play.api.libs.ws.JsonBodyWritables.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URL
import javax.inject.Inject
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[ClaimsConnectorImpl])
trait ClaimsConnector {

  type UserId = String

  def retrieveUnsubmittedClaims(using hc: HeaderCarrier): Future[GetClaimsResponse]
  def saveClaim(repaymentClaimDetails: RepaymentClaimDetails)(using hc: HeaderCarrier): Future[SaveClaimResponse]

  def getClaim(claimId: String)(using hc: HeaderCarrier): Future[Option[Claim]]
  def updateClaim(claimId: String, updateClaimRequest: UpdateClaimRequest)(using
    hc: HeaderCarrier
  ): Future[UpdateClaimResponse]
  def submitClaim(claimId: String, lastUpdatedReference: String, declarationLanguage: String)(using
    hc: HeaderCarrier
  ): Future[SubmitClaimResponse]
  def deleteClaim(claimId: String)(using hc: HeaderCarrier): Future[Boolean]

  def getSubmissionClaimSummary(claimId: String)(using hc: HeaderCarrier): Future[SubmissionSummaryResponse]
}

class ClaimsConnectorImpl @Inject() (
  http: HttpClientV2,
  configuration: Configuration,
  servicesConfig: ServicesConfig,
  val actorSystem: ActorSystem
)(using
  ExecutionContext
) extends ClaimsConnector
    with Retries
    with Logging {

  val baseUrl: String = servicesConfig.baseUrl("charities-claims")

  val retryIntervals: Seq[FiniteDuration] = Retries.getConfIntervals("charities-claims", configuration)

  val contextPath: String = servicesConfig
    .getConfString("charities-claims.context-path", "charities-claims")

  val claimsApiUrl: String = s"$baseUrl$contextPath/claims"

  val chrisApiUrl: String = s"$baseUrl$contextPath/chris"

  final def retrieveUnsubmittedClaims(using hc: HeaderCarrier): Future[GetClaimsResponse] =
    callCharitiesClaimsBackend[Nothing, GetClaimsResponse](
      method = "GET",
      url = s"$claimsApiUrl?claimSubmitted=false",
      payload = None
    )

  final def saveClaim(repaymentClaimDetails: RepaymentClaimDetails)(using
    hc: HeaderCarrier
  ): Future[SaveClaimResponse] = {
    val payload = SaveClaimRequest(
      claimingGiftAid = repaymentClaimDetails.claimingGiftAid,
      claimingTaxDeducted = repaymentClaimDetails.claimingTaxDeducted,
      claimingUnderGiftAidSmallDonationsScheme = repaymentClaimDetails.claimingUnderGiftAidSmallDonationsScheme,
      claimReferenceNumber = repaymentClaimDetails.claimReferenceNumber,
      claimingDonationsNotFromCommunityBuilding = repaymentClaimDetails.claimingDonationsNotFromCommunityBuilding,
      claimingDonationsCollectedInCommunityBuildings =
        repaymentClaimDetails.claimingDonationsCollectedInCommunityBuildings,
      connectedToAnyOtherCharities = repaymentClaimDetails.connectedToAnyOtherCharities,
      makingAdjustmentToPreviousClaim = repaymentClaimDetails.makingAdjustmentToPreviousClaim
    )

    callCharitiesClaimsBackend[SaveClaimRequest, SaveClaimResponse](
      method = "POST",
      url = claimsApiUrl,
      payload = Some(payload)
    )
  }

  final def getClaim(claimId: String)(using
    hc: HeaderCarrier
  ): Future[Option[Claim]] =
    given Reads[Option[Claim]] = Reads.optionWithNull[Claim]
    callCharitiesClaimsBackend[Nothing, Option[Claim]](
      method = "GET",
      url = s"$claimsApiUrl/$claimId",
      noneOnNotFound = true,
      noneValue = None
    )

  final def updateClaim(claimId: String, updateClaimRequest: UpdateClaimRequest)(using
    hc: HeaderCarrier
  ): Future[UpdateClaimResponse] =
    callCharitiesClaimsBackend[UpdateClaimRequest, UpdateClaimResponse](
      method = "PUT",
      url = s"$claimsApiUrl/$claimId",
      payload = Some(updateClaimRequest)
    )

  final def deleteClaim(claimId: String)(using
    hc: HeaderCarrier
  ): Future[Boolean] =
    callCharitiesClaimsBackend[Nothing, DeleteClaimResponse](
      method = "DELETE",
      url = s"$claimsApiUrl/$claimId"
    ).map(r => r.success)

  final def submitClaim(claimId: String, lastUpdatedReference: String, declarationLanguage: String)(using
    hc: HeaderCarrier
  ): Future[SubmitClaimResponse] =
    callCharitiesClaimsBackend[SubmitClaimRequest, SubmitClaimResponse](
      method = "POST",
      url = chrisApiUrl,
      payload = Some(SubmitClaimRequest(claimId, lastUpdatedReference, declarationLanguage))
    )

  final def getSubmissionClaimSummary(claimId: String)(using hc: HeaderCarrier): Future[SubmissionSummaryResponse] =
    val claimSummaryApiUrl: String = s"$baseUrl$contextPath/submission-summary/$claimId"
    callCharitiesClaimsBackend[Nothing, SubmissionSummaryResponse](
      method = "GET",
      url = claimSummaryApiUrl,
      payload = None
    )

  private def callCharitiesClaimsBackend[I, O](
    method: String,
    url: String,
    payload: Option[I] = None,
    noneOnNotFound: Boolean = false,
    noneValue: O = null
  )(using
    writes: Writes[I],
    reads: Reads[O],
    hc: HeaderCarrier
  ): Future[O] = {
    logger.info(s"$method $url [requestId=${hc.requestId.map(_.value).getOrElse("-")}]")
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
          .fold(
            error => {
              logger.error(s"Failed to parse response from $method $url: $error")
              Future.failed(Exception(error))
            },
            Future.successful
          )
      else if noneOnNotFound && response.status == 404 then Future.successful(noneValue)
      else if response.status == 400 then
        response
          .parseJSON[ClaimError]()
          .fold(
            error => {
              logger.error(s"Failed to parse 400 error response from $method $url: $error")
              Future.failed(Exception(error))
            },
            e => {
              logger.warn(s"$method $url returned 400: ${e.getMessage}")
              Future.failed(e)
            }
          )
      else {
        logger.error(s"$method $url failed with status ${response.status}")
        Future.failed(Exception(s"Request to $method $url failed because of $response ${response.body}"))
      }
    )
  }

  given Writes[Nothing] = Writes.apply(_ => JsNull)
}

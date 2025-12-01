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

@ImplementedBy(classOf[ClaimsConnectorImpl])
trait ClaimsConnector {

  type UserId = String

  def retrieveUnsubmittedClaims(using hc: HeaderCarrier): Future[GetClaimsResponse]
  def saveClaim(repaymentClaimDetails: RepaymentClaimDetails)(using hc: HeaderCarrier): Future[UserId]
  def updateClaim(claimId: String, repaymentClaimDetails: RepaymentClaimDetails)(using hc: HeaderCarrier): Future[Unit]
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

  private type HttpVerb = HttpClientV2 => URL => RequestBuilder

  val baseUrl: String = servicesConfig.baseUrl("charities-claims")

  val retryIntervals: Seq[FiniteDuration] = Retries.getConfIntervals("charities-claims", configuration)

  val contextPath: String = servicesConfig
    .getConfString("charities-claims.context-path", "charities-claims")

  val retrieveUnsubmittedClaimsUrl: String = s"$baseUrl$contextPath/get-claims"
  val saveClaimUrl: String                 = s"$baseUrl$contextPath/claims"
  val updateClaimUrl: String               = s"$baseUrl$contextPath/claims"

  final def retrieveUnsubmittedClaims(using hc: HeaderCarrier): Future[GetClaimsResponse] =
    callCharitiesClaimsBackend[GetClaimsRequest, GetClaimsResponse](_.post)(
      retrieveUnsubmittedClaimsUrl,
      GetClaimsRequest(claimSubmitted = false)
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

    callCharitiesClaimsBackend[SaveClaimRequest, SaveClaimResponse](_.post)(
      saveClaimUrl,
      payload
    ).map(_.claimId)
  }

  final def updateClaim(claimId: String, repaymentClaimDetails: RepaymentClaimDetails)(using
    hc: HeaderCarrier
  ): Future[Unit] = {
    val payload = UpdateClaimRequest(
      claimId,
      repaymentClaimDetails = Some(repaymentClaimDetails)
    )
    callCharitiesClaimsBackend[UpdateClaimRequest, UpdateClaimResponse](_.put)(
      updateClaimUrl,
      payload
    ).map(_ => ())
  }

  private def callCharitiesClaimsBackend[I, O](verb: HttpVerb)(url: String, payload: I)(using
    writes: Writes[I],
    reads: Reads[O]
  ): Future[O] =
    retry(retryIntervals*)(shouldRetry, retryReason)(
      verb(http)(URL(url))
        .withBody(Json.toJson(payload))
        .execute[HttpResponse]
    ).flatMap(response =>
      if response.status == 200 then
        response
          .parseJSON[O]()
          .fold(error => Future.failed(Exception(error)), Future.successful)
      else
        Future.failed(
          Exception(s"Request to POST $retrieveUnsubmittedClaimsUrl failed because of $response ${response.body}")
        )
    )
}

case class MissingRequiredFieldsException(message: String) extends Exception(message)

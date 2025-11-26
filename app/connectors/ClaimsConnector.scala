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
import models.SaveClaimResponse
import models.RepaymentClaimDetails
import models.SaveClaimRequest
import play.api.libs.json.Writes
import play.api.libs.json.Reads

@ImplementedBy(classOf[ClaimsConnectorImpl])
trait ClaimsConnector {

  type UserId = String

  def retrieveUnsubmittedClaims(using hc: HeaderCarrier): Future[GetClaimsResponse]
  def saveClaim(repaymentClaimDetails: RepaymentClaimDetails)(using hc: HeaderCarrier): Future[UserId]

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

  lazy val retryIntervals: Seq[FiniteDuration] = Retries.getConfIntervals("charities-claims", configuration)

  lazy val contextPath: String = servicesConfig
    .getConfString("charities-claims.context-path", "charities-claims")

  lazy val retrieveUnsubmittedClaimsUrl: String = s"$baseUrl$contextPath/get-claims"
  lazy val saveClaimUrl: String                 = s"$baseUrl$contextPath/claims"

  final def retrieveUnsubmittedClaims(using hc: HeaderCarrier): Future[GetClaimsResponse] =
    callCharitiesClaimsBackend[GetClaimsRequest, GetClaimsResponse](
      retrieveUnsubmittedClaimsUrl,
      GetClaimsRequest(claimSubmitted = false)
    )

  final def saveClaim(repaymentClaimDetails: RepaymentClaimDetails)(using hc: HeaderCarrier): Future[UserId] =
    callCharitiesClaimsBackend[SaveClaimRequest, SaveClaimResponse](
      saveClaimUrl,
      SaveClaimRequest(
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
    ).map(_.claimId)

  private def callCharitiesClaimsBackend[I, O](url: String, payload: I)(using
    hc: HeaderCarrier,
    writes: Writes[I],
    reads: Reads[O]
  ): Future[O] =
    retry(retryIntervals*)(shouldRetry, retryReason)(
      http
        .post(URL(url))
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

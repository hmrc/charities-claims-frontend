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

package util

import connectors.ClaimsConnector
import uk.gov.hmrc.http.HeaderCarrier
import models.*

import scala.concurrent.Future

class FakeClaimsConnector(
  claim: Claim
) extends ClaimsConnector {

  override def retrieveUnsubmittedClaims(using hc: HeaderCarrier): Future[GetClaimsResponse] =
    Future.successful(
      GetClaimsResponse(
        claimsCount = 0,
        claimsList = List(
          ClaimInfo(
            claimId = claim.claimId,
            hmrcCharitiesReference = None,
            nameOfCharity = None
          )
        )
      )
    )

  override def getClaim(claimId: String)(using hc: HeaderCarrier): Future[Option[Claim]] =
    Future.successful(Some(claim))

  override def saveClaim(repaymentClaimDetails: RepaymentClaimDetails)(using
    hc: HeaderCarrier
  ): Future[SaveClaimResponse] =
    Future.successful(SaveClaimResponse(claimId = claim.claimId, lastUpdatedReference = "1234567890"))

  override def updateClaim(claimId: String, updateClaimRequest: UpdateClaimRequest)(using
    hc: HeaderCarrier
  ): Future[UpdateClaimResponse] =
    Future.successful(UpdateClaimResponse(success = true, lastUpdatedReference = "1234567890"))

  override def submitClaim(claimId: String, lastUpdatedReference: String, declarationLanguage: String)(using
    hc: HeaderCarrier
  ): Future[SubmitClaimResponse] =
    Future.successful(SubmitClaimResponse(success = true, submissionReference = "1234567890"))

  override def getSubmissionClaimSummary(claimId: String)(using hc: HeaderCarrier): Future[SubmissionSummaryResponse] =
    Future.successful(
      SubmissionSummaryResponse(
        claimDetails = ClaimDetails("test charity", "test ref", "2026-04-07T11:34:21.147Z", "Mr John"),
        giftAidDetails = None,
        otherIncomeDetails = None,
        gasdsDetails = None,
        adjustmentDetails = None,
        submissionReferenceNumber = "sub ref"
      )
    )

  override def deleteClaim(claimId: String)(using hc: HeaderCarrier): Future[Boolean] =
    Future.successful(true)

  override def updateLastVisitedAt(claimId: String)(using hc: HeaderCarrier): Future[Unit] =
    Future.successful(())

  override def hasUnsubmittedClaim(charitiesReference: String)(using hc: HeaderCarrier): Future[Boolean] =
    Future.successful(
      !claim.claimSubmitted
        && claim.claimData.repaymentClaimDetails.hmrcCharitiesReference.contains(charitiesReference)
    )
}

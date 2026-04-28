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

package controllers.giftAidSmallDonationsScheme

import models.GiftAidSmallDonationsSchemeDonationDetails
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.Json
import play.api.test.Helpers.{LOCATION, *}
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class ClaimDetailsForTaxYearCheckYourAnswersControllerISpec
  extends ComponentSpecHelper
    with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val index = 1
  private val url   = s"/check-claim-details-for-tax-year/$index"

  "GET /check-claim-details-for-tax-year/:index" should {

    "render the check your answers page when claim exists" in {
      stubBackend()

      val result = get(url)

      result.status shouldBe OK
    }
  }

  "POST /check-claim-details-for-tax-year/:index" should {

    "redirect to claim added page" in {
      stubBackend()

      val result = post(url)(Json.obj())

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value shouldBe
        "/charities-claims/claim-added-for-tax-year"
    }
  }

  private def stubBackend(): Unit = {

    val existingClaims =
      claim.claimData.giftAidSmallDonationsSchemeDonationDetails
        .map(_.claims)
        .getOrElse(Seq.empty)

    val gasdsClaim = claim.copy(
      claimData = claim.claimData.copy(
        repaymentClaimDetails = claim.claimData.repaymentClaimDetails.copy(
          claimingUnderGiftAidSmallDonationsScheme = true,
          makingAdjustmentToPreviousClaim = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(true),
          claimingDonationsCollectedInCommunityBuildings = Some(false),
          connectedToAnyOtherCharities = Some(false),
          claimReferenceNumber = Some("ref")
        ),
        giftAidSmallDonationsSchemeDonationDetails = Some(
          GiftAidSmallDonationsSchemeDonationDetails(
            adjustmentForGiftAidOverClaimed = BigDecimal(0),
            claims = existingClaims.take(1)
          )
        )
      )
    )

    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(gasdsClaim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
  }
}

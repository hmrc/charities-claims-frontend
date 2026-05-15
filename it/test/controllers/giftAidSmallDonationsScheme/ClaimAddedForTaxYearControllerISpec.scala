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

import models.Mode.NormalMode
import models.{GiftAidSmallDonationsSchemeClaim, GiftAidSmallDonationsSchemeDonationDetails}
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.Json
import play.api.test.Helpers.{LOCATION, *}
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class ClaimAddedForTaxYearControllerISpec     extends ComponentSpecHelper
  with TestDataUtils
  with ClaimsStub
  with AuthStub
  with ClaimsValidationStub {

  private val url = s"/claim-added-for-tax-year"

  "GET /claim-added-for-tax-year" should {

    "render the claim tax page" in {
      stubBackend()

      val result = get(url)

      result.status shouldBe OK
      Jsoup.parse(result.body).title should include(
        msg("You have added a claim for 1 tax year - Make a charity tax repayment claim - GOV.UK")
      )
    }
  }

  "POST /claim-added-for-tax-year" should {

    "redirect to check your answers page when valid add another tax year submitted" in {
      stubBackend()

      val result =
        post(url)(Json.obj("value" -> true))

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value shouldBe
        controllers.giftAidSmallDonationsScheme.routes.WhichTaxYearAreYouClaimingForController.onPageLoad(2,NormalMode).url
    }
  }
  private def stubBackend(): Unit = {

    val baseClaim = claim.copy(
      claimData = claim.claimData.copy(
        repaymentClaimDetails = claim.claimData.repaymentClaimDetails.copy(
          claimingGiftAid = true,
          claimingTaxDeducted = false,
          claimingUnderGiftAidSmallDonationsScheme = true,
          claimReferenceNumber = Some("ref"),
          makingAdjustmentToPreviousClaim = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(true),
          claimingDonationsCollectedInCommunityBuildings = Some(false),
          connectedToAnyOtherCharities = Some(false)
        ),
        giftAidSmallDonationsSchemeDonationDetails = Some(
          GiftAidSmallDonationsSchemeDonationDetails(
            adjustmentForGiftAidOverClaimed = BigDecimal(0),
            claims = Seq(
              GiftAidSmallDonationsSchemeClaim(
                taxYear = 2025,
                amountOfDonationsReceived = BigDecimal(0)
              )
            )
          )
        )
      )
    )

    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(baseClaim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
  }

}

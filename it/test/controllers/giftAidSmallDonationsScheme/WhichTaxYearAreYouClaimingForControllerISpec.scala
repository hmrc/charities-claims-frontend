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
import models.Mode.NormalMode
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.Json
import play.api.test.Helpers.{LOCATION, *}
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class WhichTaxYearAreYouClaimingForControllerISpec
    extends ComponentSpecHelper
    with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val index = 1
  private val url   = s"/which-tax-year-are-you-claiming-for/$index"

  private val validYear = 2026

  "GET /which-tax-year-are-you-claiming-for/:index" should {

    "render the tax year page" in {
      stubBackend()

      val result = get(url)

      result.status                shouldBe OK
      Jsoup.parse(result.body).title should include(
        msg("whichTaxYearAreYouClaimingFor.title")
      )
    }
  }

  "POST /which-tax-year-are-you-claiming-for/:index" should {

    "redirect to donation amount page when valid year submitted" in {
      stubBackend()

      val result =
        post(url)(Json.obj("value" -> validYear.toString))

      result.status                 shouldBe SEE_OTHER
      result.header(LOCATION).value shouldBe routes.DonationAmountYouAreClaimingController.onPageLoad(index, NormalMode).url
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
            claims = Seq.empty
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

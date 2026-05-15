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

import models.{GiftAidSmallDonationsSchemeClaim, GiftAidSmallDonationsSchemeDonationDetails}
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.Json
import play.api.test.Helpers.{LOCATION, *}
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import util.TestUsers
import utils.{ComponentSpecHelper, TestDataUtils}

// had to rename to GasdsDetailsCheckYourAnswersControllerISpec from GiftAidSmallDonationsSchemeDetailsCheckYourAnswersControllerISpec because on internal mongo error
class GasdsDetailsCheckYourAnswersControllerISpec extends ComponentSpecHelper
  with TestDataUtils
  with ClaimsStub
  with AuthStub
  with ClaimsValidationStub {

  private val url = s"/check-your-gift-aid-small-donations-scheme-donation-details"

  "GET /check-your-gift-aid-small-donations-scheme-donation-details" should {

    "render the check your answers page when claim exists" in {
      stubBackend()

      val result = get(url)

      result.status shouldBe OK
      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("giftAidSmallDonationsSchemeDetailsCheckYourAnswers.title"))

      Jsoup.parse(result.body).title should include(
        msg("giftAidSmallDonationsSchemeDetailsCheckYourAnswers.title"))
    }
  }
  "POST /check-your-gift-aid-small-donations-scheme-donation-details" should {

    "redirect to claim added page" in {
      val url   = "/check-your-gift-aid-small-donations-scheme-donation-details"
      stubBackend()

      val result = post(url)(Json.obj())

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value shouldBe
        controllers.routes.ClaimsTaskListController.onPageLoad.url
    }
  }

  private def stubBackend(): Unit = {
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
            claims = Seq(
              GiftAidSmallDonationsSchemeClaim(
                taxYear = 2025,
                amountOfDonationsReceived = BigDecimal(20.2)
              )
            )
          )
        )
      )
    )

    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(gasdsClaim.copy(userId = TestUsers.agent1)))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
    stubUpdateClaim(claimId)(OK, Json.toJson(updateClaimResponse))

  }
}

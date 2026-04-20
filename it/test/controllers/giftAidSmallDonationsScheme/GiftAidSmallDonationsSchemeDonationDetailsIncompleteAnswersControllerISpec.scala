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

import org.jsoup.Jsoup
import play.api.http.Status.OK
import play.api.libs.json.Json
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class GiftAidSmallDonationsSchemeDonationDetailsIncompleteAnswersControllerISpec
    extends ComponentSpecHelper
    with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val url = "/cannot-set-up-gasds-claim"

  "GET /cannot-set-up-gasds-claim" should {

    "render the incomplete answers page with missing fields" in {
      val gasdsClaim = claim.copy(
        claimData = claim.claimData.copy(
          repaymentClaimDetails = claim.claimData.repaymentClaimDetails.copy(
            claimingUnderGiftAidSmallDonationsScheme = true,
            claimingDonationsNotFromCommunityBuilding = Some(true),
            claimingDonationsCollectedInCommunityBuildings = Some(false),
            connectedToAnyOtherCharities = Some(false),
            makingAdjustmentToPreviousClaim = Some(false),
            claimReferenceNumber = Some("ref")
          )
        )
      )

      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(gasdsClaim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result = get(url)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)

      doc.title() should include(msg("giftAidSmallDonationsSchemeDonationDetailsIncompleteAnswers.title"))
    }
  }
}

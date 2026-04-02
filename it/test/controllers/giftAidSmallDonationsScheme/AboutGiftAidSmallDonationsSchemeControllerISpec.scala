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
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.http.HeaderNames.LOCATION
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class AboutGiftAidSmallDonationsSchemeControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  "GET /about-gift-aid-small-donations-scheme" should {

    "render the about the gasds page when guard condition is satisfied" in {
      stubBackend()

      val result = get("/about-gift-aid-small-donations-scheme")

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("aboutGiftAidSmallDonationsScheme.title"))
    }
  }

  "POST /about-gift-aid-small-donations-scheme" should {

    // TODO when next page available
    "redirect to next page" in {
      stubBackend()

      val result = post("/about-gift-aid-small-donations-scheme")(Json.obj())

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/make-a-charity-repayment-claim")
    }
  }

  private def stubBackend(): Unit = {
    stubAuthRequest()
    val updatedClaim =
      claim.copy(
        claimData = claim.claimData.copy(
          repaymentClaimDetails =
            claim.claimData.repaymentClaimDetails.map(_.copy(
              claimingDonationsCollectedInCommunityBuildings = Some(true),
              claimingUnderGiftAidSmallDonationsScheme = true,
              makingAdjustmentToPreviousClaim = Some(true)
            ))
      )
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(updatedClaim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
  }
}
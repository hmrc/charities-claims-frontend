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

package controllers.repaymentClaimDetails

import models.Mode.NormalMode
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.http.HeaderNames.LOCATION
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class ClaimingCommunityBuildingDonationsControllerISpec
    extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
      with AuthStub
    with ClaimsValidationStub {

  "GET /claim-community-building-donations" should {

    "render the community building donations page" in {
      stubBackend()

      val result = get("/claim-community-building-donations")

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title() should include(msg("claimingCommunityBuildingDonations.title"))
    }
  }

  "POST /claim-community-building-donations" should {

    "redirect to next page when user selects yes" in {
      stubBackend()

      val result =
        post("/claim-community-building-donations")(
          Json.obj("value" -> true)
        )

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value shouldBe routes.ChangePreviousGASDSClaimController.onPageLoad(NormalMode).url
    }

    "redirect to next page when user selects no" in {
      stubBackend()

      val result =
        post("/claim-community-building-donations")(
          Json.obj("value" -> false)
        )

      result.status shouldBe SEE_OTHER
    }

    "return BadRequest when form submission is invalid" in {
      stubBackend()

      val result =
        post("/claim-community-building-donations")(Json.obj())

      result.status shouldBe BAD_REQUEST
    }

    "render update confirmation page in CheckMode when answer changes" in {
      stubBackend()

      val result =
        post("/change-claim-community-building-donations")(
          Json.obj("value" -> false)
        )

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title() should include(msg("updateRepaymentClaim.title"))
    }
  }

  private def stubBackend(): Unit = {
    stubAuthRequest()
    val updatedClaim =
      claim.copy(
        claimData = claim.claimData.copy(
          repaymentClaimDetails =
            claim.claimData.repaymentClaimDetails.copy(
              claimingUnderGiftAidSmallDonationsScheme = true,
              claimingDonationsCollectedInCommunityBuildings = Some(true),
              claimingGiftAid = false
            )
        )
      )
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(updatedClaim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
  }
}

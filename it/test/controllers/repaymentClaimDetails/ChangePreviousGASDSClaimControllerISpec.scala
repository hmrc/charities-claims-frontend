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

class ChangePreviousGASDSClaimControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with AuthStub
    with ClaimsStub
    with ClaimsValidationStub {

  "GET /change-previous-gift-aid-small-donations-scheme-claim" should {

    "render the change previous GASDS claim page" in {
      stubBackend()

      val result = get("/change-previous-gift-aid-small-donations-scheme-claim")

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("changePreviousGASDSClaim.title"))
    }
  }

  "POST /change-previous-gift-aid-small-donations-scheme-claim" should {

    "redirect to claiming reference number page when user submits valid answer" in {
      stubBackend()

      val result =
        post("/change-previous-gift-aid-small-donations-scheme-claim")(
          Json.obj("value" -> true)
        )

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value shouldBe routes.ClaimingReferenceNumberController.onPageLoad(NormalMode).url
    }

    "return BadRequest when form submission is invalid" in {
      stubBackend()

      val result =
        post("/change-previous-gift-aid-small-donations-scheme-claim")(Json.obj())

      result.status shouldBe BAD_REQUEST
    }

    "render update confirmation page when answer changes in CheckMode" in {
      stubBackend()

      val result =
        post("/change-change-previous-gift-aid-small-donations-scheme-claim")(
          Json.obj("value" -> false)
        )

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("updateRepaymentClaim.title"))
    }
  }

  private def stubBackend(): Unit = {
    stubAuthRequest()
    val updatedClaim =
      claim.copy(
        claimData = claim.claimData.copy(
          repaymentClaimDetails =
            claim.claimData.repaymentClaimDetails.copy(
              claimingDonationsCollectedInCommunityBuildings = Some(true),
              claimingUnderGiftAidSmallDonationsScheme = true,
              makingAdjustmentToPreviousClaim = Some(true)
            )
        )
      )
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(updatedClaim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
  }
}

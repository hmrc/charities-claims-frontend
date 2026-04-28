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

import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.http.HeaderNames.LOCATION
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class RepaymentClaimDetailsCheckYourAnswersControllerISpec
    extends ComponentSpecHelper with TestDataUtils with AuthStub
    with ClaimsStub
    with ClaimsValidationStub {

  "GET /check-your-repayment-claim" should {

    "render the check your answers page" in {
      stubBackend()

      val result = get("/check-your-repayment-claim")

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("repaymentClaimDetailsCheckYourAnswers.title"))
    }
  }

  "POST /check-your-repayment-claim" should {

    "redirect to task list when answers are complete" in {

      stubBackend()

      val result = post("/check-your-repayment-claim")(Json.obj())

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value shouldBe controllers.routes.ClaimsTaskListController.onPageLoad.url
    }

    "redirect to incomplete answers page when answers are incomplete" in {

      stubBackendWithIncompleteAnswers()

      val result = post("/check-your-repayment-claim")(Json.obj())

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value shouldBe routes.RepaymentClaimDetailsIncompleteAnswersController.onPageLoad.url
    }
  }

  private def stubBackend(): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(claim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
    stubUpdateClaim(claimId)(OK, Json.toJson(updateClaimResponse))
  }

  private def stubBackendWithIncompleteAnswers(): Unit = {
    stubAuthRequest()
    val updatedClaim =
      claim.copy(
        claimData = claim.claimData.copy(
          repaymentClaimDetails =
            claim.claimData.repaymentClaimDetails.copy(
              claimingUnderGiftAidSmallDonationsScheme = true,
              claimingDonationsNotFromCommunityBuilding = None
            )
        )
      )
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(updatedClaim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
    stubUpdateClaim(claimId)(OK, Json.toJson(updateClaimResponse))
  }
}

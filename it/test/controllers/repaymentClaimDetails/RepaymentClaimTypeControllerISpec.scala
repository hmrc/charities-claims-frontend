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

class RepaymentClaimTypeControllerISpec
    extends ComponentSpecHelper
    with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  "GET /select-repayment-claim-type" should {

    "render the repayment claim type page" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result = get("/select-repayment-claim-type")

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title() should include(msg("repaymentClaimType.title"))
    }
  }

  "POST /select-repayment-claim-type" should {

    "redirect to next page when valid data submitted" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result =
        post("/select-repayment-claim-type")(
          Map(
            "value[0]" -> "claimingGiftAid"
          )
        )

      result.status                 shouldBe SEE_OTHER
      result.header(LOCATION).value shouldBe routes.ClaimingReferenceNumberController.onPageLoad(NormalMode).url
    }

    "return BadRequest when form submission is invalid" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result =
        post("/select-repayment-claim-type")(Map.empty[String, Seq[String]])

      result.status shouldBe BAD_REQUEST
    }

    "render update confirmation page when answer changes in CheckMode" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result =
        post("/change-select-repayment-claim-type")(
          Map(
            "value[1]" -> "claimingUnderGiftAidSmallDonationsScheme"
          )
        )

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title() should include(msg("updateRepaymentClaim.title"))
    }
  }
}

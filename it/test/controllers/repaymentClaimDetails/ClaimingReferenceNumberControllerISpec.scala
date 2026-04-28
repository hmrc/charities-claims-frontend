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
import play.api.test.Helpers.*
import play.api.libs.json.Json
import play.api.test.Helpers.OK
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class ClaimingReferenceNumberControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  "GET /claim-reference-number" should {

    "render the claiming reference number page" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result = get("/claim-reference-number")

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title() should include(msg("claimingReferenceNumber.title"))
    }
  }

  "POST /claiming-reference-number" should {

    "redirect to enter claim reference number when user selects yes" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result =
        post("/claim-reference-number")(
          Json.obj("value" -> true)
        )

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value shouldBe routes.ClaimReferenceNumberInputController.onPageLoad(NormalMode).url
    }

    "redirect to check your answers when user selects no" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result =
        post("/claim-reference-number")(
          Json.obj("value" -> false)
        )

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value shouldBe routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
    }

    "return BadRequest when form submission is invalid" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result =
        post("/claim-reference-number")(Json.obj())

      result.status shouldBe BAD_REQUEST
    }
  }
}
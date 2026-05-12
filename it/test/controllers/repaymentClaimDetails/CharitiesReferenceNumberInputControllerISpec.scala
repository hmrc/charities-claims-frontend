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

class CharitiesReferenceNumberInputControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  val normalUrl = "/enter-charities-reference-number?claimId=123"
  val changeUrl = "/change-charities-reference-number?claimId=123"

  "GET /enter-charities-reference-number" should {

    "render the enter charity reference number page for an organisation" in {
      stubAgentAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result = get(normalUrl)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title() should include(msg("charitiesReferenceNumber.title"))
    }
    
  }

  "POST /enter-charities-reference-number" should {

    "redirect to enter charity name page when valid reference is submitted" in {
      stubAgentAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result =
        post(normalUrl)(
          Json.obj("value" -> "A12345")
        )

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value shouldBe routes.EnterCharityNameController.onPageLoad(NormalMode).url
    }


    "return BadRequest when form submission is invalid" in {
      stubAgentAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result =
        post(normalUrl)(Json.obj())

      result.status shouldBe BAD_REQUEST
    }

    "POST /change-charities-reference-number" should {

      "redirect to the check your answers page when valid value submitted" in {
        stubAgentAuthRequest()
        stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
        stubGetClaims(claimId)(OK, Json.toJson(claim))
        stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

        val result =
          post(changeUrl)(
            Json.obj("value" -> "A12345")
          )

        result.status shouldBe SEE_OTHER
        result.header(LOCATION).value shouldBe routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
      }
    }
  }
}
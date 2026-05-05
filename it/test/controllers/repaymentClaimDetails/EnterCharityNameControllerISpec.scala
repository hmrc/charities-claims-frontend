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
import models.Mode.NormalMode
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.http.HeaderNames.LOCATION
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class EnterCharityNameControllerISpec
  extends ComponentSpecHelper with TestDataUtils with ClaimsStub with AuthStub with ClaimsValidationStub {

  "GET /enter-the-charity-name" should {

    "render the enter charity name page" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result = get("/enter-the-charity-name")

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title() should include(msg("enterCharityName.title"))
    }
  }

  "GET /change-the-charity-name" should {

    "render the change charity name page" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result = get("/change-the-charity-name")

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.select("form").text() should not be empty
    }
  }

  "POST /enter-the-charity-name" should {

    "redirect to the repayment claim type page when valid value submitted" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result =
        post("/enter-the-charity-name")(
          Json.obj("value" -> "Test Name")
        )

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value shouldBe routes.RepaymentClaimTypeController.onPageLoad(NormalMode).url
    }
  }


  "POST /change-the-charity-name" should {

    "redirect to the check your answers page when valid value submitted" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result =
        post("/change-the-charity-name")(
          Json.obj("value" -> "Test Name")
        )

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value shouldBe routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
    }
  }

}
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

class RepaymentClaimDetailsControllerISpec extends ComponentSpecHelper with TestDataUtils with ClaimsStub with AuthStub with ClaimsValidationStub {

  "GET /repayment-claim-details" should {

    "render the repayment claim details page for an organisation" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result = get("/repayment-claim-details")

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.getElementsByClass("govuk-caption-l").text() should include(msg("repaymentClaimDetails.subheading"))
    }

    "render the repayment claim details page for an agent" in {
      stubAgentAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result = get("/repayment-claim-details?claimId=123")

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.getElementsByClass("govuk-caption-l").text() should include(msg("repaymentClaimDetails.agent.subheading"))
    }
  }

  "POST /repayment-claim-details" should {

    "redirect to repayment claim type page for an organisation" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result = post("/repayment-claim-details")(Map.empty[String, Seq[String]])

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value shouldBe routes.RepaymentClaimTypeController.onPageLoad(NormalMode).url
    }

/*    "redirect to repayment claim claim reference number page for an agent" in {
      // TODO: Need to change Create screen R1.9, currently redirecting to R1.8
      stubAgentAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result = post("/repayment-claim-details")(Map.empty[String, Seq[String]])

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value shouldBe controllers.routes.ClaimsTaskListController.onPageLoad.url
    }*/
  }
}

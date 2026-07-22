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

package controllers

import models.GetClaimsResponse
import org.jsoup.Jsoup
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class ClaimCannotBeSavedControllerISpec extends ComponentSpecHelper
  with AuthStub with TestDataUtils with ClaimsStub
  with ClaimsValidationStub {

  private val noClaimsResponse = GetClaimsResponse(claimsCount = 0, claimsList = List.empty)

  "GET /claim-cannot-be-saved" should {

    "render the WRN16 page for an agent user" in {
      stubAgentAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(noClaimsResponse))

      val result = get("/claim-cannot-be-saved")

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("warning16UnsubmittedClaimExistsForCharity.title"))
      doc.select("h1").text shouldBe msg("warning16UnsubmittedClaimExistsForCharity.heading")
      doc.select("p.govuk-body").text should include(msg("warning16UnsubmittedClaimExistsForCharity.paragraph.2"))
    }

    "reject the request if an organisation user" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(noClaimsResponse))

      val result = get("/claim-cannot-be-saved")

      result.status shouldBe SEE_OTHER
    }
  }
}

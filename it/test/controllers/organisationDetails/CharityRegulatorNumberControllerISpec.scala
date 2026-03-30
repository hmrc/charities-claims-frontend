/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.organisationDetails

import models.Claim
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class CharityRegulatorNumberControllerISpec extends ComponentSpecHelper
  with AuthStub with TestDataUtils with ClaimsStub with ClaimsValidationStub {

  private val normalUrl    = "/charity-regulator-number"
  private val checkModeUrl = "/change-charity-regulator-number"

  "GET /charity-regulator-number" should {

    "render the page when charity regulator name exists" in {
      stubBackend()

      val result = get(normalUrl)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("charityRegulatorNumber.title"))
    }

    "redirect to task list when charity regulator name is missing" in {
      stubBackend(claimWithoutRegulator())

      val result = get(normalUrl)

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/make-a-charity-repayment-claim")
    }
  }

  "POST /charity-regulator-number" should {

    "save the registration number and redirect to corporate trustee page in NormalMode" in {
      stubBackend()

      val result =
        post(normalUrl)(
          Json.obj("value" -> "12345678")
        )

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/corporate-trustee-claim")
    }

    "redirect to check your answers page in CheckMode" in {
      stubBackend()

      val result =
        post(checkModeUrl)(
          Json.obj("value" -> "123456")
        )

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/check-your-organisation-details")
    }

    "return BAD_REQUEST when submission is invalid" in {
      stubBackend()

      val result = post(normalUrl)(Json.obj())

      result.status shouldBe BAD_REQUEST
    }
  }

  private def stubBackend(testClaim: Claim = claim): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(testClaim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
  }

  private def claimWithoutRegulator(): Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        organisationDetails = None
      )
    )
}

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

import models.{Claim, NameOfCharityRegulator}
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class ReasonNotRegisteredWithRegulatorControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val normalUrl = "/charity-not-registered"
  private val checkModeUrl = "/change-charity-not-registered"

  "GET /charity-not-registered" should {

    "render the page when regulator is None" in {
      stubBackend(claimWithRegulator(NameOfCharityRegulator.None))

      val result = get(normalUrl)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("reasonNotRegisteredWithRegulator.title"))
    }

    "redirect to task list when regulator is present" in {
      stubBackend()

      val result = get(normalUrl)

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/make-a-charity-repayment-claim")
    }
  }

  "POST /charity-not-registered" should {

    "return BAD_REQUEST when form submission invalid" in {
      stubBackend(claimWithRegulator(NameOfCharityRegulator.None))

      val result = post(normalUrl)(Json.obj())

      result.status shouldBe BAD_REQUEST
    }

    "redirect to charity excepted page when Excepted selected" in {
      stubBackend(claimWithRegulator(NameOfCharityRegulator.None))

      val result =
        post(normalUrl)(
          Json.obj("value" -> "Excepted")
        )

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/charity-excepted")
    }

    "redirect to charity exempt page when Exempt selected" in {
      stubBackend(claimWithRegulator(NameOfCharityRegulator.None))

      val result =
        post(normalUrl)(
          Json.obj("value" -> "Exempt")
        )

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/charity-exempt")
    }

    "redirect to corporate trustee claim when LowIncome selected in NormalMode" in {
      stubBackend(claimWithRegulator(NameOfCharityRegulator.None))

      val result =
        post(normalUrl)(
          Json.obj("value" -> "LowIncome")
        )

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/corporate-trustee-claim")
    }

    "redirect to check your answers when LowIncome selected in CheckMode" in {
      stubBackend(claimWithRegulator(NameOfCharityRegulator.None))

      val result =
        post(checkModeUrl)(
          Json.obj("value" -> "LowIncome")
        )

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/check-your-organisation-details")
    }
  }

  private def stubBackend(testClaim: Claim = claim): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(testClaim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
  }

  private def claimWithRegulator(answer: NameOfCharityRegulator): Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        organisationDetails =
          claim.claimData.organisationDetails.map(
            _.copy(nameOfCharityRegulator = answer)
          )
      )
    )
}

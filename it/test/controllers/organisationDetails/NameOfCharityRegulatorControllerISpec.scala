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

class NameOfCharityRegulatorControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val normalUrl = "/name-of-charity-regulator"
  private val checkModeUrl = "/change-name-of-charity-regulator"

  "GET /name-of-charity-regulator" should {

    "render the page" in {
      stubBackend(claim)

      val result = get(normalUrl)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("nameOfCharityRegulator.title"))
    }
  }

  "POST /name-of-charity-regulator" should {

    "return BAD_REQUEST when submission invalid" in {
      stubBackend()

      val result = post(normalUrl)(Json.obj())

      result.status shouldBe BAD_REQUEST
    }

    "redirect to charity-not-registered page when None selected in NormalMode" in {
      stubBackend()

      val result =
        post(normalUrl)(
          Json.obj("value" -> "None")
        )

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/charity-not-registered")
    }

    "redirect to regulator number page when regulator selected in NormalMode" in {
      stubBackend()

      val result =
        post(normalUrl)(
          Json.obj("value" -> "EnglandAndWales")
        )

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/charity-regulator-number")
    }
  }

  "POST /change-name-of-charity-regulator" should {

    "redirect to charity not registered page when regulator changed to None" in {
      stubBackend()

      val result =
        post(checkModeUrl)(
          Json.obj("value" -> "None")
        )

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/change-charity-not-registered")
    }

    "redirect to regulator number when None changed to regulator" in {
      stubBackend(claimWithAnswer(NameOfCharityRegulator.None))

      val result =
        post(checkModeUrl)(
          Json.obj("value" -> "EnglandAndWales")
        )

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/change-charity-regulator-number")
    }

    "redirect to check your answers when answer unchanged" in {
      stubBackend()

      val result =
        post(checkModeUrl)(
          Json.obj("value" -> "EnglandAndWales")
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

  private def claimWithAnswer(answer: NameOfCharityRegulator): Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        organisationDetails =
          claim.claimData.organisationDetails.map(
            _.copy(nameOfCharityRegulator = answer)
          )
      )
    )
}

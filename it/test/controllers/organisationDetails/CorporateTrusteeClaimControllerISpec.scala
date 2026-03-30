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

class CorporateTrusteeClaimControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val normalUrl = "/corporate-trustee-claim"
  private val checkModeUrl = "/change-corporate-trustee-claim"

  "GET /corporate-trustee-claim" should {

    "render the page" in {
      stubBackend()

      val result = get(normalUrl)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("corporateTrusteeClaim.title"))
    }
  }

  "POST /corporate-trustee-claim" should {

    "return BAD_REQUEST when submission is invalid" in {
      stubBackend()

      val result = post(normalUrl)(Json.obj())

      result.status shouldBe BAD_REQUEST
    }

    "redirect to corporate trustee address when Yes in NormalMode" in {
      stubBackend()

      val result = post(normalUrl)(Json.obj("value" -> true))

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/corporate-trustee-address")
    }

    "redirect to authorised official address when No in NormalMode" in {
      stubBackend()

      val result = post(normalUrl)(Json.obj("value" -> false))

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/authorised-official-address")
    }

    "redirect to authorised official address when new No in CheckMode" in {
      stubBackend()

      val result = post(checkModeUrl)(Json.obj("value" -> false))

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/change-authorised-official-address")
    }

    "redirect to check your answers when answer unchanged in CheckMode" in {
      stubBackend()

      val result = post(checkModeUrl)(Json.obj("value" -> true))

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/check-your-organisation-details")
    }
  }

  private def stubBackend(): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(claim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
  }
}

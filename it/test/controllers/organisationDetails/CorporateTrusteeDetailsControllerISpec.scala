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

class CorporateTrusteeDetailsControllerISpec extends ComponentSpecHelper
  with AuthStub with TestDataUtils with ClaimsStub with ClaimsValidationStub {

  private val normalUrl = "/corporate-trustee-details"

  "GET /corporate-trustee-details" should {

    "render the page when corporate trustee and address answer exist" in {
      stubBackend()

      val result = get(normalUrl)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("corporateTrusteeDetails.title"))
    }

    "redirect to task list when corporate trustee is false" in {
      stubBackend(claimWithoutCorporateTrustee())

      val result = get(normalUrl)

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/make-a-charity-repayment-claim")
    }
  }

  "POST /corporate-trustee-details" should {

    "return BAD_REQUEST when form submission invalid" in {
      stubBackend()
      val result = post(normalUrl)(Json.obj())

      result.status shouldBe BAD_REQUEST
    }

    "save details and redirect to check your answers" in {
      stubBackend()

      val result =
        post(normalUrl)(
          Json.obj(
            "nameOfCorporateTrustee"                 -> "Corporate Trustee1",
            "corporateTrusteeDaytimeTelephoneNumber" -> "01234567890",
            "corporateTrusteePostcode"               -> "SW1A 1AA"
          )
        )

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/check-your-organisation-details")
    }
  }

  private def stubBackend(testClaim: Claim = claim): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(testClaim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
  }

  private def claimWithoutCorporateTrustee(): Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        organisationDetails = claim.claimData.organisationDetails.map(
          _.copy(
            areYouACorporateTrustee = false
          )
        )
      )
    )
}

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

package controllers.organisationDetails

import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.http.HeaderNames.LOCATION
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class AuthorisedOfficialAddressControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  "GET /authorised-official-address" should {

    "render the authorised official address page when not a corporate trustee" in {
      stubBackend()

      val result = get("/authorised-official-address")

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("authorisedOfficialAddress.title"))
    }

    "redirect to task list when user is a corporate trustee" in {
      stubBackendWithCorporateTrustee()

      val result = get("/authorised-official-address")

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/make-a-charity-repayment-claim")
    }
  }

  "POST /authorised-official-address" should {

    "redirect to authorised official details page when valid answer submitted" in {
      stubBackend()

      val result =
        post("/authorised-official-address")(
          Json.obj("value" -> true)
        )

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/authorised-official-details")
    }

    "return BadRequest when form submission is invalid" in {

      stubBackend()

      val result =
        post("/authorised-official-address")(Json.obj())

      result.status shouldBe BAD_REQUEST
    }
  }

  private def stubBackend(): Unit = {
    val notCorporateTrusteeClaim =
      claim.copy(
        claimData = claim.claimData.copy(
          organisationDetails =
            claim.claimData.organisationDetails.map(
              _.copy(areYouACorporateTrustee = false)
            )
        )
      )
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(notCorporateTrusteeClaim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
  }

  private def stubBackendWithCorporateTrustee(): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(claim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
  }
}
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

import models.Claim
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.http.HeaderNames.LOCATION
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class AuthorisedOfficialDetailsControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val validPayload = Json.obj(
    "firstName"   -> "John",
    "lastName"    -> "Doe",
    "phoneNumber" -> "01234567890",
    "postcode"    -> "SW1A 1AA"
  )

  "GET /authorised-official-details" should {

    "render the authorised official details page when prerequisites are satisfied" in {
      stubBackend(claimWithoutCorporateTrustee)

      val result = get("/authorised-official-details")

      result.status shouldBe OK
      Jsoup.parse(result.body).title should include(msg("authorisedOfficialDetails.title"))
    }

    "redirect to task list when corporate trustee is true" in {
      stubBackend(claim)

      val result = get("/authorised-official-details")

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/make-a-charity-repayment-claim")
    }
  }

  "POST /authorised-official-details" should {

    "save authorised official details and redirect to check your answers page" in {
      stubBackend(claimWithoutCorporateTrustee)

      val result = post("/authorised-official-details")(validPayload)

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/check-your-organisation-details")
    }

    "return BadRequest when form submission is invalid" in {
      stubBackend(claimWithoutCorporateTrustee)

      val result = post("/authorised-official-details")(Json.obj())

      result.status shouldBe BAD_REQUEST
    }

    "redirect to authorised official address page when address answer missing on submit" in {
      stubBackend(claimWithoutAddressAnswer)

      val result = post("/authorised-official-details")(Json.obj())

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/authorised-official-address")
    }
  }

  private def stubBackend(testClaim: Claim): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(testClaim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
  }

  private def claimWithoutCorporateTrustee: Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        organisationDetails =
          claim.claimData.organisationDetails.map(
            _.copy(
              doYouHaveAuthorisedOfficialTrusteeUKAddress = Some(true),
              areYouACorporateTrustee = false
            )
          )
      )
    )

  private def claimWithoutAddressAnswer: Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        organisationDetails =
          claim.claimData.organisationDetails.map(
            _.copy(
              doYouHaveAuthorisedOfficialTrusteeUKAddress = None,
              areYouACorporateTrustee = false
            )
          )
      )
    )
}
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

class OrganisationDetailsCheckYourAnswersControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val url = "/check-your-organisation-details"

  "GET /check-your-organisation-details" should {

    "render the check your answers page" in {
      stubBackend()

      val result = get(url)

      result.status shouldBe OK
      Jsoup.parse(result.body).title should include(msg("organisationDetailsCheckYourAnswers.title"))
    }
  }

  "POST /check-your-organisation-details" should {

    "save answers and redirect to task list when answers complete" in {
      stubBackend()
      stubUpdateClaim(claimId)(OK, Json.toJson(updateClaimResponse))

      val result = post(url)(Json.obj())

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/make-a-charity-repayment-claim")
    }

    "redirect to incomplete answers page when answers not complete" in {
      stubBackend(claimWithoutOrganisationDetails)

      val result = post(url)(Json.obj())

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/cannot-set-up-organisation-details")
    }
  }

  private def stubBackend(testClaim: Claim = claim): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(testClaim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
  }

  private def claimWithoutOrganisationDetails: Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        organisationDetails = None
      )
    )
}

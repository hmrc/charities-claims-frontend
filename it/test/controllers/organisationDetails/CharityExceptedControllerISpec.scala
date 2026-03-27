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

import models.{Claim, ReasonNotRegisteredWithRegulator}
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class CharityExceptedControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val normalUrl = "/charity-excepted"
  private val checkModeUrl = "/change-charity-excepted"

  "GET /charity-excepted" should {

    "render the page when reason not registered is Excepted" in {
      stubBackend(claimWithReason(Some(ReasonNotRegisteredWithRegulator.Excepted)))

      val result = get(normalUrl)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("charityExcepted.title"))
    }

    "redirect to task list when reason not registered is not Excepted" in {
      stubBackend(claimWithReason(None))

      val result = get(normalUrl)

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/make-a-charity-repayment-claim")
    }
  }

  "POST /charity-excepted" should {

    "redirect to corporate trustee claim page in NormalMode" in {
      stubBackend(claimWithReason(Some(ReasonNotRegisteredWithRegulator.Excepted)))

      val result = post(normalUrl)(Json.obj())

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/corporate-trustee-claim")
    }

    "redirect to check your answers page in CheckMode" in {
      stubBackend(claimWithReason(Some(ReasonNotRegisteredWithRegulator.Excepted)))

      val result = post(checkModeUrl)(Json.obj())

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/check-your-organisation-details")
    }
  }

  private def stubBackend(testClaim: Claim): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(testClaim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
  }

  private def claimWithReason(
                               reason: Option[ReasonNotRegisteredWithRegulator]
                             ): Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        organisationDetails =
          claim.claimData.organisationDetails.map(
            _.copy(reasonNotRegisteredWithRegulator = reason)
          )
      )
    )
}
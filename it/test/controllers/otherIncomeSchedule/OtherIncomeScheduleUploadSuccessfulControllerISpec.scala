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

package controllers.otherIncomeSchedule

import models.{Claim, DeleteScheduleResponse, UpdateClaimResponse}
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class OtherIncomeScheduleUploadSuccessfulControllerISpec
    extends ComponentSpecHelper
    with AuthStub
    with TestDataUtils
    with ClaimsStub
    with ClaimsValidationStub {

  private val url = "/other-income-schedule-upload-successful?claimId=123"

  "GET /other-income-schedule-upload-successful" should {

    "render the other income schedule upload successful page" in {
      stubBackend(false)

      val result = get(url)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("otherIncomeScheduleUploadSuccessful.title"))
    }

    "render the agent other income schedule upload successful page" in {
      stubBackend(true)

      val result = get(url)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.text should include(msg("otherIncomeScheduleUploadSuccessful.agent.message.2"))
    }
  }

  "POST /other-income-schedule-upload-successful" should {
    
    "redirect to claims task list page" in {
      stubBackend(false)

      val result =
        post(url)(
          Json.obj()
        )

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/make-a-charity-repayment-claim")
    }
  }

  private def stubBackend(isAgent: Boolean): Unit = {
    if(isAgent) {
      stubAgentAuthRequest()
    }else{
      stubAuthRequest()
    }
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))

    stubGetClaims(
      claimId
    )(
      OK,
      Json.toJson(claimWithOtherIncomeSchedule)
    )

    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryOtherIncomeValidatedResponse))
  }

  private def claimWithOtherIncomeSchedule: Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        repaymentClaimDetails = claim.claimData.repaymentClaimDetails.copy(
          claimingTaxDeducted = true
        ),
        otherIncomeScheduleFileUploadReference = Some(otherIncomefileRef)
      )
    )
}

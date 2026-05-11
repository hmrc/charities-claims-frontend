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

import models.*
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class CheckYourOtherIncomeScheduleControllerISpec
    extends ComponentSpecHelper with TestDataUtils
    with ClaimsValidationStub
      with AuthStub
      with ClaimsStub {

  private val url     = "/check-your-other-income-schedule?claimId=123"

  "GET /check-your-other-income-schedule" should {

    "render the check your other income schedule page" in {
      stubBackend()

      val result = get(url)

      result.status                shouldBe OK
      Jsoup.parse(result.body).title should include(msg("checkYourOtherIncomeSchedule.title"))
    }
    
  "render the agent check your other income schedule page" in {
    stubAgentBackend()

    val result = get(url)

    result.status                shouldBe OK
    Jsoup.parse(result.body).text should include(msg("checkYourOtherIncomeSchedule.agent.heading"))
  }
  }

  "POST /check-your-other-income-schedule" should {

    "return BAD_REQUEST when form submission invalid" in {
      stubBackend()

      val result = post(url)(Json.obj())

      result.status shouldBe BAD_REQUEST
    }

    "redirect to update other income schedule when answer is Yes" in {
      stubBackend()

      val result = post(url)(Json.obj("value" -> true))

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/update-other-income-schedule")
    }

    "redirect to task list when answer is No and schedule already completed" in {
      stubBackend()

      val result = post(url)(Json.obj("value" -> false))

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/make-a-charity-repayment-claim")
    }

    "save data and redirect to success page when answer is No and schedule not completed" in {
      stubBackend(fileRefOpt = None)
      stubUpdateClaim(claimId)(OK, Json.toJson(UpdateClaimResponse(true, otherIncomefileRef)))
      val result = post(url)(Json.obj("value" -> false))

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/other-income-schedule-upload-successful")
    }
  }

  private def stubBackend(fileRefOpt: Option[FileUploadReference] = Some(otherIncomefileRef)): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(
      claimId
    )(OK, Json.toJson(claimWithTaxDeducted(fileRefOpt)))

    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryOtherIncomeValidatedResponse))
    stubGetUploadResult(claimId, otherIncomefileRef)(OK, validatedOtherIncomeJson)
  }
  
 private def stubAgentBackend(fileRefOpt: Option[FileUploadReference] = Some(otherIncomefileRef)): Unit = {
    stubAgentAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(
      claimId
    )(OK, Json.toJson(claimWithTaxDeducted(fileRefOpt)))

    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryOtherIncomeValidatedResponse))
    stubGetUploadResult(claimId, otherIncomefileRef)(OK, validatedOtherIncomeJson)
  }

  private def claimWithTaxDeducted(fileRef: Option[FileUploadReference]): Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        repaymentClaimDetails = claim.claimData.repaymentClaimDetails.copy(
          claimingTaxDeducted = true
        ),
        otherIncomeScheduleFileUploadReference = fileRef
      )
    )

  private val validatedOtherIncomeJson = Json.parse(
    """
      {
        "reference": "test-other-income-file-upload-ref",
        "validationType": "OtherIncome",
        "fileStatus": "VALIDATED",
        "otherIncomeData": {
          "adjustmentForOtherIncomePreviousOverClaimed": 78.00,
          "totalOfGrossPayments": 123.00,
          "totalOfTaxDeducted": 39.00,
          "otherIncomes": [
            {
              "otherIncomeItem": 1,
              "payerName": "Test User",
              "paymentDate": "2025-01-01",
              "grossPayment": 1234,
              "taxDeducted": 56
            }
          ]
        }
      }
    """
  )
}

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

class YourOtherIncomeScheduleUploadControllerISpec
    extends ComponentSpecHelper
    with TestDataUtils
    with AuthStub
    with ClaimsStub
    with ClaimsValidationStub {

  private val pageUrl   = "/your-other-income-schedule-upload"
  private val removeUrl = "/your-other-income-schedule-upload/remove"
  private val submitUrl = "/your-other-income-schedule-upload"

  "GET /your-other-income-schedule-upload" should {

    "redirect to upload page when file reference missing" in {
      stubBackend(withReference = false)
      stubEmptyUploadSummary()

      val result = get(pageUrl)

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-other-income-schedule")
    }

    "render the page when upload result exists" in {
      stubBackend()
      stubUploadSummary()
      stubGetUploadResult(claimId, otherIncomefileRef)(OK, Json.toJson(getUploadResultVerifying))

      val result = get(pageUrl)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("yourOtherIncomeScheduleUpload.title"))
    }
  }

  "GET /your-other-income-schedule-upload/remove" should {

    "delete upload and redirect to upload page" in {
      stubBackend()
      stubUploadSummary()
      stubDeleteSchedule(claimId, otherIncomefileRef)(OK, Json.toJson(SuccessResponse(true)))
      stubUpdateClaim(claimId)(OK, Json.toJson(UpdateClaimResponse(success = true, claimId)))

      val result = get(removeUrl)

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-other-income-schedule")
    }
  }

  "POST /your-other-income-schedule-upload" should {

    "redirect to check page when upload validated" in {
      stubBackend()
      stubUploadSummary()
      stubGetUploadResult(claimId, otherIncomefileRef)(OK, Json.toJson(getUploadResultValidated))

      val result = post(submitUrl)(Json.obj())

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/check-your-other-income-schedule")
    }
  }

  private def stubBackend(withReference: Boolean = true): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))

    val claimResponse =
      claim.copy(
        claimData = claim.claimData.copy(
          repaymentClaimDetails = claim.claimData.repaymentClaimDetails.copy(
            claimingTaxDeducted = true
          ),
          otherIncomeScheduleFileUploadReference = if withReference then Some(otherIncomefileRef) else None
        )
      )

    stubGetClaims(claimId)(OK, Json.toJson(claimResponse))
  }

  private def stubUploadSummary(): Unit =
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryOtherIncomeValidatedResponse))

  private def stubEmptyUploadSummary(): Unit =
    stubGetUploadSummary(claimId)(OK, Json.toJson(GetUploadSummaryResponse(Seq.empty)))

  private val getUploadResultVerifying = Json.parse(
    """
      {
        "reference": "test-other-income-file-upload-ref",
        "validationType": "OtherIncome",
        "fileStatus": "VERIFYING"
      }
    """
  )

  private val getUploadResultValidated = Json.parse(
    """
      {
        "reference": "f5da5578-8393-4cd1-be0e-d8ef1b78d8e7",
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

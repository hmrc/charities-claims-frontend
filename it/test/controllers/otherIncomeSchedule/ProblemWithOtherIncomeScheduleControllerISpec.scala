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

class ProblemWithOtherIncomeScheduleControllerISpec
    extends ComponentSpecHelper
    with TestDataUtils
    with AuthStub
    with ClaimsStub
    with ClaimsValidationStub {

  private val url = "/problem-with-other-income-schedule"

  "GET /problem-with-other-income-schedule" should {

    "render the page when validation errors exist" in {
      stubBackendWithValidationErrors()

      val result = get(url)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("problemWithOtherIncomeSchedule.title"))
    }

    "redirect to upload page when file reference is missing" in {
      stubBackendWithoutFileRef()

      val result = get(url)

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-other-income-schedule")
    }

    "redirect to upload summary when upload result is not validation failed" in {
      stubBackendWithValidatedResult()

      val result = get(url)

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/your-other-income-schedule-upload")
    }
  }

  "POST /problem-with-other-income-schedule" should {

    "delete schedule and redirect to upload page" in {
      stubBackendWithValidationErrors()
      val deleteRes = DeleteScheduleResponse(success = true)
      stubDeleteSchedule(claimId, otherIncomefileRef)(OK, Json.toJson(deleteRes))
      stubUpdateClaim(claimId)(OK, Json.toJson(UpdateClaimResponse(success = true, claimId)))

      val result = post(url)(Json.obj())

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-other-income-schedule")
    }
  }

  private def stubBackendWithValidationErrors(): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))

    stubGetClaims(
      claimId
    )(OK, Json.toJson(claimWithOtherIncome))

    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryOtherIncomeValidatedResponse))

    val validationFailedJson = Json.parse(
      s"""
      {
        "reference": "$otherIncomefileRef",
        "validationType": "OtherIncome",
        "fileStatus": "VALIDATION_FAILED",
        "errors": [
          {
            "field": "payerName",
            "error": "Invalid payer name"
          }
        ]
      }
      """
    )

    stubGetUploadResult(claimId, otherIncomefileRef)(OK, validationFailedJson)
  }

  private def stubBackendWithValidatedResult(): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))

    stubGetClaims(
      claimId
    )(OK, Json.toJson(claimWithOtherIncome))

    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryOtherIncomeValidatedResponse))

    val validatedJson = Json.parse(
      s"""
      {
        "reference": "$otherIncomefileRef",
        "validationType": "OtherIncome",
        "fileStatus": "VALIDATED",
        "otherIncomeData": {
          "adjustmentForOtherIncomePreviousOverClaimed": 0,
          "totalOfGrossPayments": 100,
          "totalOfTaxDeducted": 10,
          "otherIncomes": []
        }
      }
      """
    )

    stubGetUploadResult(claimId, otherIncomefileRef)(OK, validatedJson)
  }

  private def stubBackendWithoutFileRef(): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    val validatedJson = Json.parse(
      s"""
          {
            "reference": "f5da5578-8393-4cd1-be0e-d8ef1b78d8e7",
            "validationType": "OtherIncome",
            "fileStatus": "AWAITING_UPLOAD",
            "uploadUrl": "https://xxxx/upscan-upload-proxy/bucketName"
          }
      """
    )
    stubGetClaims(
      claimId
    )(OK, Json.toJson(claimWithoutFileRef))

    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
    stubGetUploadResult(claimId, otherIncomefileRef)(OK, validatedJson)
  }

  private def claimWithOtherIncome: Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        repaymentClaimDetails = claim.claimData.repaymentClaimDetails.copy(
          claimingTaxDeducted = true
        ),
        otherIncomeScheduleFileUploadReference = Some(otherIncomefileRef)
      )
    )

  private def claimWithoutFileRef: Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        repaymentClaimDetails = claim.claimData.repaymentClaimDetails.copy(
          claimingTaxDeducted = true
        ),
        otherIncomeScheduleFileUploadReference = None
      )
    )
}

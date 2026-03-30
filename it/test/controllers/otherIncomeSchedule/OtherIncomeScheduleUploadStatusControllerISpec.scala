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

import models.{Claim, FileUploadReference}
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class OtherIncomeScheduleUploadStatusControllerISpec
    extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
      with AuthStub
      with ClaimsValidationStub {

  private val url     = "/other-income-schedule-upload-status"
  private val fileRef = FileUploadReference("test-other-income-file-upload-ref")

  "GET /other-income-schedule-upload-status" should {

    "return upload status when upload result is available" in {
      stubBackend()

      val uploadResultJson = Json.parse(
        s"""
        {
          "reference": "$fileRef",
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

      stubGetUploadResult(claimId, fileRef)(OK, uploadResultJson)

      val result = get(url)

      result.status                         shouldBe OK
      (result.json \ "isFinal").as[Boolean] shouldBe true
    }

    "return BAD_REQUEST when backend returns CLAIM_REFERENCE_DOES_NOT_EXIST" in {
      stubBackend()

      stubGetUploadResult(claimId, fileRef)(BAD_REQUEST, Json.obj("message" -> "CLAIM_REFERENCE_DOES_NOT_EXIST"))

      val result = get(url)

      result.status shouldBe BAD_REQUEST
    }

    "return INTERNAL_SERVER_ERROR when backend throws other error" in {
      stubBackend()

      stubGetUploadResult(claimId, fileRef)(INTERNAL_SERVER_ERROR, Json.obj())

      val result = get(url)

      result.status shouldBe INTERNAL_SERVER_ERROR
    }
  }

  private def stubBackend(): Unit = {
    stubAuthRequest()

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

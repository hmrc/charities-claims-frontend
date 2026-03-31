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

package controllers.connectedCharitiesSchedule

import models.*
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class ProblemWithConnectedCharitiesScheduleControllerISpec
    extends ComponentSpecHelper
    with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val pageUrl = "/problem-with-connected-charities-schedule"

  "GET /problem-with-connected-charities-schedule" should {

    "render the page when validation errors exist" in {
      stubBackend()
      stubClaimWithConnectedCharities()
      stubUploadSummary()
      stubValidationFailed()

      val result = get(pageUrl)

      result.status                shouldBe OK
      Jsoup.parse(result.body).title should include(msg("problemWithConnectedCharitiesSchedule.title"))
    }

    "redirect to upload summary when upload result is not validation failed" in {
      stubBackend()
      stubClaimWithConnectedCharities()
      stubUploadSummary()
      stubValidatedResult()

      val result = get(pageUrl)

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/your-connected-charities-schedule-upload")
    }
  }

  "POST /problem-with-connected-charities-schedule" should {

    "delete schedule and redirect to upload page" in {
      stubBackend()
      stubClaimWithConnectedCharities()
      stubUploadSummary()
      stubValidationFailed()

      stubDeleteSchedule(claimId, connectedCharitiesFileRef)(
        OK,
        Json.toJson(DeleteScheduleResponse(success = true))
      )
      stubUpdateClaim(claimId)(OK, Json.toJson(UpdateClaimResponse(success = true, claimId)))

      val result = post(pageUrl)(Json.obj())

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-connected-charities-schedule")
    }
  }

  private def stubBackend(): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
  }

  private def stubUploadSummary(): Unit =
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryConnectedCharitiesValidatedResponse))

  private def stubClaimWithConnectedCharities(): Unit =
    stubGetClaims(claimId)(OK, Json.toJson(claimWithConnectedCharities))

  private def stubValidationFailed(): Unit =
    stubGetUploadResult(claimId, connectedCharitiesFileRef)(OK, validationFailedJson)

  private def stubValidatedResult(): Unit =
    stubGetUploadResult(claimId, connectedCharitiesFileRef)(OK, getUploadResultConnectedCharitiesValidatedJson)

  private val validationFailedJson = Json.parse(
    s"""
      {
        "reference": "$connectedCharitiesFileRef",
        "validationType": "ConnectedCharities",
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

  private def claimWithConnectedCharities: Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        repaymentClaimDetails = claim.claimData.repaymentClaimDetails.copy(connectedToAnyOtherCharities = Some(true)),
        connectedCharitiesScheduleFileUploadReference = Some(connectedCharitiesFileRef)
      )
    )
}

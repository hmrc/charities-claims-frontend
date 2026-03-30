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

class YourConnectedCharitiesScheduleUploadControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val pageUrl = "/your-connected-charities-schedule-upload"
  private val removeUrl = "/your-connected-charities-schedule-upload/remove"
  private val submitUrl = "/your-connected-charities-schedule-upload"

  "GET /your-connected-charities-schedule-upload" should {

    "redirect to upload page when file reference missing" in {
      stubBackend(withReference = false)
      stubEmptyUploadSummary()

      val result = get(pageUrl)

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-connected-charities-schedule")
    }

    "render the page when upload result exists" in {
      stubBackend()
      stubUploadSummary()
      stubGetUploadResult(claimId, connectedCharitiesFileRef)(OK, Json.toJson(getUploadResultVerifying))

      val result = get(pageUrl)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("yourConnectedCharitiesScheduleUpload.title"))
    }
  }

  "GET /your-connected-charities-schedule-upload/remove" should {

    "delete upload and redirect to upload page" in {
      stubBackend()
      stubUploadSummary()
      stubDeleteSchedule(claimId, connectedCharitiesFileRef)(OK, Json.toJson(SuccessResponse(true)))

      val result = get(removeUrl)

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-connected-charities-schedule")
    }
  }

  "POST /your-connected-charities-schedule-upload" should {

    "redirect to check page when upload validated" in {
      stubBackend()
      stubUploadSummary()
      stubGetUploadResult(claimId, connectedCharitiesFileRef)(OK, Json.toJson(getUploadResultConnectedCharitiesValidatedJson))

      val result = post(submitUrl)(Json.obj())

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/check-your-connected-charities-schedule")
    }
  }

  private def stubBackend(withReference: Boolean = true): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))

    val claimResponse =
      claim.copy(
        claimData = claim.claimData.copy(
          repaymentClaimDetails =
            claim.claimData.repaymentClaimDetails.copy(
              connectedToAnyOtherCharities = Some(true)
            ),
          connectedCharitiesScheduleFileUploadReference =
            if withReference then Some(connectedCharitiesFileRef) else None
        )
      )

    stubGetClaims(claimId)(OK, Json.toJson(claimResponse))
  }

  private def stubUploadSummary(): Unit =
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryConnectedCharitiesValidatedResponse))

  private def stubEmptyUploadSummary(): Unit =
    stubGetUploadSummary(claimId)(OK, Json.toJson(GetUploadSummaryResponse(Seq.empty)))

  private val getUploadResultVerifying = Json.parse(
    """
      {
        "reference": "test-connected-charities-file-upload-ref",
        "validationType": "ConnectedCharities",
        "fileStatus": "VERIFYING"
      }
    """
  )

}

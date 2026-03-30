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
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub, UpscanInitiateStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class UploadConnectedCharitiesScheduleControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub
    with UpscanInitiateStub {

  private val url = "/upload-connected-charities-schedule"

  "GET /upload-connected-charities-schedule" should {

    "render the upload page when upscan is already initialised" in {
      stubBackend(withUpscan = true)

      val result = get(url)

      result.status shouldBe OK
      Jsoup.parse(result.body).title should include(msg("uploadConnectedCharitiesSchedule.title"))
    }

    "redirect to your upload page when file reference already exists" in {
      stubBackend(withReference = true)

      val result = get(url)

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/your-connected-charities-schedule-upload")
    }
  }

  "GET /upload-success" should {

    "update upload status and redirect to upload summary page" in {
      stubBackend(withUpscan = true)
      stubUpdateUploadStatus(claimId, "test-reference")(OK, Json.toJson(SuccessResponse(success = true)))

      val result = get("/upload-connected-charities-schedule/success")

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/your-connected-charities-schedule-upload")
    }
  }

  "GET /upload-error" should {

    "render page with error code when upscan exists" in {
      stubBackend(withUpscan = true)

      val result = get("/upload-connected-charities-schedule/error?errorCode=EntityTooLarge")

      result.status shouldBe BAD_REQUEST
    }

    "redirect to upload page when upscan session missing" in {
      stubBackend()

      val result = get("/upload-connected-charities-schedule/error")

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-connected-charities-schedule")
    }
  }

  private def stubBackend(
                           withUpscan: Boolean = false,
                           withReference: Boolean = false
                         ): Unit = {

    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))

    val claimResponse =
      claim.copy(
        claimData = claim.claimData.copy(
          repaymentClaimDetails = claim.claimData.repaymentClaimDetails.copy(
            connectedToAnyOtherCharities = Some(true)
          ),
          connectedCharitiesScheduleFileUploadReference = if withReference then Some(connectedCharitiesFileRef) else None
        )
      )

    stubGetClaims(claimId)(OK, Json.toJson(claimResponse))

    stubGetUploadSummary(claimId)(
      OK,
      Json.toJson(uploadSummary(withUpscan))
    )

    if withReference then
      stubGetUploadResult(
        claimId,
        connectedCharitiesFileRef
      )(OK, getUploadResultConnectedCharitiesValidatedJson)

    if withUpscan then stubCreateUploadTracking(claimId)(OK, Json.toJson(SuccessResponse(success = true)))
  }

  private def uploadSummary(withUpscan: Boolean): GetUploadSummaryResponse =
    GetUploadSummaryResponse(
      uploads = Seq(
        UploadSummary(
          reference = FileUploadReference("test-reference"),
          validationType = ValidationType.ConnectedCharities,
          fileStatus = if withUpscan then FileStatus.AWAITING_UPLOAD else FileStatus.VALIDATED,
          uploadUrl = if withUpscan then Some("https://s3-bucket/upload") else None,
          fields = if withUpscan then Some(Map("key" -> "value")) else None
        )
      )
    )
}

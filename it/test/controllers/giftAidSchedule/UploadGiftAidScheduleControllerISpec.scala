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

package controllers.giftAidSchedule

import models.*
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub, UpscanInitiateStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class UploadGiftAidScheduleControllerISpec
    extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
      with AuthStub
      with ClaimsValidationStub
    with UpscanInitiateStub {

  private val url = "/upload-gift-aid-schedule?claimId=123"

  "GET /upload-gift-aid-schedule" should {

    "render the upload page when upscan is already initialised" in {
      stubBackend(withUpscan = true)

      val result = get(url)

      result.status                shouldBe OK
      Jsoup.parse(result.body).title should include(msg("uploadGiftAidSchedule.title"))
    }

    "render the page for Agent when upscan is already initialised" in {
      stubAgentBackend(withUpscan = true)

      val result = get(url)

      result.status shouldBe OK
      Jsoup.parse(result.body).text should include(msg("uploadGiftAidSchedule.agent.paragraph.one"))
    }


    "redirect to your upload page when file reference already exists" in {
      stubBackend(withReference = true)

      val result = get(url)

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/your-gift-aid-schedule-upload")
    }
  }

  "GET /upload-success" should {

    "update upload status and redirect to upload summary page" in {
      stubBackend(withUpscan = true)
      stubUpdateUploadStatus(claimId, "test-reference")(OK, Json.toJson(SuccessResponse(success = true)))

      val result = get("/upload-gift-aid-schedule/success")

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/your-gift-aid-schedule-upload")
    }
  }

  "GET /upload-error" should {

    "render page with error code when upscan exists" in {
      stubBackend(withUpscan = true)

      val result = get("/upload-gift-aid-schedule/error?errorCode=EntityTooLarge")

      result.status shouldBe BAD_REQUEST
    }

    "redirect to upload page when upscan session missing" in {
      stubBackend()

      val result = get("/upload-gift-aid-schedule/error")

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-gift-aid-schedule")
    }
  }

  private def stubAgentBackend(
                           withUpscan: Boolean = false,
                           withReference: Boolean = false
                         ): Unit = {

    stubAgentAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))

    val claimResponse =
      claim.copy(
        claimData = claim.claimData.copy(
          repaymentClaimDetails = claim.claimData.repaymentClaimDetails.copy(
            claimingGiftAid = true
          ),
          giftAidScheduleFileUploadReference = if withReference then Some(giftAidFileRef) else None
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
        giftAidFileRef
      )(OK, getUploadResultGiftAidValidatedJson)

    if withUpscan then stubCreateUploadTracking(claimId)(OK, Json.toJson(SuccessResponse(success = true)))
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
            claimingGiftAid = true
          ),
          giftAidScheduleFileUploadReference = if withReference then Some(giftAidFileRef) else None
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
        giftAidFileRef
      )(OK, getUploadResultGiftAidValidatedJson)

    if withUpscan then stubCreateUploadTracking(claimId)(OK, Json.toJson(SuccessResponse(success = true)))
  }

  private def uploadSummary(withUpscan: Boolean): GetUploadSummaryResponse =
    GetUploadSummaryResponse(
      uploads = Seq(
        UploadSummary(
          reference = FileUploadReference("test-reference"),
          validationType = ValidationType.GiftAid,
          fileStatus = if withUpscan then FileStatus.AWAITING_UPLOAD else FileStatus.VALIDATED,
          uploadUrl = if withUpscan then Some("https://s3-bucket/upload") else None,
          fields = if withUpscan then Some(Map("key" -> "value")) else None
        )
      )
    )
}

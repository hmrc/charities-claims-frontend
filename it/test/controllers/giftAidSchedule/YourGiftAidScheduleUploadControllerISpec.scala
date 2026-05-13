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
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class YourGiftAidScheduleUploadControllerISpec
    extends ComponentSpecHelper
    with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val pageUrl   = "/your-gift-aid-schedule-upload?claimId=123"
  private val removeUrl = "/your-gift-aid-schedule-upload/remove"
  private val submitUrl = "/your-gift-aid-schedule-upload"

  "GET /your-gift-aid-schedule-upload" should {

    "redirect to upload page when file reference missing" in {
      stubBackend(withReference = false)
      stubEmptyUploadSummary()

      val result = get(pageUrl)

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-gift-aid-schedule")
    }

    "render the page when upload result exists" in {
      stubBackend()
      stubUploadSummary()
      stubGetUploadResult(claimId, giftAidFileRef)(OK, Json.toJson(getUploadResultVerifying))

      val result = get(pageUrl)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("yourGiftAidScheduleUpload.title"))
    }

    "render the page for agents when upload result exists" in {
      stubAgentBackend()
      stubUploadSummary()
      stubGetUploadResult(claimId, giftAidFileRef)(OK, Json.toJson(getUploadResultVerifying))

      val result = get(pageUrl)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      Jsoup.parse(result.body).title should include(msg("yourGiftAidScheduleUpload.agent.title"))
      Jsoup.parse(result.body).text should include(msg("yourGiftAidScheduleUpload.agent.paragraph.one"))
    }
  }

  "GET /your-gift-aid-schedule-upload/remove" should {

    "delete upload and redirect to upload page" in {
      stubBackend()
      stubUploadSummary()
      stubDeleteSchedule(claimId, giftAidFileRef)(OK, Json.toJson(SuccessResponse(true)))
      stubUpdateClaim(claimId)(OK, Json.toJson(UpdateClaimResponse(success = true, claimId)))

      val result = get(removeUrl)

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-gift-aid-schedule")
    }
  }

  "POST /your-gift-aid-schedule-upload" should {

    "redirect to check page when upload validated" in {
      stubBackend()
      stubUploadSummary()
      stubGetUploadResult(claimId, giftAidFileRef)(OK, Json.toJson(getUploadResultGiftAidValidatedJson))

      val result = post(submitUrl)(Json.obj())

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/check-your-gift-aid-schedule")
    }
  }

  private def stubBackend(withReference: Boolean = true): Unit = {
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
  }

  private def stubAgentBackend(withReference: Boolean = true): Unit = {
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
  }

  private def stubUploadSummary(): Unit =
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryGiftAidValidatedResponse))

  private def stubEmptyUploadSummary(): Unit =
    stubGetUploadSummary(claimId)(OK, Json.toJson(GetUploadSummaryResponse(Seq.empty)))

  private val getUploadResultVerifying = Json.parse(
    """
      {
        "reference": "test-gift-aid-file-upload-ref",
        "validationType": "GiftAid",
        "fileStatus": "VERIFYING"
      }
    """
  )

}

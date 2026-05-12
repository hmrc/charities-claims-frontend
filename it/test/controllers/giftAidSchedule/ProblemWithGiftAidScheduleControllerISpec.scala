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

class ProblemWithGiftAidScheduleControllerISpec
    extends ComponentSpecHelper
    with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val pageUrl = "/problem-with-gift-aid-schedule?claimId=123"

  "GET /problem-with-gift-aid-schedule" should {

    "render the page when validation errors exist" in {
      stubBackend()
      stubClaimWithGiftAid()
      stubUploadSummary()
      stubValidationFailed()

      val result = get(pageUrl)

      result.status                shouldBe OK
      Jsoup.parse(result.body).title should include(msg("problemWithGiftAidSchedule.title"))
    }
    
    "render the agent page when validation errors exist" in {
      stubAgentBackend()
      stubClaimWithGiftAid()
      stubUploadSummary()
      stubValidationFailed()

      val result = get(pageUrl)

      result.status                shouldBe OK
      Jsoup.parse(result.body).title should include(msg("problemWithGiftAidSchedule.agent.title"))
      Jsoup.parse(result.body).text should include(msg("problemWithGiftAidSchedule.agent.heading"))
      Jsoup.parse(result.body).text should include(msg("problemWithGiftAidSchedule.agent.list.item.3"))
    }

    "redirect to upload summary when upload result is not validation failed" in {
      stubBackend()
      stubClaimWithGiftAid()
      stubUploadSummary()
      stubValidatedResult()

      val result = get(pageUrl)

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/your-gift-aid-schedule-upload")
    }
  }

  "POST /problem-with-gift-aid-schedule" should {

    "delete schedule and redirect to upload page" in {
      stubBackend()
      stubClaimWithGiftAid()
      stubUploadSummary()
      stubValidationFailed()

      stubDeleteSchedule(claimId, giftAidFileRef)(
        OK,
        Json.toJson(DeleteScheduleResponse(success = true))
      )
      stubUpdateClaim(claimId)(OK, Json.toJson(UpdateClaimResponse(success = true, claimId)))

      val result = post(pageUrl)(Json.obj())

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-gift-aid-schedule")
    }
  }

  private def stubBackend(): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubUpdateClaim(claimId)(OK, Json.toJson(UpdateClaimResponse(success = true, claimId)))
  }

  private def stubAgentBackend(): Unit = {
    stubAgentAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubUpdateClaim(claimId)(OK, Json.toJson(UpdateClaimResponse(success = true, claimId)))
  }

  private def stubUploadSummary(): Unit =
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryGiftAidValidatedResponse))

  private def stubClaimWithGiftAid(): Unit =
    stubGetClaims(claimId)(OK, Json.toJson(claimWithGiftAid))

  private def stubValidationFailed(): Unit =
    stubGetUploadResult(claimId, giftAidFileRef)(OK, validationFailedJson)

  private def stubValidatedResult(): Unit =
    stubGetUploadResult(claimId, giftAidFileRef)(OK, getUploadResultGiftAidValidatedJson)

  private val validationFailedJson = Json.parse(
    s"""
      {
        "reference": "$giftAidFileRef",
        "validationType": "GiftAid",
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

  private def claimWithGiftAid: Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        repaymentClaimDetails = claim.claimData.repaymentClaimDetails.copy(claimingGiftAid = true),
        giftAidScheduleFileUploadReference = Some(giftAidFileRef)
      )
    )
}

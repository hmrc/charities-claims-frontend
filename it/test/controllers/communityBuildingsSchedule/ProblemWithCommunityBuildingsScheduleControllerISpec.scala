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

package controllers.communityBuildingsSchedule

import models.*
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class ProblemWithCommunityBuildingsScheduleControllerISpec
    extends ComponentSpecHelper
    with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val pageUrl = "/problem-with-community-buildings-schedule"

  "GET /problem-with-community-buildings-schedule" should {

    "render the page when validation errors exist" in {
      stubBackend()
      stubClaimWithCommunityBuildings()
      stubUploadSummary()
      stubValidationFailed()

      val result = get(pageUrl)

      result.status                shouldBe OK
      Jsoup.parse(result.body).title should include(msg("problemWithCommunityBuildingsSchedule.title"))
    }

    "redirect to upload summary when upload result is not validation failed" in {
      stubBackend()
      stubClaimWithCommunityBuildings()
      stubUploadSummary()
      stubValidatedResult()

      val result = get(pageUrl)

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/your-community-buildings-schedule-upload")
    }
  }

  "POST /problem-with-community-buildings-schedule" should {

    "delete schedule and redirect to upload page" in {
      stubBackend()
      stubClaimWithCommunityBuildings()
      stubUploadSummary()
      stubValidationFailed()

      stubDeleteSchedule(claimId, communityBuildingsFileRef)(
        OK,
        Json.toJson(DeleteScheduleResponse(success = true))
      )

      val result = post(pageUrl)(Json.obj())

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-community-buildings-schedule")
    }
  }

  private def stubBackend(): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
  }

  private def stubUploadSummary(): Unit =
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryCommunityBuildingsValidatedResponse))

  private def stubClaimWithCommunityBuildings(): Unit =
    stubGetClaims(claimId)(OK, Json.toJson(claimWithCommunityBuildings))

  private def stubValidationFailed(): Unit =
    stubGetUploadResult(claimId, communityBuildingsFileRef)(OK, validationFailedJson)

  private def stubValidatedResult(): Unit =
    stubGetUploadResult(claimId, communityBuildingsFileRef)(OK, getUploadResultCommunityBuildingsValidatedJson)

  private val validationFailedJson = Json.parse(
    s"""
      {
        "reference": "$communityBuildingsFileRef",
        "validationType": "CommunityBuildings",
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

  private def claimWithCommunityBuildings: Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        repaymentClaimDetails = claim.claimData.repaymentClaimDetails.copy(
          claimingDonationsCollectedInCommunityBuildings = Some(true),
          claimingUnderGiftAidSmallDonationsScheme = true,
          claimingGiftAid = false,
          claimingDonationsNotFromCommunityBuilding = Some(false),
          connectedToAnyOtherCharities = Some(false),
          makingAdjustmentToPreviousClaim = Some(false)
        ),
        communityBuildingsScheduleFileUploadReference = Some(communityBuildingsFileRef)
      )
    )
}

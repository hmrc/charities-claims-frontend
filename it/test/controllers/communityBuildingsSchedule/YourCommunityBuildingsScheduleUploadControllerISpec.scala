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

class YourCommunityBuildingsScheduleUploadControllerISpec
    extends ComponentSpecHelper
    with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val pageUrl   = "/your-community-buildings-schedule-upload?claimId=123"
  private val removeUrl = "/your-community-buildings-schedule-upload/remove"
  private val submitUrl = "/your-community-buildings-schedule-upload"

  "GET /your-community-buildings-schedule-upload" should {

    "redirect to upload page when file reference missing" in {
      stubBackend(withReference = false)
      stubEmptyUploadSummary()

      val result = get(pageUrl)

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-community-buildings-schedule")
    }

    "render the page when upload result exists" in {
      stubBackend()
      stubUploadSummary()
      stubGetUploadResult(claimId, communityBuildingsFileRef)(OK, Json.toJson(getUploadResultVerifying))

      val result = get(pageUrl)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("yourCommunityBuildingsScheduleUpload.title"))
    }
    
    "render the agent page when upload result exists" in {
      stubAgentBackend()
      stubUploadSummary()
      stubGetUploadResult(claimId, communityBuildingsFileRef)(OK, Json.toJson(getUploadResultVerifying))

      val result = get(pageUrl)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("yourCommunityBuildingsScheduleUpload.agent.title"))
      doc.text should include(msg("yourCommunityBuildingsScheduleUpload.agent.heading"))
    }
  }

  "GET /your-community-buildings-schedule-upload/remove" should {

    "delete upload and redirect to upload page" in {
      stubBackend()
      stubUploadSummary()
      stubDeleteSchedule(claimId, communityBuildingsFileRef)(OK, Json.toJson(SuccessResponse(true)))
      stubUpdateClaim(claimId)(OK, Json.toJson(UpdateClaimResponse(success = true, claimId)))

      val result = get(removeUrl)

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-community-buildings-schedule")
    }
  }

  "POST /your-community-buildings-schedule-upload" should {

    "redirect to check page when upload validated" in {
      stubBackend()
      stubUploadSummary()
      stubGetUploadResult(claimId, communityBuildingsFileRef)(
        OK,
        Json.toJson(getUploadResultCommunityBuildingsValidatedJson)
      )

      val result = post(submitUrl)(Json.obj())

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/check-your-community-buildings-schedule")
    }
  }

  private def stubBackend(withReference: Boolean = true): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))

    val claimResponse =
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
          communityBuildingsScheduleFileUploadReference =
            if withReference then Some(communityBuildingsFileRef) else None
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
            claimingDonationsCollectedInCommunityBuildings = Some(true),
            claimingUnderGiftAidSmallDonationsScheme = true,
            claimingGiftAid = false,
            claimingDonationsNotFromCommunityBuilding = Some(false),
            connectedToAnyOtherCharities = Some(false),
            makingAdjustmentToPreviousClaim = Some(false)
          ),
          communityBuildingsScheduleFileUploadReference =
            if withReference then Some(communityBuildingsFileRef) else None
        )
      )

    stubGetClaims(claimId)(OK, Json.toJson(claimResponse))
  }

  private def stubUploadSummary(): Unit =
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryCommunityBuildingsValidatedResponse))

  private def stubEmptyUploadSummary(): Unit =
    stubGetUploadSummary(claimId)(OK, Json.toJson(GetUploadSummaryResponse(Seq.empty)))

  private val getUploadResultVerifying = Json.parse(
    """
      {
        "reference": "test-community-buildings-file-upload-ref",
        "validationType": "CommunityBuildings",
        "fileStatus": "VERIFYING"
      }
    """
  )

}

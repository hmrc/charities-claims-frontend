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

import models.Claim
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class CommunityBuildingsScheduleUploadStatusControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with AuthStub
    with ClaimsStub
    with ClaimsValidationStub {

  private val url = "/community-buildings-schedule-upload-status"

  "GET /community-buildings-schedule-upload-status" should {

    "return upload status when upload result is available" in {
      stubBackend()
      stubGetUploadResult(claimId, communityBuildingsFileRef)(OK, getUploadResultCommunityBuildingsValidatedJson)

      val result = get(url)

      result.status shouldBe OK
      (result.json \ "isFinal").as[Boolean] shouldBe true
    }

    "return BAD_REQUEST when backend returns CLAIM_REFERENCE_DOES_NOT_EXIST" in {
      stubBackend()

      stubGetUploadResult(claimId, communityBuildingsFileRef)(BAD_REQUEST, Json.obj("message" -> "CLAIM_REFERENCE_DOES_NOT_EXIST"))

      val result = get(url)

      result.status shouldBe BAD_REQUEST
    }

    "return INTERNAL_SERVER_ERROR when backend throws other error" in {
      stubBackend()

      stubGetUploadResult(claimId, communityBuildingsFileRef)(INTERNAL_SERVER_ERROR, Json.obj())

      val result = get(url)

      result.status shouldBe INTERNAL_SERVER_ERROR
    }
  }

  private def stubBackend(): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(claimWithCommunityBuildingsSchedule))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryCommunityBuildingsValidatedResponse))
  }

  private def claimWithCommunityBuildingsSchedule: Claim =
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

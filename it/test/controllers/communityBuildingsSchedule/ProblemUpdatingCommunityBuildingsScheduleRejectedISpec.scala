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
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class ProblemUpdatingCommunityBuildingsScheduleRejectedISpec
  extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val url = "/problem-uploading-community-buildings-schedule-rejected"

  "GET /problem-uploading-community-buildings-schedule-rejected" should {

    "render the rejected failure page" in {
      stubBackend()

      val result = get(url)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("problemUpdatingCommunityBuildingsScheduleRejected.title"))
    }
  }

  "POST /problem-uploading-community-buildings-schedule-rejected" should {

    "redirect to upload community buildings schedule page" in {
      stubBackend()

      val result = post(url)(Json.obj())

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-community-buildings-schedule")
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

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

import models.{Claim, FileUploadReference}
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class AboutCommunityBuildingsScheduleControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with AuthStub
    with ClaimsStub
    with ClaimsValidationStub {

  private val url = "/about-community-buildings-schedule"

  "GET /about-community-buildings-schedule" should {

    "render the page when claiming community buildings and schedule not completed" in {
      stubBackend(claimWithCommunityBuildings())

      val result = get(url)

      result.status shouldBe OK
      Jsoup.parse(result.body).title should include(msg("aboutCommunityBuildingsSchedule.title"))
    }

    "redirect to upload summary when schedule already completed" in {
      stubBackend(claimWithUploadedSchedule)

      val result = get(url)

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/your-community-buildings-schedule-upload")
    }

    "redirect to task list page triggered by data guard when not claiming community buildings" in {
      stubBackend(claimWithoutCommunityBuildings)

      val result = get(url)

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/charities-claims/make-a-charity-repayment-claim")
    }
  }

  "POST /about-community-buildings-schedule" should {

    "redirect to upload community buildings schedule page" in {
      stubBackend(claimWithCommunityBuildings())

      val result = post(url)(Json.obj())

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-community-buildings-schedule")
    }
  }

  private def stubBackend(testClaim: Claim): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(testClaim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryCommunityBuildingsResponse))
  }

  private def claimWithCommunityBuildings(fileRef: Option[FileUploadReference] = None): Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        repaymentClaimDetails =
          claim.claimData.repaymentClaimDetails.copy(
            claimingDonationsCollectedInCommunityBuildings = Some(true),
            claimingUnderGiftAidSmallDonationsScheme = true,
            claimingGiftAid = false,
            claimingDonationsNotFromCommunityBuilding = Some(false),
            connectedToAnyOtherCharities = Some(false),
            makingAdjustmentToPreviousClaim = Some(false)
          ),
        communityBuildingsScheduleFileUploadReference = fileRef
      )
    )

  private def claimWithUploadedSchedule: Claim =
    claimWithCommunityBuildings(Some(FileUploadReference("test-community-buildings-file-upload-ref")))

  private def claimWithoutCommunityBuildings: Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        repaymentClaimDetails =
          claim.claimData.repaymentClaimDetails.copy(
            claimingDonationsCollectedInCommunityBuildings = Some(false),
            claimingUnderGiftAidSmallDonationsScheme = true,
            claimingGiftAid = false,
            claimingDonationsNotFromCommunityBuilding = Some(false),
            connectedToAnyOtherCharities = Some(true),
            makingAdjustmentToPreviousClaim = Some(false)
          )
      )
    )
}

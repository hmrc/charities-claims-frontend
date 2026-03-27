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

class CheckYourCommunityBuildingsScheduleControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with AuthStub
    with ClaimsValidationStub
    with ClaimsStub {

  private val url = "/check-your-community-buildings-schedule"

  "GET /check-your-community-buildings-schedule" should {

    "render the check your community buildings schedule page" in {
      stubBackend()

      val result = get(url)

      result.status shouldBe OK
      Jsoup.parse(result.body).title should include(msg("checkYourCommunityBuildingsSchedule.title"))
    }
  }

  "POST /check-your-community-buildings-schedule" should {

    "return BAD_REQUEST when form submission invalid" in {
      stubBackend()

      val result = post(url)(Json.obj())

      result.status shouldBe BAD_REQUEST
    }

    "redirect to update community buildings schedule when answer is Yes" in {
      stubBackend()

      val result = post(url)(Json.obj("value" -> true))

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/update-community-buildings-schedule")
    }

    "redirect to task list when answer is No and schedule already completed" in {
      stubBackend()

      val result = post(url)(Json.obj("value" -> false))

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/make-a-charity-repayment-claim")
    }

    "save data and redirect to success page when answer is No and schedule not completed" in {
      stubBackend(fileRefOpt = None)
      stubUpdateClaim(claimId)(OK, Json.toJson(UpdateClaimResponse(true, communityBuildingsFileRef)))
      val result = post(url)(Json.obj("value" -> false))

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/community-buildings-schedule-upload-successful")
    }
  }

  private def stubBackend(fileRefOpt: Option[FileUploadReference] = Some(communityBuildingsFileRef)): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(
      claimId
    )(OK, Json.toJson(claimWithCommunityBuildings(fileRefOpt)))

    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryCommunityBuildingsValidatedResponse))
    stubGetUploadResult(claimId, communityBuildingsFileRef)(OK, getUploadResultCommunityBuildingsValidatedJson)
  }

  private def claimWithCommunityBuildings(fileRef: Option[FileUploadReference]): Claim =
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
        communityBuildingsScheduleFileUploadReference = fileRef
      )
    )

}

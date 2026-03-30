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

import models.{Claim, FileUploadReference}
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class AboutGiftAidScheduleControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val url = "/about-gift-aid-schedule"

  "GET /about-gift-aid-schedule" should {

    "render the page when claiming gift aid and schedule not completed" in {
      stubBackend(claimWithGiftAid())

      val result = get(url)

      result.status shouldBe OK
      Jsoup.parse(result.body).title should include(msg("aboutGiftAidSchedule.title"))
    }

    "redirect to upload summary when schedule already completed" in {
      stubBackend(claimWithUploadedSchedule)

      val result = get(url)

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/your-gift-aid-schedule-upload")
    }

    "redirect to task list page triggered by data guard when not claiming gift aid" in {
      stubBackend(claimWithoutGiftAid)

      val result = get(url)

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/charities-claims/make-a-charity-repayment-claim")
    }
  }

  "POST /about-gift-aid-schedule" should {

    "redirect to upload gift aid schedule page" in {
      stubBackend(claimWithGiftAid())

      val result = post(url)(Json.obj())

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-gift-aid-schedule")
    }
  }

  private def stubBackend(testClaim: Claim): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(testClaim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
  }

  private def claimWithGiftAid(fileRef: Option[FileUploadReference] = None): Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        repaymentClaimDetails =
          claim.claimData.repaymentClaimDetails.copy(
            claimingGiftAid = true
          ),
        giftAidScheduleFileUploadReference = fileRef
      )
    )

  private def claimWithUploadedSchedule: Claim =
    claimWithGiftAid(Some(FileUploadReference("test-gift-aid-file-upload-ref")))

  private def claimWithoutGiftAid: Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        repaymentClaimDetails =
          claim.claimData.repaymentClaimDetails.copy(
            claimingGiftAid = false
          )
      )
    )
}

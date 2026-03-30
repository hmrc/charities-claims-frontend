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

package controllers.otherIncomeSchedule

import models.{Claim, FileUploadReference}
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class AboutOtherIncomeScheduleControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val url = "/about-other-income-schedule"

  "GET /about-other-income-schedule" should {

    "render the page when claiming tax deducted and schedule not completed" in {
      stubBackend(claimWithTaxDeducted())

      val result = get(url)

      result.status shouldBe OK
      Jsoup.parse(result.body).title should include(msg("aboutOtherIncomeSchedule.title"))
    }

    "redirect to upload summary when schedule already completed" in {
      stubBackend(claimWithUploadedSchedule)

      val result = get(url)

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/your-other-income-schedule-upload")
    }

    "redirect to task list page triggered by data guard when not claiming tax deducted" in {
      stubBackend(claimWithoutTaxDeducted)

      val result = get(url)

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/charities-claims/make-a-charity-repayment-claim")
    }
  }

  "POST /about-other-income-schedule" should {

    "redirect to upload other income schedule page" in {
      stubBackend(claimWithTaxDeducted())

      val result = post(url)(Json.obj())

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-other-income-schedule")
    }
  }

  private def stubBackend(testClaim: Claim): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(testClaim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
  }

  private def claimWithTaxDeducted(fileRef: Option[FileUploadReference] = None): Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        repaymentClaimDetails =
          claim.claimData.repaymentClaimDetails.copy(
            claimingTaxDeducted = true
          ),
        otherIncomeScheduleFileUploadReference = fileRef
      )
    )

  private def claimWithUploadedSchedule: Claim =
    claimWithTaxDeducted(Some(FileUploadReference("test-other-income-file-upload-ref")))

  private def claimWithoutTaxDeducted: Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        repaymentClaimDetails =
          claim.claimData.repaymentClaimDetails.copy(
            claimingTaxDeducted = false
          )
      )
    )
}

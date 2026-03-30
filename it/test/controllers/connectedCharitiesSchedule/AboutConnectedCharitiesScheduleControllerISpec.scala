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

package controllers.connectedCharitiesSchedule

import models.{Claim, FileUploadReference}
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class AboutConnectedCharitiesScheduleControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val url = "/about-connected-charities-schedule"

  "GET /about-connected-charities-schedule" should {

    "render the page when claiming connected charities and schedule not completed" in {
      stubBackend(claimWithConnectedCharities())

      val result = get(url)

      result.status shouldBe OK
      Jsoup.parse(result.body).title should include(msg("aboutConnectedCharitiesSchedule.title"))
    }

    "redirect to upload summary when schedule already completed" in {
      stubBackend(claimWithUploadedSchedule)

      val result = get(url)

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/your-connected-charities-schedule-upload")
    }

    "redirect to task list page triggered by data guard when not claiming connected charities" in {
      stubBackend(claimWithoutConnectedCharities)

      val result = get(url)

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/charities-claims/make-a-charity-repayment-claim")
    }
  }

  "POST /about-connected-charities-schedule" should {

    "redirect to upload connected charities schedule page" in {
      stubBackend(claimWithConnectedCharities())

      val result = post(url)(Json.obj())

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-connected-charities-schedule")
    }
  }

  private def stubBackend(testClaim: Claim): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(testClaim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
  }

  private def claimWithConnectedCharities(fileRef: Option[FileUploadReference] = None): Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        repaymentClaimDetails =
          claim.claimData.repaymentClaimDetails.copy(
            connectedToAnyOtherCharities = Some(true)
          ),
        connectedCharitiesScheduleFileUploadReference = fileRef
      )
    )

  private def claimWithUploadedSchedule: Claim =
    claimWithConnectedCharities(Some(connectedCharitiesFileRef))

  private def claimWithoutConnectedCharities: Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        repaymentClaimDetails =
          claim.claimData.repaymentClaimDetails.copy(
            connectedToAnyOtherCharities = Some(false)
          )
      )
    )
}

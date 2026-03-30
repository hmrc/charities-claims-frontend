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

class ProblemUpdatingCCScheduleQuarantineControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val url = "/problem-uploading-connected-charities-schedule-quarantine"

  "GET /problem-uploading-connected-charities-schedule-quarantine" should {

    "render the quarantine failure page" in {
      stubBackend()

      val result = get(url)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("problemUpdatingConnectedCharitiesScheduleQuarantine.title"))
    }
  }

  "POST /problem-uploading-connected-charities-schedule-quarantine" should {

    "redirect to upload connected charities schedule page" in {
      stubBackend()

      val result = post(url)(Json.obj())

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-connected-charities-schedule")
    }
  }

  private def stubBackend(): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(claimWithConnectedCharitiesSchedule))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryConnectedCharitiesValidatedResponse))
  }

  private def claimWithConnectedCharitiesSchedule: Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        repaymentClaimDetails =
          claim.claimData.repaymentClaimDetails.copy(
            connectedToAnyOtherCharities = Some(true)
          ),
        connectedCharitiesScheduleFileUploadReference = Some(connectedCharitiesFileRef)
      )
    )
}

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

import models.*
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class UpdateConnectedCharitiesControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val url = "/update-connected-charities-schedule"

  "GET /update-connected-charities-schedule" should {

    "render the update connected charities schedule page" in {
      stubBackend()

      val result = get(url)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("updateConnectedCharitiesSchedule.title"))
    }
  }

  "POST /update-connected-charities-schedule" should {

    "return BAD_REQUEST when form submission invalid" in {
      stubBackend()

      val result = post(url)(Json.obj())

      result.status shouldBe BAD_REQUEST
    }

    "delete schedule and redirect to upload page when answer is Yes" in {
      stubBackend()

      val deleteResponse = DeleteScheduleResponse(success = true)
      stubDeleteSchedule(claimId, connectedCharitiesFileRef)(OK, Json.toJson(deleteResponse))

      val updateResponse = UpdateClaimResponse(true, claimId)
      stubUpdateClaim(claimId)(OK, Json.toJson(updateResponse))

      val result = post(url)(Json.obj("value" -> true))

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/upload-connected-charities-schedule")
    }

    "redirect to check your connected charities schedule page when answer is No" in {
      stubBackend()

      val result = post(url)(Json.obj("value" -> false))

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/check-your-connected-charities-schedule")
    }
  }

  private def stubBackend(): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))

    stubGetClaims(
      claimId
    )(OK, Json.toJson(claimWithConnectedCharities))

    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryConnectedCharitiesValidatedResponse))
  }

  private def claimWithConnectedCharities: Claim =
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

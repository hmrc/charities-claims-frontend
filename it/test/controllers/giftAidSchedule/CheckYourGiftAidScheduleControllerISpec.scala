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

import models.*
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class CheckYourGiftAidScheduleControllerISpec
    extends ComponentSpecHelper with TestDataUtils
      with AuthStub
      with ClaimsValidationStub
    with ClaimsStub {

  private val url     = "/check-your-gift-aid-schedule"

  "GET /check-your-gift-aid-schedule" should {

    "render the check your gift aid schedule page" in {
      stubBackend()

      val result = get(url)

      result.status                shouldBe OK
      Jsoup.parse(result.body).title should include(msg("checkYourGiftAidSchedule.title"))
    }
  }

  "POST /check-your-gift-aid-schedule" should {

    "return BAD_REQUEST when form submission invalid" in {
      stubBackend()

      val result = post(url)(Json.obj())

      result.status shouldBe BAD_REQUEST
    }

    "redirect to update gift aid schedule when answer is Yes" in {
      stubBackend()

      val result = post(url)(Json.obj("value" -> true))

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/update-gift-aid-schedule")
    }

    "redirect to task list when answer is No and schedule already completed" in {
      stubBackend()

      val result = post(url)(Json.obj("value" -> false))

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/make-a-charity-repayment-claim")
    }

    "save data and redirect to success page when answer is No and schedule not completed" in {
      stubBackend(fileRefOpt = None)
      stubUpdateClaim(claimId)(OK, Json.toJson(UpdateClaimResponse(true, giftAidFileRef)))
      val result = post(url)(Json.obj("value" -> false))

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/gift-aid-schedule-upload-successful")
    }
  }

  private def stubBackend(fileRefOpt: Option[FileUploadReference] = Some(giftAidFileRef)): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(
      claimId
    )(OK, Json.toJson(claimWithGiftAid(fileRefOpt)))

    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryGiftAidValidatedResponse))
    stubGetUploadResult(claimId, giftAidFileRef)(OK, getUploadResultGiftAidValidatedJson)
  }

  private def claimWithGiftAid(fileRef: Option[FileUploadReference]): Claim =
    claim.copy(
      claimData = claim.claimData.copy(
        repaymentClaimDetails = claim.claimData.repaymentClaimDetails.copy(
          claimingGiftAid = true
        ),
        giftAidScheduleFileUploadReference = fileRef
      )
    )
  
}

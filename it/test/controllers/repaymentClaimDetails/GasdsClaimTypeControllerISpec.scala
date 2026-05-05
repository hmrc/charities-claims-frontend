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

package controllers.repaymentClaimDetails

import models.Mode.NormalMode
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.http.HeaderNames.LOCATION
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class GasdsClaimTypeControllerISpec
  extends ComponentSpecHelper
    with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {

  private val url = "/select-gift-aid-small-donations-scheme-claim-type"

  "GET /select-gift-aid-small-donations-scheme-claim-type" should {

    "render the GASDS claim type page" in {
      val updatedClaim =
        claim.copy(
          claimData = claim.claimData.copy(
            repaymentClaimDetails =
              claim.claimData.repaymentClaimDetails.copy(
                claimingUnderGiftAidSmallDonationsScheme = true
              )
          )
        )
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(updatedClaim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result = get(url)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title() should include(msg("gasdsClaimType.title"))
    }
  }

  "POST /select-gift-aid-small-donations-scheme-claim-type" should {

    "redirect to change previous gasds claim page when connected charities NOT selected" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result = post(url)(
        Map("value" -> Seq("communityBuildings"))
      )

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value shouldBe routes.ChangePreviousGASDSClaimController.onPageLoad(NormalMode).url
    }
  }
}

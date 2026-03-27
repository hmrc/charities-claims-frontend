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

import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.http.HeaderNames.LOCATION
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class ClaimGiftAidSmallDonationsSchemeControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with AuthStub
    with ClaimsStub
    with ClaimsValidationStub {

  "GET /claim-gasds-payment" should {

    "render the GASDS page when guard condition is satisfied" in {
      stubBackend()

      val result = get("/claim-gasds-payment")

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("claimGASDS.title"))
    }
  }

  "POST /claim-gasds-payment" should {

    "redirect to community building donations page when user submits valid answer" in {

      stubBackend()

      val result =
        post("/claim-gasds-payment")(
          Json.obj("value" -> true)
        )

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/charities-claims/claim-community-building-donations")
    }

    "return BadRequest when form submission is invalid" in {

      stubBackend()

      val result =
        post("/claim-gasds-payment")(Json.obj())

      result.status shouldBe BAD_REQUEST
    }

    "render update confirmation page when answer changes in CheckMode" in {

      stubBackend()

      val result =
        post("/change-claim-gasds-payment")(
          Json.obj("value" -> false)
        )

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("updateRepaymentClaim.title"))
    }
  }

  private def stubBackend(): Unit = {
    stubAuthRequest()
    val updatedClaim =
      claim.copy(
        claimData = claim.claimData.copy(
          repaymentClaimDetails =
            claim.claimData.repaymentClaimDetails.copy(
              claimingUnderGiftAidSmallDonationsScheme = true,
              claimingDonationsNotFromCommunityBuilding = Some(true),
              claimingGiftAid = false
            )
        )
      )
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(updatedClaim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
  }
}
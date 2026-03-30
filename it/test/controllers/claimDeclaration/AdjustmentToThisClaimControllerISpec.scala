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

package controllers.claimDeclaration

import models.*
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.http.HeaderNames.LOCATION
import play.api.libs.json.Json
import play.api.test.Helpers.*
import repositories.SessionCache
import stubs.{ClaimsStub, ClaimsValidationStub, AuthStub}
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import utils.{ComponentSpecHelper, TestDataUtils}

class AdjustmentToThisClaimControllerISpec extends ComponentSpecHelper with TestDataUtils with ClaimsStub with AuthStub with ClaimsValidationStub {
  lazy val sessionCache: SessionCache              = app.injector.instanceOf[SessionCache]
  private val getTotalUnregulatedDonationsResponse = GetTotalUnregulatedDonationsResponse(BigDecimal(6000))

  "GET /adjustment-to-this-claim" should {

    "render the adjustment page when no unregulated limit exceeded" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
      val result = get("/adjustments-to-this-claim")

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title() should include("What adjustments have you made to this claim?")
    }

    "redirect to register charity page when unregulated limit exceeded" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      val claimWithLimit =
        claim.copy(
          claimData = claim.claimData.copy(
            organisationDetails = claim.claimData.organisationDetails.map(
              _.copy(
                reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.LowIncome)
              )
            )
          )
        )

      stubGetClaims(claimId)(OK, Json.toJson(claimWithLimit))
      stubGetTotalUnregulatedDonations("1234567890")(OK, Json.toJson(getTotalUnregulatedDonationsResponse))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
      val result = get("/adjustments-to-this-claim")

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/registering-your-charity-with-a-regulator")
    }
  }

  "POST /adjustments-to-this-claim" should {

    "return BadRequest when form submission is invalid" in {
      val claimWithAdjustments =
        claim.copy(
          claimData = claim.claimData.copy(
            prevOverclaimedGiftAid = Some(BigDecimal(100))
          )
        )
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claimWithAdjustments))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
      stubUpdateClaim(claimId)(OK, Json.toJson(updateClaimResponse))
      val result               = post("/adjustments-to-this-claim")(Json.obj())

      result.status shouldBe BAD_REQUEST
    }

    "save adjustments and redirect to declaration page when valid" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
      stubUpdateClaim(claimId)(OK, Json.toJson(updateClaimResponse))
      val result =
        post("/adjustments-to-this-claim")(
          Json.obj("value" -> "Test adjustment reason")
        )

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/declaration")

      given HeaderCarrier =
        HeaderCarrier(sessionId = Some(SessionId("mock-sessionid")))

      val cached = await(sessionCache.get())

      cached.value.includedAnyAdjustmentsInClaimPrompt shouldBe
        Some("Test adjustment reason")
    }
  }
}

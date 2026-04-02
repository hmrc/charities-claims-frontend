/*
 * Copyright 2024 HM Revenue & Customs
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
import play.api.http.Status.SEE_OTHER
import play.api.libs.json.Json
import play.api.test.Helpers.{OK, await, defaultAwaitTimeout}
import repositories.SessionCache
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import utils.{ComponentSpecHelper, TestDataUtils}

class ClaimDeclarationControllerISpec extends ComponentSpecHelper with TestDataUtils with AuthStub with ClaimsStub with ClaimsValidationStub {
  lazy val sessionCache: SessionCache =
    app.injector.instanceOf[SessionCache]

  "GET /declaration"  should {
    "render the declaration page" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))

      val result = get("/declaration")
      val doc    = Jsoup.parse(result.body)
      result.status shouldBe OK
      doc.title       should include("Declaration")
    }

    "redirect when adjustments exist but prompt not answered" in {
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
      val result               = get("/declaration")

      result.status               shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/make-a-charity-repayment-claim")
    }
  }
  "POST /declaration" should {
    "save declaration confirmation and update session cache" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.toJson(claim))
      val updateResponse = UpdateClaimResponse(true, claimId)
      stubUpdateClaim(claimId)(OK, Json.toJson(updateResponse))
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
      stubChrisSubmission(OK, Json.toJson(SubmitClaimResponse(success = true)))

      val result = post("/declaration")(Json.obj())

      result.status shouldBe SEE_OTHER

      given HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("mock-sessionid")))
      val cached          = await(sessionCache.get())

      cached.value.understandFalseStatements shouldBe Some(true)
    }
  }
}

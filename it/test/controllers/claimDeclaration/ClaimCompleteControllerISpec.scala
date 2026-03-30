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
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import org.jsoup.Jsoup
import play.api.libs.json.Json
import play.api.test.Helpers.OK
import repositories.SessionCache
import utils.{ComponentSpecHelper, TestDataUtils}

class ClaimCompleteControllerISpec extends ComponentSpecHelper with TestDataUtils with AuthStub with ClaimsStub with ClaimsValidationStub {
  lazy val sessionCache: SessionCache =
    app.injector.instanceOf[SessionCache]

  private val url = "/claim-complete"

  "GET /claim-complete"  should {
    "render the claim complete page" in {
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

      val result = get(url)
      val doc    = Jsoup.parse(result.body)
      result.status shouldBe OK
      doc.title       should include(msg("claimComplete.title"))
    }
  }
}

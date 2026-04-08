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
import play.api.libs.json.Json
import play.api.test.Helpers.OK
import repositories.SessionCache
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import utils.{ComponentSpecHelper, TestDataUtils}

class RepaymentClaimSummaryControllerISpec
    extends ComponentSpecHelper
    with TestDataUtils
    with AuthStub
    with ClaimsStub
    with ClaimsValidationStub {
  lazy val sessionCache: SessionCache =
    app.injector.instanceOf[SessionCache]

  "GET /charity-repayment-claim-summary" should {
    "render the charity repayment claim summary page" in {
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(
        OK,
        Json.toJson(claim.copy(claimSubmitted = true, submissionDetails = Some(SubmissionDetails("", "sub ref"))))
      )
      stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
      stubGetSubmissionSummary(claimId)(OK, Json.toJson(submissionSummaryResponse))

      val result = get("/charity-repayment-claim-summary")
      result.status                shouldBe OK
      Jsoup.parse(result.body).title should include(msg("repaymentClaimSummary.title"))
    }
  }
}

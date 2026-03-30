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

package controllers.organisationDetails

import models.{DeleteClaimResponse, GetClaimsResponse}
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.Json
import play.api.test.Helpers.*
import repositories.SessionCache
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import utils.{ComponentSpecHelper, TestDataUtils}

class DeleteRepaymentClaimControllerISpec
  extends ComponentSpecHelper with TestDataUtils
    with ClaimsStub
    with AuthStub
    with ClaimsValidationStub {
  lazy val sessionCache: SessionCache = app.injector.instanceOf[SessionCache]

  private val url = "/delete-repayment-claim"

  "GET /delete-repayment-claim" should {

    "render the delete repayment claim page when session has claimId" in {
      stubBackend()

      val result = get(url)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("deleteRepaymentClaim.title"))
    }

    "redirect to dashboard when no unsubmitted claim exists" in {
      val getClaimsResponse = GetClaimsResponse(
        claimsCount = 0,
        claimsList = List()
      )
      stubAuthRequest()
      stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
      stubGetClaims(claimId)(OK, Json.obj())
      stubGetUploadSummary(claimId)(OK, Json.obj())

      val result = get(url)

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/charity-repayment-dashboard")
    }
  }

  "POST /delete-repayment-claim" should {

    "return BAD_REQUEST when form submission invalid" in {
      stubBackend()

      val result = post(url)(Json.obj())

      result.status shouldBe BAD_REQUEST
    }

    "redirect to task list when user selects No" in {
      stubBackend()

      val result =
        post(url)(
          Json.obj("value" -> false)
        )

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/make-a-charity-repayment-claim")
    }

    "delete claim and redirect to dashboard when user selects Yes" in {
      val deleteClaimResponse = DeleteClaimResponse(success = true)
      stubBackend()
      stubDeleteClaim(claimId)(OK, Json.toJson(deleteClaimResponse))

      val result =
        post(url)(
          Json.obj("value" -> true)
        )

      given HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("mock-sessionid")))

      val cached = await(sessionCache.get())
      cached.value.unsubmittedClaimId shouldBe None
      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value should include("/charity-repayment-dashboard")
    }
  }

  private def stubBackend(): Unit = {
    stubAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(claim))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
  }
}

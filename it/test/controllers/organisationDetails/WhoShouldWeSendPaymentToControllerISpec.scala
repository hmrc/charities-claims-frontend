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

import models.Mode.NormalMode
import models.{Claim, WhoShouldHmrcSendPaymentTo}
import org.jsoup.Jsoup
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.http.HeaderNames.LOCATION
import play.api.libs.json.Json
import play.api.test.Helpers.*
import repositories.SessionCache
import stubs.{AuthStub, ClaimsStub, ClaimsValidationStub}
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import util.TestUsers
import utils.{ComponentSpecHelper, TestDataUtils}

class WhoShouldWeSendPaymentToControllerISpec
  extends ComponentSpecHelper
    with AuthStub
    with TestDataUtils
    with ClaimsStub
    with ClaimsValidationStub {

  private val normalUrl = "/who-should-we-send-the-payment-to?claimId=123"

  lazy val sessionCache: SessionCache =
    app.injector.instanceOf[SessionCache]

  "GET /who-should-we-send-the-payment-to" should {

    "render the page for agent user" in {
      stubBackend()

      val result = get(normalUrl)

      result.status shouldBe OK

      val doc = Jsoup.parse(result.body)
      doc.title should include(msg("whoShouldWeSendPaymentTo.title"))
    }
  }

  "POST /who-should-we-send-the-payment-to" should {

    "save selection in session and redirect" in {
      stubBackend()

      val result =
        post(normalUrl)(
          Json.obj(
            "value" -> WhoShouldHmrcSendPaymentTo.AgentOrNominee.value
          )
        )

      result.status shouldBe SEE_OTHER
      result.header(LOCATION).value shouldBe routes.EnterTelephoneNumberController.onPageLoad(NormalMode).url

      given HeaderCarrier =
        HeaderCarrier(sessionId = Some(SessionId("mock-sessionid")))

      val cached = await(sessionCache.get())

      cached.value.agentUserOrganisationDetailsAnswers
        .flatMap(_.whoShouldHmrcSendPaymentTo) shouldBe Some(
        WhoShouldHmrcSendPaymentTo.AgentOrNominee
      )
    }
  }

  private def stubBackend(testClaim: Claim = claim): Unit = {
    stubAgentAuthRequest()
    stubRetrieveUnsubmittedClaims(OK, Json.toJson(getClaimsResponse))
    stubGetClaims(claimId)(OK, Json.toJson(testClaim.copy(userId = TestUsers.agent1)))
    stubGetUploadSummary(claimId)(OK, Json.toJson(testUploadSummaryResponse))
  }
}

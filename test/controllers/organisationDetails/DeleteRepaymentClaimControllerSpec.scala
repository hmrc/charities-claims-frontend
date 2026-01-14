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

import connectors.ClaimsConnector
import controllers.ControllerSpec
import forms.YesNoFormProvider
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import views.html.DeleteRepaymentClaimView

import scala.concurrent.Future

class DeleteRepaymentClaimControllerSpec extends ControllerSpec {

  val form: Form[Boolean] = new YesNoFormProvider()()

  val mockClaimsConnector: ClaimsConnector = mock[ClaimsConnector]

  "DeleteRepaymentClaimController" - {
    "onPageLoad" - {
      "should render the page correctly" in {

        given application: Application = applicationBuilder()
          .overrides(bind[ClaimsConnector].toInstance(mockClaimsConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.DeleteRepaymentClaimController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[DeleteRepaymentClaimView]
          val msgs   = application.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

          status(result)                        shouldBe OK
          contentAsString(result)               shouldBe view(form).body
          view.render(form, request, msgs).body shouldBe view(form).body
        }
      }
    }

    "onSubmit" - {
      "should reload the page with errors when a required field is missing" in {
        given application: Application = applicationBuilder()
          .overrides(bind[ClaimsConnector].toInstance(mockClaimsConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteRepaymentClaimController.onSubmit.url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldBe BAD_REQUEST
        }
      }

      // TODO: Update test when R2 screen route is completed (currently redirects to placeholder /make-charity-repayment-claim)
      "should redirect to R2 screen when no is selected" in {
        given application: Application = applicationBuilder()
          .overrides(bind[ClaimsConnector].toInstance(mockClaimsConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteRepaymentClaimController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.MakeCharityRepaymentClaimController.onPageLoad.url)
        }
      }

      // TODO: Update test when AA1 screen route is completed (currently redirects to placeholder /charity-repayment-dashboard)
      "should call backend deletion endpoint and redirect to AA1 when yes is selected and delete succeeds" in {
        val sessionData = models.SessionData.empty.copy(unsubmittedClaimId = Some("test-claim-123"))

        (mockClaimsConnector
          .deleteClaim(_: String)(using _: HeaderCarrier))
          .expects("test-claim-123", *)
          .returning(Future.successful(true))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(bind[ClaimsConnector].toInstance(mockClaimsConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteRepaymentClaimController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CharityRepaymentDashboardController.onPageLoad.url)
        }
      }

      "should handle case when backend returns success: false" in {
        val sessionData = models.SessionData.empty.copy(unsubmittedClaimId = Some("test-claim-123"))

        (mockClaimsConnector
          .deleteClaim(_: String)(using _: HeaderCarrier))
          .expects("test-claim-123", *)
          .returning(Future.successful(false))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(bind[ClaimsConnector].toInstance(mockClaimsConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteRepaymentClaimController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          a[RuntimeException] should be thrownBy result.futureValue
        }
      }

      "should handle case when no claimId is found in session data" in {
        val sessionData = models.SessionData.empty.copy(unsubmittedClaimId = None)

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(bind[ClaimsConnector].toInstance(mockClaimsConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteRepaymentClaimController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          a[RuntimeException] should be thrownBy result.futureValue
        }
      }
    }
  }
}

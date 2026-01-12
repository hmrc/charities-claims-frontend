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

import connectors.ClaimsValidationConnector
import controllers.ControllerSpec
import forms.YesNoFormProvider
import models.{DeleteScheduleResponse, GetUploadSummaryResponse, UploadSummary}
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import views.html.DeleteOtherIncomeScheduleView

import scala.concurrent.Future

class DeleteOtherIncomeScheduleControllerSpec extends ControllerSpec {

  val form: Form[Boolean] = new YesNoFormProvider()()

  val mockClaimsValidationConnector: ClaimsValidationConnector = mock[ClaimsValidationConnector]

  val testUploadSummaryWithOtherIncome = GetUploadSummaryResponse(
    uploads = Seq(
      UploadSummary(
        reference = "other-income-ref-456",
        validationType = "OtherIncome",
        fileStatus = "VALIDATED",
        uploadUrl = None
      )
    )
  )

  val testUploadSummaryWithoutOtherIncome = GetUploadSummaryResponse(
    uploads = Seq(
      UploadSummary(
        reference = "gift-aid-ref-789",
        validationType = "GiftAid",
        fileStatus = "VALIDATED",
        uploadUrl = None
      )
    )
  )

  "DeleteOtherIncomeScheduleController" - {
    "onPageLoad" - {
      "should render the page correctly" in {

        given application: Application = applicationBuilder()
          .overrides(bind[ClaimsValidationConnector].toInstance(mockClaimsValidationConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.DeleteOtherIncomeScheduleController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[DeleteOtherIncomeScheduleView]
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
          .overrides(bind[ClaimsValidationConnector].toInstance(mockClaimsValidationConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteOtherIncomeScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldBe BAD_REQUEST
        }
      }

      // TODO: Update test when G2 screen route is completed (currently redirects to placeholder /add-schedule)
      "should redirect to G2 screen when no is selected" in {
        given application: Application = applicationBuilder()
          .overrides(bind[ClaimsValidationConnector].toInstance(mockClaimsValidationConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteOtherIncomeScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.AddScheduleController.onPageLoad.url)
        }
      }

      // TODO: Update test when R2 screen route is completed (currently redirects to placeholder /make-charity-repayment-claim)
      "should call backend deletion endpoint and redirect to R2 when yes is selected" in {
        val sessionData = models.SessionData.empty.copy(unsubmittedClaimId = Some("test-claim-123"))

        (mockClaimsValidationConnector
          .getUploadSummary(_: String)(using _: HeaderCarrier))
          .expects("test-claim-123", *)
          .returning(Future.successful(testUploadSummaryWithOtherIncome))

        (mockClaimsValidationConnector
          .deleteSchedule(_: String, _: String)(using _: HeaderCarrier))
          .expects("test-claim-123", "other-income-ref-456", *)
          .returning(Future.successful(DeleteScheduleResponse(success = true)))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(bind[ClaimsValidationConnector].toInstance(mockClaimsValidationConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteOtherIncomeScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.MakeCharityRepaymentClaimController.onPageLoad.url)
        }
      }

      "should handle case when no OtherIncome upload data is found" in {
        val sessionData = models.SessionData.empty.copy(unsubmittedClaimId = Some("test-claim-123"))

        (mockClaimsValidationConnector
          .getUploadSummary(_: String)(using _: HeaderCarrier))
          .expects("test-claim-123", *)
          .returning(Future.successful(testUploadSummaryWithoutOtherIncome))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(bind[ClaimsValidationConnector].toInstance(mockClaimsValidationConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteOtherIncomeScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          a[RuntimeException] should be thrownBy result.futureValue
        }
      }

      "should handle case when no claimId is in session data" in {
        val sessionData = models.SessionData.empty.copy(unsubmittedClaimId = None)

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(bind[ClaimsValidationConnector].toInstance(mockClaimsValidationConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteOtherIncomeScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          a[RuntimeException] should be thrownBy result.futureValue
        }
      }
    }
  }
}

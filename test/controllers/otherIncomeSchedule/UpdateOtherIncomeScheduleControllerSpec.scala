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

package controllers.otherIncomeSchedule

import controllers.ControllerSpec
import forms.YesNoFormProvider
import models.requests.DataRequest
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import services.ClaimsValidationService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.UpdateOtherIncomeScheduleView

import scala.concurrent.Future
import services.ClaimsService
import models.RepaymentClaimDetailsAnswers

class UpdateOtherIncomeScheduleControllerSpec extends ControllerSpec {

  val form: Form[Boolean] = new YesNoFormProvider()()

  val mockClaimsValidationService: ClaimsValidationService = mock[ClaimsValidationService]
  val mockClaimsService: ClaimsService                     = mock[ClaimsService]

  "UpdateOtherIncomeScheduleController" - {
    "onPageLoad" - {
      "should render the page correctly" in {
        val sessionData                =
          completeRepaymentDetailsAnswersSession.and(RepaymentClaimDetailsAnswers.setClaimingTaxDeducted(true))
        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.UpdateOtherIncomeScheduleController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[UpdateOtherIncomeScheduleView]
          val msgs   = application.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

          status(result)                        shouldBe OK
          contentAsString(result)               shouldBe view(form).body
          view.render(form, request, msgs).body shouldBe view(form).body
        }
      }
    }

    "onSubmit" - {
      "should reload the page with errors when a required field is missing" in {
        val sessionData                =
          completeRepaymentDetailsAnswersSession.and(RepaymentClaimDetailsAnswers.setClaimingTaxDeducted(true))
        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.UpdateOtherIncomeScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldBe BAD_REQUEST
        }
      }

      "should redirect to CheckYourOtherIncomeSchedule screen when no is selected" in {
        val sessionData                =
          completeRepaymentDetailsAnswersSession.and(RepaymentClaimDetailsAnswers.setClaimingTaxDeducted(true))
        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.UpdateOtherIncomeScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(
            routes.CheckYourOtherIncomeScheduleController.onPageLoad.url
          )
        }
      }

      "should call backend deletion endpoint and redirect to UploadOtherIncomeSchedule when yes is selected" in {
        val sessionData =
          completeRepaymentDetailsAnswersSession
            .and(RepaymentClaimDetailsAnswers.setClaimingTaxDeducted(true))

        (mockClaimsValidationService
          .deleteOtherIncomeSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        (mockClaimsService
          .save(using _: HeaderCarrier))
          .expects(*)
          .returning(Future.successful(()))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .overrides(bind[ClaimsService].toInstance(mockClaimsService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.UpdateOtherIncomeScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.UploadOtherIncomeScheduleController.onPageLoad.url)
        }
      }

      "should handle case when no OtherIncome upload data is found" in {
        val sessionData =
          completeRepaymentDetailsAnswersSession
            .and(RepaymentClaimDetailsAnswers.setClaimingTaxDeducted(true))

        (mockClaimsValidationService
          .deleteOtherIncomeSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.failed(new RuntimeException("No OtherIncome schedule upload found")))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.UpdateOtherIncomeScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          a[RuntimeException] should be thrownBy result.futureValue
        }
      }

      "should handle case when no claimId is in session data" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingTaxDeducted(true)
          .copy(unsubmittedClaimId = None)

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.UpdateOtherIncomeScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value
          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(
            controllers.routes.PageNotFoundController.onPageLoad.url
          )
        }
      }
    }
  }
}

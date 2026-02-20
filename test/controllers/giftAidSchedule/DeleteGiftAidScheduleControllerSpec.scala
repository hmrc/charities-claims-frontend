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

package controllers.giftAidSchedule

import play.api.test.FakeRequest
import services.ClaimsValidationService
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import controllers.ControllerSpec
import play.api.inject.bind
import views.html.DeleteGiftAidScheduleView
import play.api.Application
import models.RepaymentClaimDetailsAnswers
import forms.YesNoFormProvider
import uk.gov.hmrc.http.HeaderCarrier
import models.requests.DataRequest
import play.api.data.Form

import scala.concurrent.Future

class DeleteGiftAidScheduleControllerSpec extends ControllerSpec {

  val form: Form[Boolean] = new YesNoFormProvider()()

  val mockClaimsValidationService: ClaimsValidationService = mock[ClaimsValidationService]

  "DeleteGiftAidScheduleController" - {
    "onPageLoad" - {
      "should render the page correctly" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.DeleteGiftAidScheduleController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[DeleteGiftAidScheduleView]
          val msgs   = application.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

          status(result)                        shouldBe OK
          contentAsString(result)               shouldBe view(form).body
          view.render(form, request, msgs).body shouldBe view(form).body
        }
      }
    }

    "onSubmit" - {
      "should reload the page with errors when a required field is missing" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteGiftAidScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldBe BAD_REQUEST
        }
      }

      "should redirect to ProblemWithGiftAidScheduleController (G1.4) when no is selected" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteGiftAidScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(
            controllers.giftAidSchedule.routes.ProblemWithGiftAidScheduleController.onPageLoad.url
          )
        }
      }

      "should call backend deletion endpoint and redirect to R2 when yes is selected" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))

        (mockClaimsValidationService
          .deleteGiftAidSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteGiftAidScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should handle case when no GiftAid upload data is found" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))

        (mockClaimsValidationService
          .deleteGiftAidSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.failed(new RuntimeException("No GiftAid schedule upload found")))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteGiftAidScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          a[RuntimeException] should be thrownBy result.futureValue
        }
      }

      "should handle case when gift aid upload is not expected" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingGiftAid(false)
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteGiftAidScheduleController.onSubmit.url)
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

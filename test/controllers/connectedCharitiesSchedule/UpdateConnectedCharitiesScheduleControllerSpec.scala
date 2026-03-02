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

package controllers.connectedCharitiesSchedule

import controllers.ControllerSpec
import forms.YesNoFormProvider
import models.*
import models.requests.DataRequest
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import services.ClaimsValidationService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.UpdateConnectedCharitiesScheduleView

import scala.concurrent.Future
import services.ClaimsService

class UpdateConnectedCharitiesScheduleControllerSpec extends ControllerSpec {

  val form: Form[Boolean] = new YesNoFormProvider()()

  val mockClaimsValidationService: ClaimsValidationService = mock[ClaimsValidationService]
  val mockClaimsService: ClaimsService                     = mock[ClaimsService]

  def validSessionData: SessionData = completeGasdsSession

  "UpdateConnectedCharitiesScheduleController" - {
    "onPageLoad" - {
      "should render the page correctly" in {
        given application: Application = applicationBuilder(sessionData = validSessionData)
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.UpdateConnectedCharitiesScheduleController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[UpdateConnectedCharitiesScheduleView]
          val msgs   = application.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

          status(result)                        shouldBe OK
          contentAsString(result)               shouldBe view(form).body
          view.render(form, request, msgs).body shouldBe view(form).body
        }
      }
    }

    "onSubmit" - {
      "should reload the page with errors when a required field is missing" in {
        given application: Application = applicationBuilder(sessionData = validSessionData)
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.UpdateConnectedCharitiesScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldBe BAD_REQUEST
        }
      }

      "should redirect to CheckYourConnectedCharitiesSchedule screen when no is selected" in {
        given application: Application = applicationBuilder(sessionData = validSessionData)
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.UpdateConnectedCharitiesScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(
            routes.CheckYourConnectedCharitiesScheduleController.onPageLoad.url
          )
        }
      }

      "should call backend deletion endpoint and redirect to UploadConnectedCharitiesSchedule when yes is selected" in {
        (mockClaimsValidationService
          .deleteConnectedCharitiesSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        (mockClaimsService
          .save(using _: HeaderCarrier))
          .expects(*)
          .returning(Future.successful(()))

        given application: Application = applicationBuilder(sessionData = validSessionData)
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .overrides(bind[ClaimsService].toInstance(mockClaimsService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.UpdateConnectedCharitiesScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.UploadConnectedCharitiesScheduleController.onPageLoad.url)
        }
      }

      "should handle case when no ConnectedCharities upload data is found" in {
        (mockClaimsValidationService
          .deleteConnectedCharitiesSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.failed(new RuntimeException("No ConnectedCharities schedule upload found")))

        given application: Application = applicationBuilder(sessionData = validSessionData)
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.UpdateConnectedCharitiesScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          a[RuntimeException] should be thrownBy result.futureValue
        }
      }

      "should redirect to ClaimsTaskListController when data guard is triggered" in {
        val sessionDataFailingGuard = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setConnectedToAnyOtherCharities(false))
          .copy(unsubmittedClaimId = None)

        given application: Application = applicationBuilder(sessionData = sessionDataFailingGuard)
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.UpdateConnectedCharitiesScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value
          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }
    }
  }
}

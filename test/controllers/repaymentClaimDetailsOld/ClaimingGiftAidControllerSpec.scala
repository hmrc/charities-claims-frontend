/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.repaymentclaimdetailsold

import controllers.ControllerSpec
import forms.YesNoFormProvider
import models.*
import models.Mode.*
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import services.ClaimsValidationService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ClaimingGiftAidView
import util.TestScheduleData
import models.requests.DataRequest

import scala.concurrent.Future

class ClaimingGiftAidControllerSpec extends ControllerSpec {

  private val form: Form[Boolean] = new YesNoFormProvider()()

  val mockClaimsValidationService: ClaimsValidationService = mock[ClaimsValidationService]

  "ClaimingGiftAidController" - {
    "onPageLoad" - {
      "should render the page correctly" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingGiftAidController.onPageLoad(NormalMode).url)
          val form: Form[Boolean]                            = new YesNoFormProvider()()

          val result = route(application, request).value

          val view = application.injector.instanceOf[ClaimingGiftAidView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(form, NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with true value in check mode" in {

        val sessionData = RepaymentClaimDetailsAnswersOld.setClaimingGiftAid(true)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingGiftAidController.onPageLoad(CheckMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimingGiftAidView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(true), CheckMode).body
        }
      }

      "should render the page and pre-populate correctly with false value" in {

        val sessionData = RepaymentClaimDetailsAnswersOld.setClaimingGiftAid(false)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingGiftAidController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimingGiftAidView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(false), NormalMode).body
        }
      }
    }

    "onSubmit" - {
      "should redirect to the next page when the value is true" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingGiftAidController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.ClaimingOtherIncomeController.onPageLoad(NormalMode).url)
        }
      }

      "should redirect to the next page when the value is false" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingGiftAidController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.ClaimingOtherIncomeController.onPageLoad(NormalMode).url)
        }
      }

      "should redirect back to cya page when the value is true" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingGiftAidController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.CheckYourAnswersController.onPageLoad.url)
        }
      }

      "should redirect back to cya page when the value is false" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingGiftAidController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.CheckYourAnswersController.onPageLoad.url)
        }
      }

      "should reload the page with errors when a required field is missing" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingGiftAidController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }
    }

    "onSubmit with warning" - {
      "should trigger warning when changing to false with Gift Aid schedule data present" in {
        val sessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            repaymentClaimDetailsAnswersOld = RepaymentClaimDetailsAnswersOld(claimingGiftAid = Some(true)),
            giftAidScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
            giftAidScheduleData = Some(TestScheduleData.exampleGiftAidScheduleData)
          )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingGiftAidController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.ClaimingGiftAidController.onPageLoad(NormalMode).url)
          flash(result).get("warning") shouldEqual Some("true")
        }
      }

      "should not show warning when no previous gift aid schedule data exists" in {
        val sessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            repaymentClaimDetailsAnswersOld = RepaymentClaimDetailsAnswersOld(claimingGiftAid = Some(false))
          )

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingGiftAidController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          flash(result).get("warning") shouldEqual None
        }
      }

      "should delete schedule and redirect to ClaimingOtherIncomeController when changing to No after warning in NormalMode" in {
        val sessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            unsubmittedClaimId = Some("test-claim-123"),
            repaymentClaimDetailsAnswersOld = RepaymentClaimDetailsAnswersOld(claimingGiftAid = Some(true)),
            giftAidScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
            giftAidScheduleData = Some(TestScheduleData.exampleGiftAidScheduleData)
          )

        (mockClaimsValidationService
          .deleteGiftAidSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingGiftAidController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "false", "warningShown" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result).value shouldEqual routes.ClaimingOtherIncomeController.onPageLoad(NormalMode).url
        }
      }

      "should delete schedule and redirect to CheckYourAnswersController when changing to No after warning in CheckMode" in {
        val sessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            unsubmittedClaimId = Some("test-claim-456"),
            repaymentClaimDetailsAnswersOld = RepaymentClaimDetailsAnswersOld(claimingGiftAid = Some(true)),
            giftAidScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
            giftAidScheduleData = Some(TestScheduleData.exampleGiftAidScheduleData)
          )

        (mockClaimsValidationService
          .deleteGiftAidSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingGiftAidController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false", "warningShown" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result).value shouldEqual routes.CheckYourAnswersController.onPageLoad.url
        }
      }

      "should redirect to ClaimingOtherIncomeController when selecting Yes (not trigger delete)" in {
        val sessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            repaymentClaimDetailsAnswersOld = RepaymentClaimDetailsAnswersOld(claimingGiftAid = Some(true)),
            giftAidScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
            giftAidScheduleData = Some(TestScheduleData.exampleGiftAidScheduleData)
          )

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingGiftAidController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result).value shouldEqual routes.ClaimingOtherIncomeController.onPageLoad(NormalMode).url
        }
      }

      "should redirect to ClaimingOtherIncomeController when selecting No with no existing schedule data" in {
        val sessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            repaymentClaimDetailsAnswersOld = RepaymentClaimDetailsAnswersOld(claimingGiftAid = Some(false)),
            giftAidScheduleFileUploadReference = None,
            giftAidScheduleData = None
          )

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingGiftAidController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result).value shouldEqual routes.ClaimingOtherIncomeController.onPageLoad(NormalMode).url
        }
      }

      "should render warning parameter hidden field when flash present" in {
        val sessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            repaymentClaimDetailsAnswersOld = RepaymentClaimDetailsAnswersOld(claimingGiftAid = Some(true)),
            giftAidScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
            giftAidScheduleData = Some(TestScheduleData.exampleGiftAidScheduleData)
          )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingGiftAidController.onPageLoad(NormalMode).url)
              .withFlash("warning" -> "true", "warningAnswer" -> "false")

          val result = route(application, request).value

          contentAsString(result) should include("warningShown")
        }
      }
    }
  }
}

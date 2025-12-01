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

package controllers.repaymentclaimdetails

import play.api.test.FakeRequest
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import controllers.ControllerSpec
import views.html.ClaimReferenceNumberInputView
import play.api.Application
import forms.TextInputFormProvider
import models.{CheckMode, Mode, NormalMode, RepaymentClaimDetailsAnswers, SessionData}
import play.api.data.Form
import play.api.test.Helpers.*
import handlers.ErrorHandler

class ClaimReferenceNumberInputControllerSpec extends ControllerSpec {

  private val form: Form[String] = new TextInputFormProvider()(
    "claimReferenceNumberInput.error.required",
    (20, "claimReferenceNumberInput.error.length"),
    "claimReferenceNumberInput.error.regex"
  )

  "ClaimReferenceNumberInputController" - {
    "onPageLoad" - {
      "should render the page correctly" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingReferenceNumber(true)
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimReferenceNumberInputController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimReferenceNumberInputView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, NormalMode).body
        }
      }

      "should render page not found if claiming reference number is false" in {
        val sessionData = RepaymentClaimDetailsAnswers.setClaimingReferenceNumber(false)

        given application: Application = applicationBuilder(sessionData = sessionData).build()
        val errorHandler               = application.injector.instanceOf[ErrorHandler]

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimReferenceNumberInputController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual NOT_FOUND
          contentAsString(result) shouldEqual await(errorHandler.notFoundTemplate(request)).body
        }
      }

      "should render page not found if claiming reference number is empty" in {

        val sessionData = SessionData(repaymentClaimDetailsAnswers =
          Some(RepaymentClaimDetailsAnswers(claimReferenceNumber = Some("123456")))
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()
        val errorHandler               = application.injector.instanceOf[ErrorHandler]

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimReferenceNumberInputController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual NOT_FOUND
          contentAsString(result) shouldEqual await(errorHandler.notFoundTemplate(request)).body
        }
      }

      "should render the page and pre-populate correctly" in {

        val sessionData = SessionData(repaymentClaimDetailsAnswers =
          Some(
            RepaymentClaimDetailsAnswers(claimingReferenceNumber = Some(true), claimReferenceNumber = Some("123456"))
          )
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        val errorHandler = application.injector.instanceOf[ErrorHandler]
        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimReferenceNumberInputController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimReferenceNumberInputView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill("123456"), NormalMode).body
        }
      }

      "should render the page not found and incorrectly pre-populate data" in {

        val sessionData = SessionData(repaymentClaimDetailsAnswers =
          Some(
            RepaymentClaimDetailsAnswers(claimingReferenceNumber = Some(false), claimReferenceNumber = Some("123456"))
          )
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()
        val errorHandler               = application.injector.instanceOf[ErrorHandler]

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimReferenceNumberInputController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual NOT_FOUND
          contentAsString(result) shouldEqual await(errorHandler.notFoundTemplate(request)).body
        }
      }

    }

    "onSubmit" - {
      "should redirect to Declaration page when in NormalMode" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimReferenceNumberInputController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "123456")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.ClaimDeclarationController.onPageLoad.url)
        }
      }

      "should redirect back to CYA page when in CheckMode" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimReferenceNumberInputController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "123456")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.CheckYourAnswersController.onPageLoad.url)
        }
      }

      "should reload the page with errors when a required field is missing" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimReferenceNumberInputController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }
    }
  }
}

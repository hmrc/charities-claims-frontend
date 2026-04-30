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

package controllers.repaymentClaimDetails

import controllers.ControllerSpec
import forms.TextInputFormProvider
import models.Mode.*
import models.{RepaymentClaimDetailsAnswers, SessionData}
import play.api.Application
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import views.html.ClaimReferenceNumberInputView

class CharitiesReferenceNumberInputControllerSpec extends ControllerSpec {

  private val form: Form[String] = new TextInputFormProvider()(
    "charitiesReferenceNumber.error.required",
    (7, "charitiesReferenceNumber.error.length"),
    "charitiesReferenceNumber.error.regex"
  )

  "CharitiesReferenceNumberInputController" - {

    "onPageLoad" - {
      "should render the page correctly when the user has said YES to having a reference number" in {
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

      "should render ClaimsTaskListController if claimingReferenceNumber is false" in {
        val sessionData = RepaymentClaimDetailsAnswers.setClaimingReferenceNumber(false)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimReferenceNumberInputController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should render ClaimsTaskListController if charitiesReferenceNumber is empty (None)" in {
        // Must provide 'old' answers to satisfy the case class, even if empty - this needs to be removed when the other pages are created
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          repaymentClaimDetailsAnswers = Some(RepaymentClaimDetailsAnswers(claimReferenceNumber = Some("123456")))
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimReferenceNumberInputController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should render the ClaimsTaskListController and incorrectly pre-populate data" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          repaymentClaimDetailsAnswers = Some(
            RepaymentClaimDetailsAnswers(
              claimingReferenceNumber = Some(false),
              claimReferenceNumber = Some("123456")
            )
          )
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimReferenceNumberInputController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

    }

    "onSubmit" - {
      "should render ClaimCompleteController if submissionReference is defined" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          lastUpdatedReference = Some(testCharitiesReference),
          submissionReference = Some(testCharitiesReference)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.ClaimReferenceNumberInputController.onSubmit(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
      "should redirect to Declaration page when in NormalMode" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingReferenceNumber(true)
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimReferenceNumberInputController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "123456")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "should redirect back to CYA page when in CheckMode" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingReferenceNumber(true)
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimReferenceNumberInputController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "123456")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "should reload the page with errors when a required field is missing" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingReferenceNumber(true)
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimReferenceNumberInputController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      "should redirect to ClaimsTaskListController when claimingReferenceNumber is false" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingReferenceNumber(false)
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimReferenceNumberInputController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "123456")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should redirect to ClaimsTaskListController when claimingReferenceNumber is None" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimReferenceNumberInputController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "123456")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }
    }
  }
}

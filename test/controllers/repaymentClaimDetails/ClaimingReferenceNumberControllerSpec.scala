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
import forms.YesNoFormProvider
import models.{RepaymentClaimDetailsAnswers, SessionData}
import play.api.Application
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import uk.gov.hmrc.auth.core.AffinityGroup
import play.api.test.FakeRequest
import views.html.ClaimingReferenceNumberView
import models.Mode.*

class ClaimingReferenceNumberControllerSpec extends ControllerSpec {
  private val form: Form[Boolean] = new YesNoFormProvider()()
  "ClaimingReferenceNumberController" - {
    "onPageLoad" - {
      "should render the page correctly for an organisation" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingReferenceNumberController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimingReferenceNumberView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, NormalMode, false).body
        }
      }

      "should render the page correctly for an agent" in {

        given application: Application = applicationBuilder(affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingReferenceNumberController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimingReferenceNumberView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, NormalMode, true).body
        }
      }

      "should render the page and pre-populate correctly with true value for an organisation" in {

        val sessionData = RepaymentClaimDetailsAnswers.setClaimingReferenceNumber(true)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingReferenceNumberController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimingReferenceNumberView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(true), NormalMode, false).body
        }
      }

      "should render the page and pre-populate correctly with true value for an agent" in {

        val sessionData = RepaymentClaimDetailsAnswers.setClaimingReferenceNumber(true)

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingReferenceNumberController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimingReferenceNumberView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(true), NormalMode, true).body
        }
      }

      "should render the page and pre-populate correctly with false value for an organisation" in {

        val sessionData = RepaymentClaimDetailsAnswers.setClaimingReferenceNumber(false)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingReferenceNumberController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimingReferenceNumberView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(false), NormalMode, false).body
        }
      }

      "should render the page and pre-populate correctly with false value for an agent" in {

        val sessionData = RepaymentClaimDetailsAnswers.setClaimingReferenceNumber(false)

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingReferenceNumberController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimingReferenceNumberView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(false), NormalMode, true).body
        }
      }

      "should render ClaimCompleteController if submissionReference is defined" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          lastUpdatedReference = Some(testCharitiesReference),
          submissionReference = Some(testCharitiesReference)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingReferenceNumberController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
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
            FakeRequest(POST, routes.ClaimingReferenceNumberController.onSubmit(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
      "should redirect to the next page when the value is true" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingReferenceNumberController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimReferenceNumberInputController.onPageLoad(NormalMode).url
          )
        }
      }

      "CheckMode: should redirect to the next page when the value is true" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingReferenceNumberController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimReferenceNumberInputController.onPageLoad(CheckMode).url
          )
        }
      }

      "CheckMode: should redirect to the next page when the value is false" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingReferenceNumberController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "checkmode: should redirect to the next page when the value is true and previously was true " in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingReferenceNumber(true)
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingReferenceNumberController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "checkmode: should redirect to the next page when the value is true and previously was false " in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingReferenceNumber(false)
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingReferenceNumberController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimReferenceNumberInputController.onPageLoad(CheckMode).url
          )
        }
      }

      "should redirect to the next page(cya) when the value is false" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingReferenceNumberController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "should reload the page with errors when a required field is missing" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingReferenceNumberController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }
    }
  }
}

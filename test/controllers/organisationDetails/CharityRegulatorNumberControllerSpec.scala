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

package controllers.organisationDetails

import controllers.ControllerSpec
import forms.CharityRegulatorNumberFormProvider
import models.Mode.*
import models.{OrganisationDetailsAnswers, SessionData}
import play.api.Application
import play.api.i18n.MessagesApi
import models.NameOfCharityRegulator.*
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.CharityRegulatorNumberView

class CharityRegulatorNumberControllerSpec extends ControllerSpec {

  val formProvider = new CharityRegulatorNumberFormProvider()
  val form         = formProvider()

  "CharityRegulatorNumberController" - {

    "onPageLoad" - {
      "should render ClaimCompleteController if submissionReference is defined" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          lastUpdatedReference = Some(testCharitiesReference),
          submissionReference = Some(testCharitiesReference)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CharityRegulatorNumberController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
      "should redirect to the ClaimsTaskListController if charity ref start with CH" in {
        val testCharitiesReference: String                      = "CH-test-charities-ref"
        val completeRepaymentDetailsAnswersSession: SessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some("test-claim-id"),
          repaymentClaimDetailsAnswers = Some(completeRepaymentClaimDetailsAnswers)
        )

        given application: Application =
          applicationBuilder(sessionData = completeRepaymentDetailsAnswersSession).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CharityRegulatorNumberController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should redirect to the ClaimsTaskListController if charity ref start with CF" in {
        val testCharitiesReference: String                      = "CF-test-charities-ref"
        val completeRepaymentDetailsAnswersSession: SessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some("test-claim-id"),
          repaymentClaimDetailsAnswers = Some(completeRepaymentClaimDetailsAnswers)
        )

        given application: Application =
          applicationBuilder(sessionData = completeRepaymentDetailsAnswersSession).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CharityRegulatorNumberController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should render the page correctly if name of charity is EnglandAndWales" in {
        val sessionData                = completeRepaymentDetailsAnswersSession.and(
          OrganisationDetailsAnswers.setNameOfCharityRegulator(EnglandAndWales)
        )
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CharityRegulatorNumberController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[CharityRegulatorNumberView]

          val messages = application.injector.instanceOf[MessagesApi].preferred(request)

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(form, NormalMode)(using request, messages).body
        }
      }

      "should render the page correctly if name of charity is NorthernIreland" in {
        val sessionData = completeRepaymentDetailsAnswersSession.and(
          OrganisationDetailsAnswers.setNameOfCharityRegulator(NorthernIreland)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CharityRegulatorNumberController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[CharityRegulatorNumberView]

          val messages = application.injector.instanceOf[MessagesApi].preferred(request)

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(form, NormalMode)(using request, messages).body
        }
      }

      "should render the page correctly if name of charity is Scottish" in {
        val sessionData =
          completeRepaymentDetailsAnswersSession.and(OrganisationDetailsAnswers.setNameOfCharityRegulator(Scottish))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CharityRegulatorNumberController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[CharityRegulatorNumberView]

          val messages = application.injector.instanceOf[MessagesApi].preferred(request)

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(form, NormalMode)(using request, messages).body
        }
      }

      "should render page not found if name of charity is None" in {
        val sessionData =
          completeRepaymentDetailsAnswersSession.and(OrganisationDetailsAnswers.setNameOfCharityRegulator(None))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CharityRegulatorNumberController.onPageLoad(NormalMode).url)

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
            FakeRequest(POST, routes.CharityRegulatorNumberController.onSubmit(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
      "should redirect to the next page when valid data is submitted" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          val request = FakeRequest(POST, routes.CharityRegulatorNumberController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody("value" -> "12345678")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.CorporateTrusteeClaimController.onPageLoad(NormalMode).url)
        }
      }

      "should redirect to CYA when valid data is submitted" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          val request = FakeRequest(POST, routes.CharityRegulatorNumberController.onSubmit(CheckMode).url)
            .withFormUrlEncodedBody("value" -> "12345678")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)
        }
      }

      "should return BadRequest when invalid data (letters) is submitted" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request = FakeRequest(POST, routes.CharityRegulatorNumberController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody("value" -> "123ABC")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      "should return BadRequest when empty data is submitted" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request = FakeRequest(POST, routes.CharityRegulatorNumberController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody("value" -> "")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }
    }
  }
}

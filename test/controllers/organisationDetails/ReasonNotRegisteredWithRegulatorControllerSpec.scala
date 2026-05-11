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

import play.api.test.FakeRequest
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import controllers.ControllerSpec
import views.html.ReasonNotRegisteredWithRegulatorView
import play.api.Application
import forms.RadioListFormProvider
import models.{OrganisationDetailsAnswers, ReasonNotRegisteredWithRegulator, SessionData}
import play.api.data.Form
import models.Mode.*
import models.NameOfCharityRegulator.{EnglandAndWales, None, NorthernIreland, Scottish}
import models.ReasonNotRegisteredWithRegulator.*
import uk.gov.hmrc.auth.core.AffinityGroup

class ReasonNotRegisteredWithRegulatorControllerSpec extends ControllerSpec {

  private val form: Form[ReasonNotRegisteredWithRegulator] = new RadioListFormProvider()(
    "reasonNotRegisteredWithRegulator.error.required"
  )

  "ReasonNotRegisteredWithRegulatorController" - {
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
            FakeRequest(GET, routes.ReasonNotRegisteredWithRegulatorController.onPageLoad(NormalMode).url)

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
            FakeRequest(GET, routes.ReasonNotRegisteredWithRegulatorController.onPageLoad(NormalMode).url)

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
            FakeRequest(GET, routes.ReasonNotRegisteredWithRegulatorController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should render the page correctly if name of charity is None" in {
        val sessionData                =
          completeRepaymentDetailsAnswersSession.and(OrganisationDetailsAnswers.setNameOfCharityRegulator(None))
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ReasonNotRegisteredWithRegulatorController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ReasonNotRegisteredWithRegulatorView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, NormalMode).body
        }
      }

      "should render page not found if name of charity is Scottish" in {
        val sessionData =
          completeRepaymentDetailsAnswersSession.and(OrganisationDetailsAnswers.setNameOfCharityRegulator(Scottish))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ReasonNotRegisteredWithRegulatorController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should render page not found if name of charity is NorthernIreland" in {
        val sessionData = completeRepaymentDetailsAnswersSession.and(
          OrganisationDetailsAnswers.setNameOfCharityRegulator(NorthernIreland)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ReasonNotRegisteredWithRegulatorController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should render page not found if name of charity is EnglandAndWales" in {
        val sessionData = completeRepaymentDetailsAnswersSession.and(
          OrganisationDetailsAnswers.setNameOfCharityRegulator(EnglandAndWales)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ReasonNotRegisteredWithRegulatorController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should render the page and pre-populate correctly with lowIncome value when name of charity is None" in {

        val sessionDataWithLowIncome                        = completeRepaymentDetailsAnswersSession.and(
          OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(LowIncome)
        )
        val sessionDataWithLowIncomeAndCharityRegulatorNone =
          OrganisationDetailsAnswers.setNameOfCharityRegulator(None)(using sessionDataWithLowIncome)

        given application: Application =
          applicationBuilder(sessionData = sessionDataWithLowIncomeAndCharityRegulatorNone).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ReasonNotRegisteredWithRegulatorController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ReasonNotRegisteredWithRegulatorView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(
            form.fill(ReasonNotRegisteredWithRegulator.LowIncome),
            NormalMode
          ).body
        }
      }

      "should render the page and pre-populate correctly with excepted value when name of charity is None" in {

        val sessionDataWithExcepted                        = completeRepaymentDetailsAnswersSession.and(
          OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(Excepted)
        )
        val sessionDataWithExceptedAndCharityRegulatorNone =
          OrganisationDetailsAnswers.setNameOfCharityRegulator(None)(using sessionDataWithExcepted)

        given application: Application =
          applicationBuilder(sessionData = sessionDataWithExceptedAndCharityRegulatorNone).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ReasonNotRegisteredWithRegulatorController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ReasonNotRegisteredWithRegulatorView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(Excepted), NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with exempted value when name of charity is None" in {

        val sessionDataWithExempt                        = completeRepaymentDetailsAnswersSession.and(
          OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(Exempt)
        )
        val sessionDataWithExemptAndCharityRegulatorNone =
          OrganisationDetailsAnswers.setNameOfCharityRegulator(None)(using sessionDataWithExempt)

        given application: Application =
          applicationBuilder(sessionData = sessionDataWithExemptAndCharityRegulatorNone).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ReasonNotRegisteredWithRegulatorController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ReasonNotRegisteredWithRegulatorView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(Exempt), NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with waiting value when name of charity is None" in {

        val sessionDataWithWaiting                        = completeRepaymentDetailsAnswersSession.and(
          OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(Waiting)
        )
        val sessionDataWithWaitingAndCharityRegulatorNone =
          OrganisationDetailsAnswers.setNameOfCharityRegulator(None)(using sessionDataWithWaiting)

        given application: Application =
          applicationBuilder(sessionData = sessionDataWithWaitingAndCharityRegulatorNone).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ReasonNotRegisteredWithRegulatorController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ReasonNotRegisteredWithRegulatorView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(Waiting), NormalMode).body
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
            FakeRequest(POST, routes.ReasonNotRegisteredWithRegulatorController.onSubmit(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }

      "should redirect to CharityExceptedController when the value is Excepted for an organisation" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ReasonNotRegisteredWithRegulatorController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "Excepted")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.CharityExceptedController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to CharityExceptedController when the value is Excepted for an agent" in {
        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ReasonNotRegisteredWithRegulatorController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "Excepted")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.CharityExceptedController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to CharityExemptController when the value is Exempt for an organisation" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ReasonNotRegisteredWithRegulatorController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "Exempt")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.CharityExemptController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to CharityExemptController when the value is Exempt for an agent" in {
        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ReasonNotRegisteredWithRegulatorController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "Exempt")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.CharityExemptController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to CorporateTrusteeClaimController when the value is lowIncome for an organisation" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ReasonNotRegisteredWithRegulatorController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "LowIncome")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.CorporateTrusteeClaimController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to WhoShouldWeSendPaymentToController when the value is lowIncome for an agent" in {
        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ReasonNotRegisteredWithRegulatorController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "LowIncome")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.WhoShouldWeSendPaymentToController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to CorporateTrusteeClaimController when the value is waiting for an organisation" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ReasonNotRegisteredWithRegulatorController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "Waiting")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.CorporateTrusteeClaimController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to WhoShouldWeSendPaymentToController when the value is waiting for an agent" in {
        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ReasonNotRegisteredWithRegulatorController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "Waiting")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.WhoShouldWeSendPaymentToController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to CharityExceptedController when the value is Excepted in CheckMode for an organisation" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ReasonNotRegisteredWithRegulatorController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "Excepted")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.CharityExceptedController.onPageLoad(CheckMode).url
          )
        }
      }

      "should redirect to CharityExceptedController when the value is Excepted in CheckMode for an agent" in {
        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ReasonNotRegisteredWithRegulatorController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "Excepted")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.CharityExceptedController.onPageLoad(CheckMode).url
          )
        }
      }

      "should redirect to CharityExemptController when the value is Exempt in CheckMode for an organisation" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ReasonNotRegisteredWithRegulatorController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "Exempt")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.CharityExemptController.onPageLoad(CheckMode).url
          )
        }
      }

      "should redirect to CharityExemptController when the value is Exempt in CheckMode for an agent" in {
        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ReasonNotRegisteredWithRegulatorController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "Exempt")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.CharityExemptController.onPageLoad(CheckMode).url
          )
        }
      }

      "should redirect to OrganisationDetailsCheckYourAnswersController when the value is lowIncome in CheckMode for an organisation" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ReasonNotRegisteredWithRegulatorController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "LowIncome")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "should redirect to OrganisationDetailsCheckYourAnswersController when the value is lowIncome in CheckMode for an agent" in {
        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ReasonNotRegisteredWithRegulatorController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "LowIncome")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "should redirect to OrganisationDetailsCheckYourAnswersController when the value is waiting in CheckMode for an organisation" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ReasonNotRegisteredWithRegulatorController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "Waiting")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "should redirect to OrganisationDetailsCheckYourAnswersController when the value is waiting in CheckMode for an agent" in {
        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ReasonNotRegisteredWithRegulatorController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "Waiting")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "should reload the page with errors when a required field is missing" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ReasonNotRegisteredWithRegulatorController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }
    }
  }
}

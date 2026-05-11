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

import controllers.ControllerSpec
import forms.RadioListFormProvider
import models.Mode.*
import models.{SessionData, WhoShouldHmrcSendPaymentTo}
import play.api.Application
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup
import views.html.WhoShouldWeSendPaymentToView

class WhoShouldWeSendPaymentToControllerSpec extends ControllerSpec {

  val formProvider                           = new RadioListFormProvider()
  val form: Form[WhoShouldHmrcSendPaymentTo] =
    formProvider("whoShouldWeSendPaymentTo.error.required")

  "WhoShouldWeSendPaymentToController" - {

    "onPageLoad" - {
      "should redirect to Claims List for Organisation user since Agent only screen" in {
        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Organisation).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.WhoShouldWeSendPaymentToController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }
      "should redirect to Claims List for Organisation user since Agent only screen - checkmode" in {
        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Organisation).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.WhoShouldWeSendPaymentToController.onPageLoad(CheckMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should redirect to ClaimCompleteController if submissionReference is defined" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          submissionReference = Some(testCharitiesReference)
        )

        given application: Application =
          applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.WhoShouldWeSendPaymentToController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }

      "should redirect to ClaimsTaskListController if user is not agent" in {
        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Organisation).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.WhoShouldWeSendPaymentToController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should render page for agent user" in {
        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.WhoShouldWeSendPaymentToController.onPageLoad(NormalMode).url)

          val result   = route(application, request).value
          val view     = application.injector.instanceOf[WhoShouldWeSendPaymentToView]
          val messages = application.injector.instanceOf[MessagesApi].preferred(request)

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual
            view(
              form,
              NormalMode
            )(using request, messages).body
        }
      }
    }

    "onSubmit" - {
      "should redirect to Claims List for Organisation user since Agent only screen" in {
        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Organisation).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.WhoShouldWeSendPaymentToController.onSubmit(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }
      "should redirect to Claims List for Organisation user since Agent only screen - checkmode" in {
        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Organisation).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.WhoShouldWeSendPaymentToController.onSubmit(CheckMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should redirect to ClaimCompleteController if submissionReference is defined" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          submissionReference = Some(testCharitiesReference)
        )

        given application: Application =
          applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request =
            FakeRequest(POST, routes.WhoShouldWeSendPaymentToController.onSubmit(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }

      "should redirect when valid data is submitted" in {
        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).mockSaveSession
            .build()

        running(application) {
          val request =
            FakeRequest(POST, routes.WhoShouldWeSendPaymentToController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody(
                "value" -> WhoShouldHmrcSendPaymentTo.AgentOrNominee.value
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.EnterTelephoneNumberController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect CYA when valid data is submitted in checkmode" in {
        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).mockSaveSession
            .build()

        running(application) {
          val request =
            FakeRequest(POST, routes.WhoShouldWeSendPaymentToController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody(
                "value" -> WhoShouldHmrcSendPaymentTo.AgentOrNominee.value
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "should return BadRequest when no option selected" in {
        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          val request =
            FakeRequest(POST, routes.WhoShouldWeSendPaymentToController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody()

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }
    }
  }
}

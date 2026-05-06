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
import forms.PhoneNumberFormProvider
import models.Mode.*
import models.SessionData
import play.api.Application
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup
import views.html.EnterTelephoneNumberView

class EnterTelephoneNumberControllerSpec extends ControllerSpec {
  val formProvider       = new PhoneNumberFormProvider()
  val form: Form[String] = formProvider()

  "EnterTelephoneNumberController" - {

    "onPageLoad" - {

      "should redirect to ClaimCompleteController if submissionReference is defined" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          submissionReference = Some(testCharitiesReference)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.EnterTelephoneNumberController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }

      "should redirect to ClaimsTaskListController if user is not agent" in {
        val sessionData = completeRepaymentDetailsAnswersSession.copy()

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Organisation).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.EnterTelephoneNumberController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should render the page correctly for agent user" in {
        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.EnterTelephoneNumberController.onPageLoad(NormalMode).url)

          val result   = route(application, request).value
          val view     = application.injector.instanceOf[EnterTelephoneNumberView]
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

      "should redirect to ClaimCompleteController if submissionReference is defined" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          submissionReference = Some(testCharitiesReference)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request =
            FakeRequest(POST, routes.EnterTelephoneNumberController.onSubmit(NormalMode).url)

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
            FakeRequest(POST, routes.EnterTelephoneNumberController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "1234567890")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            "/charities-claims/do-you-have-a-uk-address"
          ) // TODO: Integrate this page once the corresponding page is implemented
        }
      }

      "should return BadRequest when invalid data is submitted" in {
        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          val request =
            FakeRequest(POST, routes.EnterTelephoneNumberController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "invalid")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      "should return BadRequest when empty data is submitted" in {
        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          val request =
            FakeRequest(POST, routes.EnterTelephoneNumberController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }
    }
  }
}

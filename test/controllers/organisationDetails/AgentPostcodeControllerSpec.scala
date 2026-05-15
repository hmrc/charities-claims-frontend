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

import play.api.mvc.AnyContentAsEmpty
import controllers.ControllerSpec
import views.html.AgentPostcodeView
import uk.gov.hmrc.auth.core.AffinityGroup
import play.api.Application
import forms.PhoneNumberFormProvider
import models.{AgentUserOrganisationDetailsAnswers, SessionData}
import play.api.i18n.MessagesApi
import play.api.data.Form
import play.api.test.FakeRequest
import models.Mode.*

class AgentPostcodeControllerSpec extends ControllerSpec {

  val formProvider       = new PhoneNumberFormProvider()
  val form: Form[String] = formProvider()

  "AgentPostcodeController" - {

    "onPageLoad" - {

      "should redirect to Claims List for Organisation user since Agent only screen" in {

        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(
            sessionData = sessionData,
            affinityGroup = AffinityGroup.Organisation
          ).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AgentPostcodeController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should redirect to Claims List for Organisation user since Agent only screen - checkmode" in {

        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(
            sessionData = sessionData,
            affinityGroup = AffinityGroup.Organisation
          ).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AgentPostcodeController.onPageLoad(CheckMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
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
            FakeRequest(GET, routes.AgentPostcodeController.onPageLoad(NormalMode).url)

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
          applicationBuilder(
            sessionData = sessionData,
            affinityGroup = AffinityGroup.Organisation
          ).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AgentPostcodeController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should redirect to ClaimsTaskListController if UK postcode = false" in {

        val sessionData = completeRepaymentDetailsAnswersSession.and(
          AgentUserOrganisationDetailsAnswers.setDoYouHaveAgentUKAddress(
            value = false,
            previousAnswer = None
          )
        )

        given application: Application =
          applicationBuilder(
            sessionData = sessionData,
            affinityGroup = AffinityGroup.Agent
          ).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AgentPostcodeController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should render the page correctly for agent user" in {

        val sessionData = completeRepaymentDetailsAnswersSession.and(
          AgentUserOrganisationDetailsAnswers.setDoYouHaveAgentUKAddress(
            value = true,
            previousAnswer = None
          )
        )

        given application: Application =
          applicationBuilder(
            sessionData = sessionData,
            affinityGroup = AffinityGroup.Agent
          ).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AgentPostcodeController.onPageLoad(NormalMode).url)

          val result   = route(application, request).value
          val view     = application.injector.instanceOf[AgentPostcodeView]
          val messages = application.injector.instanceOf[MessagesApi].preferred(request)

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual
            view(form, NormalMode)(using request, messages).body
        }
      }
    }

    "onSubmit" - {

      "should redirect to Claims List for Organisation user since Agent only screen" in {

        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(
            sessionData = sessionData,
            affinityGroup = AffinityGroup.Organisation
          ).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.AgentPostcodeController.onSubmit(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should redirect to Claims List for Organisation user since Agent only screen - checkmode" in {

        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(
            sessionData = sessionData,
            affinityGroup = AffinityGroup.Organisation
          ).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.AgentPostcodeController.onSubmit(CheckMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
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
            FakeRequest(POST, routes.AgentPostcodeController.onSubmit(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }

      "should redirect when valid data is submitted" in {

        val sessionData = completeRepaymentDetailsAnswersSession.and(
          AgentUserOrganisationDetailsAnswers.setDoYouHaveAgentUKAddress(
            value = true,
            previousAnswer = None
          )
        )

        given application: Application =
          applicationBuilder(
            sessionData = sessionData,
            affinityGroup = AffinityGroup.Agent
          ).mockSaveSession.build()

        running(application) {

          val request =
            FakeRequest(POST, routes.AgentPostcodeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "NE27 0QQ")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result) shouldEqual Some(
            routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "should return BadRequest when invalid data is submitted" in {

        val sessionData = completeRepaymentDetailsAnswersSession.and(
          AgentUserOrganisationDetailsAnswers.setDoYouHaveAgentUKAddress(
            value = true,
            previousAnswer = None
          )
        )

        given application: Application =
          applicationBuilder(
            sessionData = sessionData,
            affinityGroup = AffinityGroup.Agent
          ).build()

        running(application) {

          val request =
            FakeRequest(POST, routes.EnterTelephoneNumberController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "invalid")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      "should return BadRequest when empty data is submitted" in {

        val sessionData = completeRepaymentDetailsAnswersSession.and(
          AgentUserOrganisationDetailsAnswers.setDoYouHaveAgentUKAddress(
            value = true,
            previousAnswer = None
          )
        )

        given application: Application =
          applicationBuilder(
            sessionData = sessionData,
            affinityGroup = AffinityGroup.Agent
          ).build()

        running(application) {

          val request =
            FakeRequest(POST, routes.AgentPostcodeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }
    }
  }
}

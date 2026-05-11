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
import forms.CharityRegulatorNumberFormProvider
import models.Mode.*
import models.NameOfCharityRegulator.*
import models.{AgentUserOrganisationDetailsAnswers, OrganisationDetailsAnswers, SessionData}
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup
import views.html.CharityRegulatorNumberView

class CharityRegulatorNumberControllerSpec extends ControllerSpec {

  private val formProvider = new CharityRegulatorNumberFormProvider()
  private val form         = formProvider()

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

      Seq("CH-test-ref", "CF-test-ref").foreach { ref =>
        s"should redirect to ClaimsTaskListController for CASC ref $ref" in {

          val sessionData = SessionData(
            charitiesReference = ref,
            unsubmittedClaimId = Some("test-claim-id"),
            repaymentClaimDetailsAnswers = Some(
              completeRepaymentClaimDetailsAnswers
            )
          )

          given application: Application =
            applicationBuilder(sessionData = sessionData).build()

          running(application) {

            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(
                GET,
                routes.CharityRegulatorNumberController
                  .onPageLoad(NormalMode)
                  .url
              )

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER

            redirectLocation(result) shouldEqual Some(
              controllers.routes.ClaimsTaskListController.onPageLoad.url
            )
          }
        }
      }

      Seq(EnglandAndWales, NorthernIreland, Scottish).foreach { regulator =>
        s"should render page for regulator $regulator" in {

          val sessionData =
            completeRepaymentDetailsAnswersSession.and(
              OrganisationDetailsAnswers.setNameOfCharityRegulator(regulator)
            )

          given application: Application =
            applicationBuilder(sessionData = sessionData).build()

          running(application) {

            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(
                GET,
                routes.CharityRegulatorNumberController
                  .onPageLoad(NormalMode)
                  .url
              )

            val result   = route(application, request).value
            val view     = application.injector.instanceOf[CharityRegulatorNumberView]
            val messages =
              application.injector.instanceOf[MessagesApi].preferred(request)

            status(result) shouldEqual OK

            contentAsString(result) shouldEqual
              view(form, NormalMode)(using request, messages).body
          }
        }
      }

      "should populate existing answer into form" in {

        val sessionData =
          completeRepaymentDetailsAnswersSession
            .and(
              OrganisationDetailsAnswers.setNameOfCharityRegulator(
                EnglandAndWales
              )
            )
            .and(
              OrganisationDetailsAnswers.setCharityRegistrationNumber(
                "123456"
              )
            )

        given application: Application =
          applicationBuilder(sessionData = sessionData).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(
              GET,
              routes.CharityRegulatorNumberController.onPageLoad(NormalMode).url
            )

          val result   = route(application, request).value
          val view     = application.injector.instanceOf[CharityRegulatorNumberView]
          val messages =
            application.injector.instanceOf[MessagesApi].preferred(request)

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual
            view(
              form.fill("123456"),
              NormalMode
            )(using request, messages).body
        }
      }

      "should redirect to task list page when regulator is None" in {

        val sessionData =
          completeRepaymentDetailsAnswersSession.and(
            OrganisationDetailsAnswers.setNameOfCharityRegulator(None)
          )

        given application: Application =
          applicationBuilder(sessionData = sessionData).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(
              GET,
              routes.CharityRegulatorNumberController.onPageLoad(NormalMode).url
            )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should redirect to task list page when regulator missing" in {

        given application: Application =
          applicationBuilder(
            sessionData = completeRepaymentDetailsAnswersSession
          ).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(
              GET,
              routes.CharityRegulatorNumberController.onPageLoad(NormalMode).url
            )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should support agent journey" in {
        val sessionData =
          completeRepaymentDetailsAnswersSession.and(
            AgentUserOrganisationDetailsAnswers
              .setNameOfCharityRegulator(EnglandAndWales)
          )

        given application: Application =
          applicationBuilder(
            sessionData = sessionData,
            affinityGroup = AffinityGroup.Agent
          ).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(
              GET,
              routes.CharityRegulatorNumberController.onPageLoad(NormalMode).url
            )

          val result = route(application, request).value

          status(result) shouldEqual OK
        }
      }
    }

    "onSubmit" - {

      "should redirect to ClaimCompleteController when submissionReference exists" in {

        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          lastUpdatedReference = Some(testCharitiesReference),
          submissionReference = Some(testCharitiesReference)
        )

        given application: Application =
          applicationBuilder(sessionData = sessionData).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(
              POST,
              routes.CharityRegulatorNumberController.onSubmit(NormalMode).url
            )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }

      "should redirect to CorporateTrusteeClaimController in normal mode" in {

        given application: Application =
          applicationBuilder(
            sessionData = completeRepaymentDetailsAnswersSession
          ).mockSaveSession.build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              routes.CharityRegulatorNumberController
                .onSubmit(NormalMode)
                .url
            ).withFormUrlEncodedBody(
              "value" -> "12345678"
            )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result) shouldEqual Some(
            routes.CorporateTrusteeClaimController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect agent users to WhoShouldWeSendPaymentToController" in {

        given application: Application =
          applicationBuilder(
            sessionData = completeRepaymentDetailsAnswersSession,
            affinityGroup = AffinityGroup.Agent
          ).mockSaveSession.build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              routes.CharityRegulatorNumberController
                .onSubmit(NormalMode)
                .url
            ).withFormUrlEncodedBody(
              "value" -> "12345678"
            )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result) shouldEqual Some(
            routes.WhoShouldWeSendPaymentToController.onSubmit(NormalMode).url
          )
        }
      }

      "should redirect to CYA in check mode" in {
        given application: Application =
          applicationBuilder(
            sessionData = completeRepaymentDetailsAnswersSession
          ).mockSaveSession.build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              routes.CharityRegulatorNumberController
                .onSubmit(CheckMode)
                .url
            ).withFormUrlEncodedBody(
              "value" -> "12345678"
            )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result) shouldEqual Some(
            routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "should return BAD_REQUEST for invalid data" in {

        given application: Application =
          applicationBuilder(
            sessionData = completeRepaymentDetailsAnswersSession
          ).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              routes.CharityRegulatorNumberController.onSubmit(NormalMode).url
            ).withFormUrlEncodedBody(
              "value" -> "ABC123"
            )

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      "should return BAD_REQUEST for empty value" in {

        given application: Application =
          applicationBuilder(
            sessionData = completeRepaymentDetailsAnswersSession
          ).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              routes.CharityRegulatorNumberController.onSubmit(NormalMode).url
            ).withFormUrlEncodedBody(
              "value" -> ""
            )

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }
    }

    "nextPage" - {

      "should return agent next page who should we send payment to" in {

        CharityRegulatorNumberController.nextPage(
          NormalMode,
          isAgent = true
        ) shouldEqual
          routes.WhoShouldWeSendPaymentToController.onSubmit(
            NormalMode
          )
      }

      "should return non-agent next page corporate trustee claim" in {

        CharityRegulatorNumberController.nextPage(
          NormalMode,
          isAgent = false
        ) shouldEqual
          routes.CorporateTrusteeClaimController.onPageLoad(
            NormalMode
          )
      }

      "should return CYA page in check mode" in {

        CharityRegulatorNumberController.nextPage(
          CheckMode,
          isAgent = true
        ) shouldEqual
          routes.OrganisationDetailsCheckYourAnswersController.onPageLoad
      }
    }
  }
}

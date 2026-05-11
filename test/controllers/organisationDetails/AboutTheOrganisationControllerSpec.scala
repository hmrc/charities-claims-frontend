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
import models.Mode.NormalMode
import models.SessionData
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup
import views.html.AboutTheOrganisationView

class AboutTheOrganisationControllerSpec extends ControllerSpec {

  "AboutTheOrganisationController" - {

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
            FakeRequest(GET, routes.AboutTheOrganisationController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }

      "should render the page correctly for an organisation" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AboutTheOrganisationController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AboutTheOrganisationView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(false).body
        }
      }

      "should render the page correctly for an agent" in {

        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AboutTheOrganisationController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AboutTheOrganisationView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(true).body
        }
      }

      "should render ClaimsTaskListController if completeRepaymentDetailsAnswersSession is false" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AboutTheOrganisationController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
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

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.AboutTheOrganisationController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }

      "should redirect to NameOfCharityRegulatorController for non-agent non-CASC" in {

        val sessionData = completeRepaymentDetailsAnswersSession.copy(
          charitiesReference = "test-ref-AB"
        )

        given application: Application =
          applicationBuilder(sessionData = sessionData).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.AboutTheOrganisationController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result) shouldEqual Some(
            routes.NameOfCharityRegulatorController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to CorporateTrusteeClaimController for CH reference" in {

        val sessionData = SessionData(
          charitiesReference = "CH-test-charities-ref",
          unsubmittedClaimId = Some("test-claim-id"),
          repaymentClaimDetailsAnswers = Some(completeRepaymentClaimDetailsAnswers)
        )

        given application: Application =
          applicationBuilder(sessionData = sessionData).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.AboutTheOrganisationController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result) shouldEqual Some(
            routes.CorporateTrusteeClaimController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to CorporateTrusteeClaimController for CF reference" in {

        val sessionData = SessionData(
          charitiesReference = "CF-test-charities-ref",
          unsubmittedClaimId = Some("test-claim-id"),
          repaymentClaimDetailsAnswers = Some(completeRepaymentClaimDetailsAnswers)
        )

        given application: Application =
          applicationBuilder(sessionData = sessionData).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.AboutTheOrganisationController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result) shouldEqual Some(
            routes.CorporateTrusteeClaimController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect agent CASC users to WhoShouldWeSendPaymentToController" in {

        val sessionData = completeRepaymentDetailsAnswersSession.copy(
          charitiesReference = "CH-test-charities-ref"
        )

        given application: Application =
          applicationBuilder(
            sessionData = sessionData,
            affinityGroup = AffinityGroup.Agent
          ).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.AboutTheOrganisationController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result) shouldEqual Some(
            routes.WhoShouldWeSendPaymentToController.onSubmit(NormalMode).url
          )
        }
      }

      "should redirect to ClaimsTaskListController when repayment details incomplete" in {

        given application: Application =
          applicationBuilder().build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.AboutTheOrganisationController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }
    }

    "nextPage" - {

      "should route agent CASC users correctly" in {

        AboutTheOrganisationController.nextPage(
          isAgent = true,
          isCASCCharityReference = true
        ) shouldEqual
          routes.WhoShouldWeSendPaymentToController.onSubmit(NormalMode)
      }

      "should route non-agent CASC users correctly" in {

        AboutTheOrganisationController.nextPage(
          isAgent = false,
          isCASCCharityReference = true
        ) shouldEqual
          routes.CorporateTrusteeClaimController.onPageLoad(NormalMode)
      }

      "should route all other users correctly" in {

        AboutTheOrganisationController.nextPage(
          isAgent = false,
          isCASCCharityReference = false
        ) shouldEqual
          routes.NameOfCharityRegulatorController.onPageLoad(NormalMode)
      }
    }
  }
}

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
import models.Mode.NormalMode
import models.SessionData
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.RepaymentClaimDetailsView
import uk.gov.hmrc.auth.core.AffinityGroup

class RepaymentClaimDetailsControllerSpec extends ControllerSpec {

  "RepaymentClaimDetailsController" - {
    "onPageLoad" - {
      "should render the page correctly for an organisation" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RepaymentClaimDetailsController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[RepaymentClaimDetailsView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(false).body
        }
      }

      "should render the page correctly for an agent" in {
        given application: Application = applicationBuilder(affinityGroup = AffinityGroup.Agent).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RepaymentClaimDetailsController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[RepaymentClaimDetailsView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(true).body
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
            FakeRequest(GET, routes.RepaymentClaimDetailsController.onPageLoad.url)

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
            FakeRequest(POST, routes.RepaymentClaimDetailsController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }

      "should redirect to the next page (R1.1) for an organisation" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.RepaymentClaimDetailsController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.RepaymentClaimTypeController.onPageLoad(NormalMode).url)
        }
      }

      "should redirect to the next page (R1.9) for an agent" in {
        given application: Application = applicationBuilder(affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.RepaymentClaimDetailsController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.CharitiesReferenceNumberInputController.onPageLoad(NormalMode).url
          )
        }
      }
    }
  }
}

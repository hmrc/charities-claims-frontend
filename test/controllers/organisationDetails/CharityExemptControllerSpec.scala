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
import models.OrganisationDetailsAnswers
import models.ReasonNotRegisteredWithRegulator.*
import models.Mode.*
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.CharityExemptView

class CharityExemptControllerSpec extends ControllerSpec {

  "CharityExemptController" - {
    "onPageLoad" - {
      "should render the page correctly if exempt" in {
        val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(Exempt)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CharityExemptController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[CharityExemptView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(NormalMode).body
        }
      }

      "should render the page not found if ReasonNotRegisteredWithRegulator is excepted" in {
        val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(Excepted)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CharityExemptController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.PageNotFoundController.onPageLoad.url)
        }
      }

      "should render the page not found if ReasonNotRegisteredWithRegulator is LowIncome" in {
        val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(LowIncome)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CharityExemptController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.PageNotFoundController.onPageLoad.url)
        }
      }

      "should render the page not found if ReasonNotRegisteredWithRegulator is Waiting" in {
        val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(Waiting)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CharityExemptController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.PageNotFoundController.onPageLoad.url)
        }
      }
    }

    "onSubmit" - {
      "in NormalMode should redirect to CorporateTrusteeClaimController" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.CharityExemptController.onSubmit(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.CorporateTrusteeClaimController.onPageLoad(NormalMode).url)
        }
      }

      "in CheckMode should redirect to CYA" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.CharityExemptController.onSubmit(CheckMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)
        }
      }
    }
  }
}

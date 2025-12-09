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
import models.{OrganisationDetailsAnswers, ReasonNotRegisteredWithRegulator}
import play.api.data.Form
import models.Mode.*
import models.ReasonNotRegisteredWithRegulator.*

class ReasonNotRegisteredWithRegulatorControllerSpec extends ControllerSpec {

  private val form: Form[ReasonNotRegisteredWithRegulator] = new RadioListFormProvider()(
    "reasonNotRegisteredWithRegulator.error.required"
  )

  "ReasonNotRegisteredWithRegulatorController" - {
    "onPageLoad" - {
      "should render the page correctly" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ReasonNotRegisteredWithRegulatorController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ReasonNotRegisteredWithRegulatorView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with lowIncome value" in {

        val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(LowIncome)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

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

      "should render the page and pre-populate correctly with excepted value" in {

        val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(Excepted)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ReasonNotRegisteredWithRegulatorController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ReasonNotRegisteredWithRegulatorView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(Excepted), NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with exempted value" in {

        val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(Exempt)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ReasonNotRegisteredWithRegulatorController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ReasonNotRegisteredWithRegulatorView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(Exempt), NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with waiting value" in {

        val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(Waiting)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

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
      "should redirect to the next page when the value is excepted" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ReasonNotRegisteredWithRegulatorController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "Excepted")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.CharityExceptedController.onPageLoad.url
          )
        }
      }

      "should reload the page with errors when a required field is missing" in {
        given application: Application = applicationBuilder().build()

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

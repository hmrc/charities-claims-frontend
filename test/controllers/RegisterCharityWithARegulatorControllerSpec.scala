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

package controllers

import controllers.ControllerSpec
import forms.YesNoFormProvider
import models.{OrganisationDetailsAnswers, ReasonNotRegisteredWithRegulator, SessionData}
import play.api.Application
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest

class RegisterCharityWithARegulatorControllerSpec extends ControllerSpec {

  val form: Form[Boolean] = new YesNoFormProvider()()

  val sessionDataLowIncome: SessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
    ReasonNotRegisteredWithRegulator.LowIncome
  )(using SessionData.empty(testCharitiesReference))

  val sessionDataExcepted: SessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
    ReasonNotRegisteredWithRegulator.Excepted
  )(using SessionData.empty(testCharitiesReference))

  "RegisterCharityWithARegulatorController" - {
    "onPageLoad" - {
      "should render the page with LowIncome limit (£5,000) for LowIncome charity" in {
        given application: Application = applicationBuilder(sessionData = sessionDataLowIncome).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RegisterCharityWithARegulatorController.onPageLoad.url)

          val result = route(application, request).value

          status(result)        shouldBe OK
          contentAsString(result) should include("5,000")
        }
      }

      "should render the page with Excepted limit (£100,000) for Excepted charity" in {
        given application: Application = applicationBuilder(sessionData = sessionDataExcepted).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RegisterCharityWithARegulatorController.onPageLoad.url)

          val result = route(application, request).value

          status(result)        shouldBe OK
          contentAsString(result) should include("100,000")
        }
      }

      "should render the page with default limit (£100,000) when charity type is not set" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RegisterCharityWithARegulatorController.onPageLoad.url)

          val result = route(application, request).value

          status(result)        shouldBe OK
          contentAsString(result) should include("100,000")
        }
      }
    }

    "onSubmit" - {
      "should reload the page with errors when a required field is missing" in {
        given application: Application = applicationBuilder(sessionData = sessionDataExcepted).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RegisterCharityWithARegulatorController.onSubmit.url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldBe BAD_REQUEST
        }
      }

      "should redirect to D3 screen when No is selected" in {
        given application: Application = applicationBuilder(sessionData = sessionDataExcepted).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RegisterCharityWithARegulatorController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.DeclarationDetailsConfirmationController.onPageLoad.url)
        }
      }

      "should redirect to R2 (Claims Task List) when Yes is selected" in {
        given application: Application = applicationBuilder(sessionData = sessionDataExcepted).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RegisterCharityWithARegulatorController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }
    }
  }
}

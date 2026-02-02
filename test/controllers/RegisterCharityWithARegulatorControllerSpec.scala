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

import config.FrontendAppConfig
import controllers.ControllerSpec
import forms.YesNoFormProvider
import play.api.Application
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import views.html.RegisterCharityWithARegulatorView

class RegisterCharityWithARegulatorControllerSpec extends ControllerSpec {

  val form: Form[Boolean] = new YesNoFormProvider()()

  "RegisterCharityWithARegulatorController" - {
    "onPageLoad" - {
      "should render the page correctly" in {

        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RegisterCharityWithARegulatorController.onPageLoad.url)

          val result    = route(application, request).value
          val view      = application.injector.instanceOf[RegisterCharityWithARegulatorView]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          val formattedLimit = java.text.DecimalFormat("#,###").format(appConfig.exceptedLimit)

          status(result)          shouldBe OK
          contentAsString(result) shouldBe view(appConfig.registerCharityWithARegulatorUrl, formattedLimit)(form).body
        }
      }
    }

    "onSubmit" - {
      "should reload the page with errors when a required field is missing" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RegisterCharityWithARegulatorController.onSubmit.url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldBe BAD_REQUEST
        }
      }

      // TODO: Update test when D3 screen route is completed (currently redirects to placeholder /declaration-details-confirmation)
      "should redirect to D3 screen when no is selected" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RegisterCharityWithARegulatorController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.DeclarationDetailsConfirmationController.onPageLoad.url)
        }
      }

      "should redirect to R2 when yes is selected" in {
        given application: Application = applicationBuilder().build()

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

      // TODO: Add more tests when F9 is implemented:

      // TODO: add test to check if under limit

      // TODO: add test to check if over high limit and is 'excepted and over £100k'

      // TODO: add test to check if over lower limit and is 'England & Wales regulated and over £5k'

      // TODO: add test to check if exactly at limit

      // TODO: check if no response from backend when checking F9

    }
  }
}

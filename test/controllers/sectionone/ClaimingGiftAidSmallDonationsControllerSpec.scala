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

package controllers.sectionone

import forms.YesNoFormProvider
import models.SessionData
import play.api.Application
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import views.html.ClaimingGiftAidSmallDonationsView
import controllers.ControllerSpec

class ClaimingGiftAidSmallDonationsControllerSpec extends ControllerSpec {

  private val form: Form[Boolean] = new YesNoFormProvider()()

  "ClaimingGiftAidSmallDonationsController" - {
    "onPageLoad" - {
      "should render the page correctly" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingGiftAidSmallDonationsController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimingGiftAidSmallDonationsView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form).body
        }
      }
      "should render the page and pre-populate correctly" in {

        val sessionData = SessionData.SectionOne.setClaimingUnderGasds(true)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingGiftAidSmallDonationsController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimingGiftAidSmallDonationsView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(true)).body
        }
      }
    }

    "onSubmit" - {
      "should redirect to the next page" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingGiftAidSmallDonationsController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
        }
      }

      "should reload the page with errors when a required field is missing" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingGiftAidSmallDonationsController.onSubmit.url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
        }
      }
    }
  }
}

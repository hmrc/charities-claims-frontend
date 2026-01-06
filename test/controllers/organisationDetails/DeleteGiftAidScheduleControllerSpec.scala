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
import forms.YesNoFormProvider
import play.api.Application
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import views.html.DeleteGiftAidScheduleView

class DeleteGiftAidScheduleControllerSpec extends ControllerSpec {

  private val form: Form[Boolean] = new YesNoFormProvider()()

  "DeleteGiftAidScheduleController" - {
    "onPageLoad" - {
      "should render the page correctly" in {

        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.DeleteGiftAidScheduleController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[DeleteGiftAidScheduleView]

          status(result)          shouldBe OK
          contentAsString(result) shouldBe view(form).body
        }
      }
    }

    "onSubmit" - {
      "should reload the page with errors when a required field true or false is missing" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteGiftAidScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldBe BAD_REQUEST
        }
      }

      // TODO: Add test when backend deletion endpoint is completed
      // "should call backend deletion endpoint and redirect to R2 when yes is selected" in {
      //   Test that:
      //   - Backend deletion endpoint is called with correct parameters
      //   - On successful deletion (response: { "success": true }), redirect to R2 screen
      //   - Gift Aid schedule data is actually deleted from database
      // }

      // TODO: Add test when G2 screen is completed
      // "should redirect to G2 screen when no is selected" in {
      //   Test that:
      //   - No deletion occurs
      //   - User is redirected to G2 (screen to add schedule data)
      //   - Gift Aid schedule data is still retained database
      // }

      // TODO: Add test when backend deletion endpoint is completed
      // "should handle backend deletion errors correctly" in {
      //   Test that:
      //   - If backend returns error (not { "success": true }), then error handling occurs
      //   - Error message is correctly returned, TBC handled on frontend
      // }
    }
  }
}

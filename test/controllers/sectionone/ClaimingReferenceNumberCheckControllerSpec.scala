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

import play.api.test.FakeRequest
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import controllers.ControllerSpec
import views.html.ClaimingReferenceNumberCheckView
import play.api.Application
import forms.YesNoFormProvider
import models.SessionData
import play.api.data.Form

class ClaimingReferenceNumberCheckControllerSpec extends ControllerSpec {

  private val form: Form[Boolean] = new YesNoFormProvider()()

  "ClaimingReferenceNumberCheckController" - {
    "onPageLoad" - {
      "should render the page correctly" in {

        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingReferenceNumberCheckController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimingReferenceNumberCheckView]

          status(result)          shouldBe OK
          contentAsString(result) shouldBe view(form).body
        }
      }

      "should render the page and pre-populate correctly" in {

        val sessionData = SessionData.SectionOne.setClaimingReferenceNumber(true)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingReferenceNumberCheckController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimingReferenceNumberCheckView]

          status(result)          shouldBe OK
          contentAsString(result) shouldBe view(form.fill(true)).body
        }
      }
    }

    "onSubmit" - {
      "should redirect to the next page" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingReferenceNumberCheckController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(
            controllers.sectionone.routes.ClaimReferenceNumberInputController.onPageLoad.url
          )
        }
      }

      "should reload the page with errors when a required field is missing" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingReferenceNumberCheckController.onSubmit.url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldBe BAD_REQUEST
        }
      }
    }
  }
}

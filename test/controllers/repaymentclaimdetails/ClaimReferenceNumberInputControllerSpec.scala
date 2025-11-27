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

package controllers.repaymentclaimdetails

import play.api.test.FakeRequest
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import controllers.ControllerSpec
import views.html.ClaimReferenceNumberInputView
import play.api.Application
import forms.TextInputFormProvider
import models.RepaymentClaimDetailsAnswers
import play.api.data.Form
import models.NormalMode

class ClaimReferenceNumberInputControllerSpec extends ControllerSpec {

  private val form: Form[String] = new TextInputFormProvider()(
    "claimReferenceNumberInput.error.required",
    (20, "claimReferenceNumberInput.error.length"),
    "claimReferenceNumberInput.error.regex"
  )

  "ClaimReferenceNumberInputController" - {
    "onPageLoad" - {
      "should render the page correctly" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimReferenceNumberInputController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimReferenceNumberInputView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, NormalMode).body
        }
      }
      "should render the page and pre-populate correctly" in {

        val sessionData = RepaymentClaimDetailsAnswers.setClaimReferenceNumber("123456")

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimReferenceNumberInputController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimReferenceNumberInputView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill("123456"), NormalMode).body
        }
      }
    }

    "onSubmit" - {
      "should redirect to the next page" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimReferenceNumberInputController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "123456")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.ClaimDeclarationController.onPageLoad.url)
        }
      }

      "should reload the page with errors when a required field is missing" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimReferenceNumberInputController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }
    }
  }
}

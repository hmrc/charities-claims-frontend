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
import play.api.Application
import play.api.data.Form
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.ClaimingGiftAidView
import controllers.ControllerSpec

class ClaimingGiftAidControllerSpec extends ControllerSpec {
  "ClaimingGiftAidController" - {
    "onPageLoad" - {
      "should render the page correctly" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingGiftAidController.onPageLoad.url)
          val form: Form[Boolean]                            = new YesNoFormProvider()()

          val result = route(application, request).value

          val view = application.injector.instanceOf[ClaimingGiftAidView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual view(form).body
        }
      }
    }
  }
}

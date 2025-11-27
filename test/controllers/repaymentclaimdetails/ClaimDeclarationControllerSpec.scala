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

import controllers.ControllerSpec
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.ClaimDeclarationView
import models.NormalMode

class ClaimDeclarationControllerSpec extends ControllerSpec {
  "ClaimDeclarationController" - {
    "onPageLoad" - {
      "should render the page correctly" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimDeclarationController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ClaimDeclarationView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view().body
        }
      }
    }

    "onSubmit" - {
      "should redirect to the next page" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.ClaimDeclarationController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.CheckYourAnswersController.onPageLoad.url)
        }
      }
    }
  }
}

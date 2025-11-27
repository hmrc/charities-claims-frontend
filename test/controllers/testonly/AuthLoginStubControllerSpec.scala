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

package controllers.testonly

import play.api.test.FakeRequest
import play.api.mvc.AnyContentAsEmpty
import controllers.ControllerSpec
import play.api.Application
import views.html.testonly.AuthLoginStubView
import play.api.mvc.Call

class AuthLoginStubControllerSpec extends ControllerSpec {

  "AuthLoginStubController" - {
    "onPageLoad" - {
      "should return OK" in {
        given application: Application =
          applicationBuilder()
            .configure(
              "application.router" -> "testOnlyDoNotUseInAppConf.Routes"
            )
            .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AuthLoginStubController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AuthLoginStubView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(
            userId = "1234567890",
            continueUrl = "http://localhost:8030/charities-claims",
            postAction = Call("POST", "http://localhost:9949/auth-login-stub/gg-sign-in")
          ).body
        }
      }
    }
  }
}

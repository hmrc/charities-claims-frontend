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

package controllers

import play.api.test.FakeRequest
import play.api.mvc.AnyContentAsEmpty
import play.api.Application
import views.html.ErrorView
import config.FrontendAppConfig

class MaxClaimsExceededControllerSpec extends ControllerSpec {

  "MaxClaimsExceededController" - {

    "onPageLoad" - {
      "should render the page correctly" in {
        given application: Application = applicationBuilder().build()
        val view                       = application.injector.instanceOf[ErrorView]
        val appConfig                  = application.injector.instanceOf[FrontendAppConfig]

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.MaxClaimsExceededController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(
            pageTitle = "maxClaimsExceeded.title",
            heading = "maxClaimsExceeded.heading",
            message = "maxClaimsExceeded.paragraph",
            messageArgs = Seq(appConfig.makeCharityRepaymentClaimUrl)
          ).body
        }
      }
    }

    "should use the correct configured URL in the message" in {
      val customConfig = Map(
        "urls.makeCharityRepaymentClaimUrl" -> "https://test.example.com/claims"
      )

      given application: Application = applicationBuilder()
        .configure(customConfig)
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.MaxClaimsExceededController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) shouldEqual OK
        contentAsString(result) should include("https://test.example.com/claims")
      }
    }
  }
}

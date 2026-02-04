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

package controllers.giftAidSchedule

import controllers.ControllerSpec
import controllers.giftAidSchedule.routes
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.AboutGiftAidScheduleView

class AboutGiftAidScheduleControllerSpec extends ControllerSpec {
  "AboutGiftAidScheduleController" - {
    "onPageLoad" - {
      "should render the page correctly" in {
//        val customConfig               = Map(
//          "urls.giftAidScheduleSpreadsheetsToClaimBackTaxOnDonationsUrl" -> "https://test.example.com/charity-repayment-claim"
//        )
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AboutGiftAidScheduleController.onPageLoad.url)

          val result = route(application, request).value

          // val view = application.injector.instanceOf[AboutGiftAidScheduleView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual include("Use this service to add a Gift Aid schedule.")
        }
      }

      "should use the correct configured giftAidScheduleSpreadsheetsToClaimBackTaxOnDonationsUrl in the message" in {
        val customConfig = Map(
          "urls.giftAidScheduleSpreadsheetsToClaimBackTaxOnDonationsUrl" -> "https://test.example.com/charity-repayment-claim"
        )

        given application: Application = applicationBuilder()
          .configure(customConfig)
          .build()

        running(application) {
          val request =
            FakeRequest(GET, routes.AboutGiftAidScheduleController.onPageLoad.url)
          val result  = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include("https://test.example.com/charity-repayment-claim")
        }
      }
    }

    "onSubmit" - {
      "should redirect to the next page" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.AboutGiftAidScheduleController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.UploadGiftAidScheduleController.onPageLoad.url)
        }
      }
    }
  }
}

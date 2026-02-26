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
import models.RepaymentClaimDetailsAnswers
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
class AboutGiftAidScheduleControllerSpec extends ControllerSpec {
  "AboutGiftAidScheduleController" - {
    "onPageLoad" - {

      "should render ClaimsTaskListController if setClaimingGiftAid is false" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingGiftAid(false)
        val customConfig               = Map(
          "urls.giftAidScheduleSpreadsheetGuidanceUrl" -> "https://test.example.com/charity-repayment-claim"
        )
        given application: Application = applicationBuilder(sessionData = sessionData).configure(customConfig).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AboutGiftAidScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should use the correct configured giftAidScheduleSpreadsheetsToClaimBackTaxOnDonationsUrl in the message" in {
        val sessionData  = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
        val customConfig = Map(
          "urls.giftAidScheduleSpreadsheetGuidanceUrl" -> "https://test.example.com/charity-repayment-claim"
        )

        given application: Application = applicationBuilder(sessionData = sessionData)
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

      "should redirect to the next page if the giftAidScheduleCompleted is true" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
          .copy(giftAidScheduleCompleted = true)

        given application: Application = applicationBuilder(sessionData = sessionData).build()
        running(application) {
          val request =
            FakeRequest(GET, routes.AboutGiftAidScheduleController.onPageLoad.url)
          val result  = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.YourGiftAidScheduleUploadController.onPageLoad.url)
        }
      }
    }

    "onSubmit" - {
      "should redirect to the next page" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
        given application: Application = applicationBuilder(sessionData = sessionData).build()

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

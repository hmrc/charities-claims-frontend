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

package controllers.otherIncomeSchedule

import controllers.ControllerSpec
import controllers.otherIncomeSchedule.routes
import models.RepaymentClaimDetailsAnswers
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

class AboutOtherIncomeScheduleControllerSpec extends ControllerSpec {
  "AboutOtherIncomeScheduleController" - {
    "onPageLoad" - {

      "should return Page Not Found if setClaimingTaxDeducted is false" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingTaxDeducted(false)
        val customConfig               = Map(
          "urls.otherIncomeScheduleSpreadsheetsUrl" -> "https://test.example.com/other-income-schedule"
        )
        given application: Application = applicationBuilder(sessionData = sessionData).configure(customConfig).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AboutOtherIncomeScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.PageNotFoundController.onPageLoad.url)
        }
      }

      "should return OK if setClaimingTaxDeducted is true" in {
        val sessionData  = RepaymentClaimDetailsAnswers.setClaimingTaxDeducted(true)
        val customConfig = Map(
          "urls.otherIncomeScheduleSpreadsheetsUrl" -> "https://test.example.com/other-income-schedule"
        )

        given application: Application = applicationBuilder(sessionData = sessionData)
          .configure(customConfig)
          .build()

        running(application) {
          val request =
            FakeRequest(GET, routes.AboutOtherIncomeScheduleController.onPageLoad.url)
          val result  = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include("https://test.example.com/other-income-schedule")
        }
      }
    }

    "onSubmit" - {
      "should redirect to the next page" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.AboutOtherIncomeScheduleController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.UploadOtherIncomeScheduleController.onPageLoad.url)
        }
      }
    }
  }
}

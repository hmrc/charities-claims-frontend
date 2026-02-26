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

package controllers.connectedCharitiesSchedule

import controllers.ControllerSpec
import controllers.connectedCharitiesSchedule.routes
import models.RepaymentClaimDetailsAnswers
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

class AboutConnectedCharitiesScheduleControllerSpec extends ControllerSpec {
  "AboutConnectedCharitiesScheduleController" - {
    "onPageLoad" - {

      "should return ClaimsTaskListController if setConnectedToAnyOtherCharities is false" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setConnectedToAnyOtherCharities(false)
        val customConfig               = Map(
          "urls.connectedCharitiesScheduleSpreadsheetGuidanceUrl" -> "https://test.example.com/connected-charities-schedule"
        )
        given application: Application = applicationBuilder(sessionData = sessionData).configure(customConfig).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AboutConnectedCharitiesScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should return OK if setConnectedToAnyOtherCharities is true" in {
        val sessionData  = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setConnectedToAnyOtherCharities(true))
        val customConfig = Map(
          "urls.connectedCharitiesScheduleSpreadsheetGuidanceUrl" -> "https://test.example.com/connected-charities-schedule"
        )

        given application: Application = applicationBuilder(sessionData = sessionData)
          .configure(customConfig)
          .build()

        running(application) {
          val request =
            FakeRequest(GET, routes.AboutConnectedCharitiesScheduleController.onPageLoad.url)
          val result  = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include("https://test.example.com/connected-charities-schedule")
        }
      }

      "should redirect to the next page if the connectedCharitiesScheduleCompleted is true" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setConnectedToAnyOtherCharities(true))
          .copy(connectedCharitiesScheduleCompleted = true)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request =
            FakeRequest(GET, routes.AboutConnectedCharitiesScheduleController.onPageLoad.url)
          val result  = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.YourConnectedCharitiesScheduleUploadController.onPageLoad.url
          )
        }
      }
    }

    "onSubmit" - {
      "should redirect to the next page" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setConnectedToAnyOtherCharities(true))
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.AboutConnectedCharitiesScheduleController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadConnectedCharitiesScheduleController.onPageLoad.url
          )
        }
      }
    }
  }
}

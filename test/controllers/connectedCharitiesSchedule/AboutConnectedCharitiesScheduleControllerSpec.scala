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
import models.{RepaymentClaimDetailsAnswers, SessionData}
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup

class AboutConnectedCharitiesScheduleControllerSpec extends ControllerSpec {
  "AboutConnectedCharitiesScheduleController" - {
    "onPageLoad" - {
      "should render ClaimCompleteController if submissionReference is defined" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          lastUpdatedReference = Some(testCharitiesReference),
          submissionReference = Some(testCharitiesReference)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AboutConnectedCharitiesScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
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

      "should render the page correctly when user is Organisation (isAgent = false)" in {
        val sessionData = completeGasdsSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Organisation).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AboutConnectedCharitiesScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual OK

          contentAsString(result)  should include(messages("aboutConnectedCharitiesSchedule.paragraph.one"))
          (contentAsString(result) should not).include(messages("aboutConnectedCharitiesSchedule.paragraph.one.agent"))
        }
      }

      "should render the page correctly when user is Agent (isAgent = true)" in {
        val sessionData = completeGasdsSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AboutConnectedCharitiesScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual OK

          contentAsString(result)  should include(messages("aboutConnectedCharitiesSchedule.paragraph.one.agent"))
          (contentAsString(result) should not).include(messages("aboutConnectedCharitiesSchedule.paragraph.one"))
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
      "should render ClaimCompleteController if submissionReference is defined" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          lastUpdatedReference = Some(testCharitiesReference),
          submissionReference = Some(testCharitiesReference)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.AboutConnectedCharitiesScheduleController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
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

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

package controllers.communityBuildingsSchedule

import play.api.test.FakeRequest
import play.api.mvc.AnyContentAsEmpty
import controllers.ControllerSpec
import views.html.ScheduleUploadFailureView
import play.api.Application
import models.{RepaymentClaimDetailsAnswers, SessionData}
import models.SessionData.and

class ProblemUpdatingCommunityBuildingsScheduleRejectedControllerSpec extends ControllerSpec {

  "ProblemUpdatingCommunityBuildingsScheduleRejectedController" - {
    "onPageLoad" - {
      "should render the page correctly" in {
        given application: Application = applicationBuilder(sessionData = completeGasdsSession)
          .build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemUpdatingCommunityBuildingsScheduleRejectedController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ScheduleUploadFailureView]
          val msgs   = application.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

          status(result) shouldBe OK

          contentAsString(result) shouldBe view(
            messagesKeyPrefix = "problemUpdatingCommunityBuildingsScheduleRejected",
            submitAction = routes.ProblemUpdatingCommunityBuildingsScheduleRejectedController.onSubmit,
            dashboardLink = controllers.routes.ClaimsTaskListController.onPageLoad
          )(using request, msgs).body
        }
      }

      "should decline when schedule checkbox is not selected" in {
        val sessionData =
          RepaymentClaimDetailsAnswers
            .setClaimingUnderGiftAidSmallDonationsScheme(false)
            .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(false, Some(true)))
            .and(SessionData.setUnsubmittedClaimId("claim-123"))

        val application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request =
            FakeRequest(GET, routes.ProblemUpdatingCommunityBuildingsScheduleRejectedController.onPageLoad.url)
          val result  = route(application, request).value
          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.PageNotFoundController.onPageLoad.url)
        }
      }

      "should decline when claim id is not in session" in {
        val sessionData =
          RepaymentClaimDetailsAnswers
            .setClaimingUnderGiftAidSmallDonationsScheme(true)

        val application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request =
            FakeRequest(GET, routes.ProblemUpdatingCommunityBuildingsScheduleRejectedController.onPageLoad.url)
          val result  = route(application, request).value
          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.PageNotFoundController.onPageLoad.url)
        }
      }
    }

    "onSubmit" - {
      "should redirect to the next page" in {
        val application: Application = applicationBuilder(sessionData = completeGasdsSession).build()

        running(application) {
          val request =
            FakeRequest(POST, routes.ProblemUpdatingCommunityBuildingsScheduleRejectedController.onSubmit.url)
          val result  = route(application, request).value
          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.UploadCommunityBuildingsScheduleController.onPageLoad.url)
        }
      }

      "should decline when schedule checkbox is not selected" in {
        val sessionData =
          RepaymentClaimDetailsAnswers
            .setClaimingUnderGiftAidSmallDonationsScheme(false)
            .and(SessionData.setUnsubmittedClaimId("claim-123"))

        val application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request =
            FakeRequest(POST, routes.ProblemUpdatingCommunityBuildingsScheduleRejectedController.onSubmit.url)
          val result  = route(application, request).value
          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.PageNotFoundController.onPageLoad.url)
        }
      }

      "should decline when claim id is not in session" in {
        val sessionData =
          RepaymentClaimDetailsAnswers
            .setClaimingUnderGiftAidSmallDonationsScheme(true)

        val application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request =
            FakeRequest(POST, routes.ProblemUpdatingCommunityBuildingsScheduleRejectedController.onSubmit.url)
          val result  = route(application, request).value
          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.PageNotFoundController.onPageLoad.url)
        }
      }
    }
  }
}

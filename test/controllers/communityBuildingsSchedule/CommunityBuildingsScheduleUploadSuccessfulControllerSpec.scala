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

import controllers.ControllerSpec
import controllers.communityBuildingsSchedule.routes
import models.{FileUploadReference, RepaymentClaimDetailsAnswers}
import play.api.Application
import play.api.i18n.{Lang, MessagesApi}
import play.api.test.FakeRequest
import util.TestScheduleData
import views.html.CommunityBuildingsScheduleUploadSuccessfulView

class CommunityBuildingsScheduleUploadSuccessfulControllerSpec extends ControllerSpec {

  "CommunityBuildingsScheduleUploadSuccessfulController" - {
    "onPageLoad" - {

      "should render page successfully" in {
        val sessionData = completeGasdsSession
          .copy(
            connectedCharitiesScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
            communityBuildingsScheduleData = Some(TestScheduleData.exampleCommunityBuildingsScheduleData)
          )

        val application = applicationBuilder(sessionData = sessionData).build()
        val view        = application.injector.instanceOf[CommunityBuildingsScheduleUploadSuccessfulView]
        val messages    = application.injector.instanceOf[MessagesApi].preferred(Seq(Lang("en")))

        running(application) {
          val request = FakeRequest(GET, routes.CommunityBuildingsScheduleUploadSuccessfulController.onPageLoad.url)

          val result = route(application, request).value
          status(result) shouldEqual OK
          contentAsString(result) shouldBe view()(using request, messages).body
        }
      }

      "should render ClaimsTaskListController when setClaimingUnderGiftAidSmallDonationsScheme = false" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(false))
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, Some(true)))
          .copy(
            connectedCharitiesScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
            communityBuildingsScheduleData = Some(TestScheduleData.exampleCommunityBuildingsScheduleData)
          )

        val application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request = FakeRequest(GET, routes.CommunityBuildingsScheduleUploadSuccessfulController.onPageLoad.url)

          val result = route(application, request).value
          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should render ClaimsTaskListController when setClaimingDonationsCollectedInCommunityBuildings = false" in {
        val sessionData              = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true))
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(false, Some(true)))
          .copy(
            connectedCharitiesScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
            communityBuildingsScheduleData = Some(TestScheduleData.exampleCommunityBuildingsScheduleData)
          )
        val application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request = FakeRequest(GET, routes.CommunityBuildingsScheduleUploadSuccessfulController.onPageLoad.url)

          val result = route(application, request).value
          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }
    }

    "onSubmit" - {
      "should redirect to the next page" in {
        val sessionData = completeGasdsSession
          .copy(
            connectedCharitiesScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference"))
          )

        val application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request = FakeRequest(POST, routes.CommunityBuildingsScheduleUploadSuccessfulController.onSubmit.url)

          val result = route(application, request).value
          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should redirect to ClaimsTaskListController when setClaimingUnderGiftAidSmallDonationsScheme= false" in {
        val sessionData              = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(false))
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, Some(true)))
          .copy(
            connectedCharitiesScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference"))
          )
        val application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request = FakeRequest(POST, routes.CommunityBuildingsScheduleUploadSuccessfulController.onSubmit.url)

          val result = route(application, request).value
          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should redirect to ClaimsTaskListController when setClaimingDonationsCollectedInCommunityBuildings= false" in {
        val sessionData              = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true))
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(false, Some(true)))
          .copy(
            connectedCharitiesScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference"))
          )
        val application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request = FakeRequest(POST, routes.CommunityBuildingsScheduleUploadSuccessfulController.onSubmit.url)

          val result = route(application, request).value
          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }
    }
  }
}

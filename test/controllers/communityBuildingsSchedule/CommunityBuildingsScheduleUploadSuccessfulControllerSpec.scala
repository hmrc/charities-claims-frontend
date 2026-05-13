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
import models.{FileUploadReference, RepaymentClaimDetailsAnswers, SessionData}
import play.api.Application
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import util.TestScheduleData
import views.html.CommunityBuildingsScheduleUploadSuccessfulView
import uk.gov.hmrc.auth.core.AffinityGroup

class CommunityBuildingsScheduleUploadSuccessfulControllerSpec extends ControllerSpec {

  "CommunityBuildingsScheduleUploadSuccessfulController" - {
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
            FakeRequest(GET, routes.CommunityBuildingsScheduleUploadSuccessfulController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
      "should render page successfully" in {
        val sessionData = completeGasdsSession
          .copy(
            connectedCharitiesScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
            communityBuildingsScheduleData = Some(TestScheduleData.exampleCommunityBuildingsScheduleData),
            communityBuildingsScheduleCompleted = true
          )

        val application = applicationBuilder(sessionData = sessionData).build()
        val view        = application.injector.instanceOf[CommunityBuildingsScheduleUploadSuccessfulView]
        val messages    = application.injector.instanceOf[MessagesApi].preferred(Seq(Lang("en")))

        running(application) {
          val request = FakeRequest(GET, routes.CommunityBuildingsScheduleUploadSuccessfulController.onPageLoad.url)

          val result = route(application, request).value
          status(result) shouldEqual OK
          contentAsString(result) shouldBe view(isAgent = false)(using request, messages).body
        }
      }

      "should render ClaimsTaskListController when communityBuildingsScheduleCompleted = false " in {
        val sessionData = completeGasdsSession
          .copy(
            connectedCharitiesScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
            communityBuildingsScheduleData = Some(TestScheduleData.exampleCommunityBuildingsScheduleData),
            communityBuildingsScheduleCompleted = false
          )

        val application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request = FakeRequest(GET, routes.CommunityBuildingsScheduleUploadSuccessfulController.onPageLoad.url)

          val result = route(application, request).value
          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
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

      "should redirect to ClaimsTaskListController (does not pass data guard controls) " in {

        val application = applicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, routes.CommunityBuildingsScheduleUploadSuccessfulController.onPageLoad.url)

          val result = route(application, request).value
          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should render content for organisation" in {
        val sessionData = completeGasdsSession
          .copy(
            connectedCharitiesScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
            communityBuildingsScheduleData = Some(TestScheduleData.exampleCommunityBuildingsScheduleData),
            communityBuildingsScheduleCompleted = true
          )

        val application = applicationBuilder(sessionData = sessionData, AffinityGroup.Organisation).build()
        // val view = application.injector.instanceOf[CommunityBuildingsScheduleUploadSuccessfulView]
        val messages    = application.injector.instanceOf[MessagesApi].preferred(Seq(Lang("en")))

        running(application) {
          val request = FakeRequest(GET, routes.CommunityBuildingsScheduleUploadSuccessfulController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include(messages("communityBuildingsScheduleUploadSuccessful.message.2"))
          content shouldNot include(messages("communityBuildingsScheduleUploadSuccessful.agent.message.2"))

        }

      }

      "should render content for agent" in {
        val sessionData = completeGasdsSession
          .copy(
            connectedCharitiesScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
            communityBuildingsScheduleData = Some(TestScheduleData.exampleCommunityBuildingsScheduleData),
            communityBuildingsScheduleCompleted = true
          )

        val application = applicationBuilder(sessionData = sessionData, AffinityGroup.Agent).build()
        // val view = application.injector.instanceOf[CommunityBuildingsScheduleUploadSuccessfulView]
        val messages    = application.injector.instanceOf[MessagesApi].preferred(Seq(Lang("en")))

        running(application) {
          val request = FakeRequest(GET, routes.CommunityBuildingsScheduleUploadSuccessfulController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include(messages("communityBuildingsScheduleUploadSuccessful.agent.message.2"))
          content shouldNot include(messages("communityBuildingsScheduleUploadSuccessful.message.2"))
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
              FakeRequest(POST, routes.CommunityBuildingsScheduleUploadSuccessfulController.onSubmit.url)

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
            )
          }
        }
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
}

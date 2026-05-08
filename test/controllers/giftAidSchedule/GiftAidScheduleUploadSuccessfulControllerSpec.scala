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
import models.{FileUploadReference, RepaymentClaimDetailsAnswers, SessionData}
import play.api.Application
import play.api.test.FakeRequest
import util.TestScheduleData
import views.html.GiftAidScheduleUploadSuccessfulView
import play.api.i18n.MessagesApi
import play.api.i18n.Lang
import play.api.mvc.AnyContentAsEmpty

class GiftAidScheduleUploadSuccessfulControllerSpec extends ControllerSpec {

  "GiftAidScheduleUploadSuccessfulControllerSpec" - {
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
            FakeRequest(GET, routes.GiftAidScheduleUploadSuccessfulController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
      "should render page successfully" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
          .copy(
            giftAidScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
            giftAidScheduleData = Some(TestScheduleData.exampleGiftAidScheduleData),
            giftAidScheduleCompleted = true
          )

        val application = applicationBuilder(sessionData = sessionData).build()
        val view        = application.injector.instanceOf[GiftAidScheduleUploadSuccessfulView]
        val messages    = application.injector.instanceOf[MessagesApi].preferred(Seq(Lang("en")))

        running(application) {
          val request = FakeRequest(GET, routes.GiftAidScheduleUploadSuccessfulController.onPageLoad.url)

          val result = route(application, request).value
          status(result) shouldEqual OK
          contentAsString(result) shouldBe view(false)(using request, messages).body
        }
      }

      "should redirect to ClaimsTaskListController (there is no validated file completion status) " in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
          .copy(
            giftAidScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
            giftAidScheduleData = Some(TestScheduleData.exampleGiftAidScheduleData)
          )

        val application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request = FakeRequest(GET, routes.GiftAidScheduleUploadSuccessfulController.onPageLoad.url)

          val result = route(application, request).value
          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should redirect to ClaimsTaskListController (does not pass data guard controls) " in {

        val application = applicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, routes.GiftAidScheduleUploadSuccessfulController.onPageLoad.url)

          val result = route(application, request).value
          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
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
            FakeRequest(POST, routes.GiftAidScheduleUploadSuccessfulController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
      "should redirect to the next page" in {
        val sessionData              = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
          .copy(giftAidScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")))
        val application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request = FakeRequest(POST, routes.GiftAidScheduleUploadSuccessfulController.onSubmit.url)

          val result = route(application, request).value
          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }
    }
  }
}

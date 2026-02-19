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
import models.RepaymentClaimDetailsAnswers
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import models.SessionData

class AboutCommunityBuildingScheduleControllerSpec extends ControllerSpec {
  "AboutCommunityBuildingsScheduleController" - {
    "onPageLoad" - {

      "should render Page Not Found if setClaimingCommunityBuildings is false" in {
        val sessionData                =
          RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(false, Some(true))
        val customConfig               = Map(
          "urls.communityBuildingsScheduleSpreadsheetsToClaimBackTaxOnDonationsUrl" -> "https://test.example.com/charity-repayment-claim"
        )
        given application: Application = applicationBuilder(sessionData = sessionData).configure(customConfig).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AboutCommunityBuildingsScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.PageNotFoundController.onPageLoad.url)
        }
      }

      "should render Page Not Found if setClaimingUnderGiftAidSmallDonationsScheme is false & setClaimingDonationsCollectedInCommunityBuildings is true" in {
        val sessionDataGASDS =
          RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(false)
        val sessionData      =
          RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, Some(true))(using
            sessionDataGASDS
          )
        val customConfig     = Map(
          "urls.communityBuildingsScheduleSpreadsheetsToClaimBackTaxOnDonationsUrl" -> "https://test.example.com/charity-repayment-claim"
        )

        given application: Application = applicationBuilder(sessionData = sessionData).configure(customConfig).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AboutCommunityBuildingsScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.PageNotFoundController.onPageLoad.url)
        }
      }

      "should use the correct configured communityBuildingsScheduleSpreadsheetsToClaimBackTaxOnDonationsUrl in the message" in {
        val sessionDataGASDS =
          RepaymentClaimDetailsAnswers
            .setClaimingUnderGiftAidSmallDonationsScheme(true)
            .and(SessionData.setUnsubmittedClaimId("claim-123"))
        val sessionData      =
          RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, Some(true))(using
            sessionDataGASDS
          )
        val customConfig     = Map(
          "urls.communityBuildingsScheduleSpreadsheetGuidanceUrl" -> "https://test.example.com/charity-repayment-claim"
        )

        given application: Application = applicationBuilder(sessionData = sessionData)
          .configure(customConfig)
          .build()

        running(application) {
          val request =
            FakeRequest(GET, routes.AboutCommunityBuildingsScheduleController.onPageLoad.url)
          val result  = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include("https://test.example.com/charity-repayment-claim")
        }
      }

      // TODO when redirect available
      "should redirect to the next page if the communityBuildingsScheduleCompleted is true" in {
        val sessionDataDonations = RepaymentClaimDetailsAnswers
          .setClaimingDonationsCollectedInCommunityBuildings(true, Some(true))
          .and(SessionData.setUnsubmittedClaimId("claim-123"))
          .copy(communityBuildingsScheduleCompleted = true)

        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)(using sessionDataDonations)

        given application: Application = applicationBuilder(sessionData = sessionData).build()
        running(application) {
          val request =
            FakeRequest(GET, routes.AboutCommunityBuildingsScheduleController.onPageLoad.url)
          val result  = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.AboutCommunityBuildingsScheduleController.onPageLoad.url
          )
        }
      }
    }

    // TODO when redirect available
    "onSubmit" - {
      "should redirect to the next page" in {
        val sessionDataDonations       = RepaymentClaimDetailsAnswers
          .setClaimingDonationsCollectedInCommunityBuildings(true, Some(true))
          .and(SessionData.setUnsubmittedClaimId("claim-123"))
          .copy(communityBuildingsScheduleCompleted = true)
        val sessionData                = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)(using sessionDataDonations)
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.AboutCommunityBuildingsScheduleController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.UploadCommunityBuildingsScheduleController.onPageLoad.url)
        }
      }
    }
  }
}

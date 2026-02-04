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

package controllers.repaymentClaimDetails

import play.api.test.FakeRequest
import play.api.mvc.AnyContentAsEmpty
import controllers.ControllerSpec
import views.html.RepaymentClaimDetailsIncompleteAnswersView
import play.api.Application
import models.{RepaymentClaimDetailsAnswers, RepaymentClaimDetailsAnswersOld, SessionData}

class RepaymentClaimDetailsIncompleteAnswersControllerSpec extends ControllerSpec {

  val repaymentClaimDetailsDefaultAnswersOld = RepaymentClaimDetailsAnswersOld(
    claimingGiftAid = Some(true),
    claimingTaxDeducted = Some(false),
    claimingUnderGiftAidSmallDonationsScheme = Some(true),
    claimingReferenceNumber = Some(true),
    claimingDonationsNotFromCommunityBuilding = Some(true),
    claimingDonationsCollectedInCommunityBuildings = Some(true),
    connectedToAnyOtherCharities = Some(true),
    makingAdjustmentToPreviousClaim = Some(true),
    claimReferenceNumber = Some("12345678AB")
  )
  "RepaymentClaimDetailsIncompleteAnswersController" - {
    "onPageLoad" - {
      "should render the page with missing fields when answers are incomplete" in {
        val incompleteAnswers = RepaymentClaimDetailsAnswers()
        val sessionData       = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some("123"),
          lastUpdatedReference = Some("123"),
          repaymentClaimDetailsAnswersOld = repaymentClaimDetailsDefaultAnswersOld,
          repaymentClaimDetailsAnswers = Some(incompleteAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RepaymentClaimDetailsIncompleteAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view                  = application.injector.instanceOf[RepaymentClaimDetailsIncompleteAnswersView]
          val expectedMissingFields = incompleteAnswers.missingFields

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url,
            expectedMissingFields
          ).body
        }
      }

      "should render the page with no missing fields when answers are complete" in {
        val completeAnswers = RepaymentClaimDetailsAnswers(
          claimingGiftAid = Some(true),
          claimingTaxDeducted = Some(false),
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingReferenceNumber = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(false),
          connectedToAnyOtherCharities = Some(false),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData     = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some("123"),
          lastUpdatedReference = Some("123"),
          repaymentClaimDetailsAnswersOld = repaymentClaimDetailsDefaultAnswersOld,
          repaymentClaimDetailsAnswers = Some(completeAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RepaymentClaimDetailsIncompleteAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RepaymentClaimDetailsIncompleteAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url,
            Seq.empty
          ).body
        }
      }

      "should render the page with no missing fields when answers are complete for claimingUnderGiftAidSmallDonationsScheme is true " in {
        val completeAnswers = RepaymentClaimDetailsAnswers(
          claimingGiftAid = Some(true),
          claimingTaxDeducted = Some(true),
          claimingUnderGiftAidSmallDonationsScheme = Some(true),
          claimingReferenceNumber = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(true),
          claimingDonationsCollectedInCommunityBuildings = Some(true),
          connectedToAnyOtherCharities = Some(true),
          makingAdjustmentToPreviousClaim = Some(true)
        )
        val sessionData     = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some("123"),
          lastUpdatedReference = Some("123"),
          repaymentClaimDetailsAnswersOld = repaymentClaimDetailsDefaultAnswersOld,
          repaymentClaimDetailsAnswers = Some(completeAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RepaymentClaimDetailsIncompleteAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RepaymentClaimDetailsIncompleteAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url,
            Seq.empty
          ).body
        }
      }
    }
  }
}

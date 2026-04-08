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

package controllers.claimDeclaration

import controllers.ControllerSpec
import models.*
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest
import play.api.Application
import views.html.ClaimCompleteView

class ClaimCompleteControllerSpec extends ControllerSpec {

  val testClaimId = testCharitiesReference

  val nextPage: Call = controllers.claimDeclaration.routes.RepaymentClaimSummaryController.onPageLoad

  val repaymentClaimDetailsAnswersCompleted: RepaymentClaimDetailsAnswers =
    RepaymentClaimDetailsAnswers(
      claimingGiftAid = Some(false),
      claimingTaxDeducted = Some(false),
      claimingUnderGiftAidSmallDonationsScheme = Some(false),
      claimingReferenceNumber = Some(false)
    )

  val organisationDetailsAnswers = OrganisationDetailsAnswers(
    nameOfCharityRegulator = Some(NameOfCharityRegulator.Scottish),
    reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.Waiting),
    charityRegistrationNumber = Some("123"),
    areYouACorporateTrustee = Some(false),
    doYouHaveAuthorisedOfficialTrusteeUKAddress = Some(true),
    authorisedOfficialTrusteePostcode = Some("none"),
    authorisedOfficialTrusteeDaytimeTelephoneNumber = Some("12345678AB"),
    authorisedOfficialTrusteeTitle = Some("MR"),
    authorisedOfficialTrusteeFirstName = Some("Jack"),
    authorisedOfficialTrusteeLastName = Some("Smith"),
    authorisedOfficialDetails = Some(AuthorisedOfficialDetails(Some("MR"), "Jack", "Smith", "12345678AB", Some("none")))
  )

  "ClaimCompleteController" - {
    "on pageLoad" - {
      "should render the page correctly when isClaimDetailsComplete condition is met" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(true),
          connectedToAnyOtherCharities = Some(false),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          lastUpdatedReference = Some(testCharitiesReference),
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(
          communityBuildingsScheduleCompleted = true,
          understandFalseStatements = Some(true),
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimCompleteController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimCompleteView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(nextPage, testClaimId).body
        }
      }

      "should render the page correctly when isClaimDetailsComplete condition is met when prev overpayment for giftAid  & otherIncome> 0.0 & prompt is entered  " in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(true),
          connectedToAnyOtherCharities = Some(false),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          lastUpdatedReference = Some(testCharitiesReference),
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(
          communityBuildingsScheduleCompleted = true,
          understandFalseStatements = Some(true),
          prevOverclaimedGiftAid = Some(BigDecimal(1.0)),
          adjustmentForOtherIncomePreviousOverClaimed = Some(BigDecimal(2.0)),
          includedAnyAdjustmentsInClaimPrompt = Some("test"),
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimCompleteController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimCompleteView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(nextPage, testClaimId).body
        }
      }

      "should render the page correctly when isClaimDetailsComplete condition is met when prev overpayment for otherIncome > 0 & prompt is entered  " in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(true),
          connectedToAnyOtherCharities = Some(false),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          lastUpdatedReference = Some(testCharitiesReference),
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(
          communityBuildingsScheduleCompleted = true,
          understandFalseStatements = Some(true),
          adjustmentForOtherIncomePreviousOverClaimed = Some(BigDecimal(1.0)),
          includedAnyAdjustmentsInClaimPrompt = Some("test"),
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimCompleteController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimCompleteView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(nextPage, testClaimId).body
        }
      }
      "should render the page correctly when isClaimDetailsComplete condition is met & understandFalseStatements is false  " in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(true),
          connectedToAnyOtherCharities = Some(false),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          lastUpdatedReference = Some(testCharitiesReference),
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(
          communityBuildingsScheduleCompleted = true,
          understandFalseStatements = Some(false),
          adjustmentForOtherIncomePreviousOverClaimed = Some(BigDecimal(1.0)),
          includedAnyAdjustmentsInClaimPrompt = Some("test"),
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimCompleteController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }
//      "should render the page correctly when isClaimDetailsComplete condition is met & unsubmitted is None  " in {
//        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
//          claimingUnderGiftAidSmallDonationsScheme = Some(false),
//          claimingDonationsNotFromCommunityBuilding = Some(false),
//          claimingDonationsCollectedInCommunityBuildings = Some(true),
//          connectedToAnyOtherCharities = Some(false),
//          makingAdjustmentToPreviousClaim = Some(false)
//        )
//        val sessionData = SessionData(
//          charitiesReference = testCharitiesReference,
//          repaymentClaimDetailsAnswers = Some(answers)
//        ).copy(
//          communityBuildingsScheduleCompleted = true,
//          lastUpdatedReference = None,
//          unsubmittedClaimId = None,
//          understandFalseStatements = Some(false),
//          adjustmentForOtherIncomePreviousOverClaimed = Some(BigDecimal(1.0)),
//          includedAnyAdjustmentsInClaimPrompt = Some("test"),
//          organisationDetailsAnswers = Some(organisationDetailsAnswers)
//        )
//
//        given application: Application = applicationBuilder(sessionData = sessionData).build()
//
//        running(application) {
//          given request: FakeRequest[AnyContentAsEmpty.type] =
//            FakeRequest(GET, routes.ClaimCompleteController.onPageLoad.url)
//
//          val result = route(application, request).value
//
//          status(result) shouldEqual SEE_OTHER
//          redirectLocation(result) shouldEqual Some(
//            controllers.routes.ClaimsTaskListController.onPageLoad.url
//          )
//        }
//      }
    }
  }
}

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
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.Application
import play.api.inject.guice.GuiceableModule
import views.html.ClaimDeclarationView
import services.{ClaimsService, SaveService}
import connectors.ClaimsConnector
import repositories.SessionCache
import play.api.inject.bind
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.Future

class ClaimDeclarationControllerSpec extends ControllerSpec {

  val mockClaimsConnector: ClaimsConnector = mock[ClaimsConnector]
  val mockClaimsService: ClaimsService     = mock[ClaimsService]
  val mockSaveService: SaveService         = mock[SaveService]
  val mockSessionCache                     = mock[SessionCache]

  override protected val additionalBindings: List[GuiceableModule] = List(
    bind[ClaimsConnector].toInstance(mockClaimsConnector),
    bind[ClaimsService].toInstance(mockClaimsService),
    bind[SaveService].toInstance(mockSaveService),
    bind[SessionCache].toInstance(mockSessionCache)
  )

  val testClaimId = "claim-123"

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

  val organisationDetailsAnswers2 = OrganisationDetailsAnswers(
    nameOfCharityRegulator = Some(NameOfCharityRegulator.Scottish),
    reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.LowIncome),
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

  "ClaimDeclarationController" - {
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
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(
          communityBuildingsScheduleCompleted = true,
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimDeclarationController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimDeclarationView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view().body
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
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(
          communityBuildingsScheduleCompleted = true,
          prevOverclaimedGiftAid = Some(BigDecimal(1.0)),
          adjustmentForOtherIncomePreviousOverClaimed = Some(BigDecimal(2.0)),
          includedAnyAdjustmentsInClaimPrompt = Some("test"),
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimDeclarationController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimDeclarationView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view().body
        }
      }

      "should render the page correctly when isClaimDetailsComplete condition is met when prev overpayment for giftAid > 0 & prompt is entered  " in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(true),
          connectedToAnyOtherCharities = Some(false),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(
          communityBuildingsScheduleCompleted = true,
          prevOverclaimedGiftAid = Some(BigDecimal(1.0)),
          includedAnyAdjustmentsInClaimPrompt = Some("test"),
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimDeclarationController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimDeclarationView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view().body
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
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(
          communityBuildingsScheduleCompleted = true,
          adjustmentForOtherIncomePreviousOverClaimed = Some(BigDecimal(1.0)),
          includedAnyAdjustmentsInClaimPrompt = Some("test"),
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimDeclarationController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimDeclarationView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view().body
        }
      }

      "should render the page correctly when isClaimDetailsComplete condition is not met when prev overpayment for otherIncome > 0 & prompt is empty  " in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(true),
          connectedToAnyOtherCharities = Some(false),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(
          communityBuildingsScheduleCompleted = true,
          adjustmentForOtherIncomePreviousOverClaimed = Some(BigDecimal(1.0)),
          includedAnyAdjustmentsInClaimPrompt = None,
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimDeclarationController.onPageLoad.url)

          val result = route(application, request).value
          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should render the page correctly when isClaimDetailsComplete condition is not met when prev overpayment for giftAid > 0 & prompt is empty  " in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(true),
          connectedToAnyOtherCharities = Some(false),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(
          communityBuildingsScheduleCompleted = true,
          prevOverclaimedGiftAid = Some(BigDecimal(1.0)),
          includedAnyAdjustmentsInClaimPrompt = None,
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimDeclarationController.onPageLoad.url)

          val result = route(application, request).value
          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should render ClaimsTaskListController if isClaimDetailsComplete is false" in {
        val sessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            includedAnyAdjustmentsInClaimPrompt = Some("some text ....")
          )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimDeclarationController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

    }
    "onSubmit" - {

      "should redirect to next page when dataguard condition is met" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(true),
          connectedToAnyOtherCharities = Some(true),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          lastUpdatedReference = Some(testClaimId),
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(
          understandFalseStatements = Some(true),
          connectedCharitiesScheduleCompleted = true,
          prevOverclaimedGiftAid = Some(BigDecimal(1.0)),
          includedAnyAdjustmentsInClaimPrompt = Some("test"),
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        (mockSaveService
          .save(_: SessionData)(using _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        (mockClaimsService
          .save(using _: HeaderCarrier))
          .expects(*)
          .returning(Future.successful(()))

        (mockSessionCache
          .get()(using _: HeaderCarrier))
          .expects(*)
          .returning(Future.successful(Some(sessionData)))

        (mockSaveService
          .save(_: SessionData)(using _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        (mockClaimsConnector
          .submitClaim(_: String, _: String, _: String)(using _: HeaderCarrier))
          .expects(testClaimId, testClaimId, "en", *)
          .returning(Future.successful(SubmitClaimResponse(true, "test sub ref")))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.ClaimDeclarationController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.ClaimCompleteController.onPageLoad.url)
        }
      }

      "should redirect to Claims List when dataguard condition is not met due upload not completed" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(true),
          connectedToAnyOtherCharities = Some(true),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(
          connectedCharitiesScheduleCompleted = false,
          prevOverclaimedGiftAid = Some(BigDecimal(1.0)),
          includedAnyAdjustmentsInClaimPrompt = Some("test"),
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.ClaimDeclarationController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should redirect to Claims List when dataguard condition is not met due empty adjustment details prompt when there is overpayment" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(true),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(true),
          connectedToAnyOtherCharities = Some(true),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(
          connectedCharitiesScheduleCompleted = true,
          giftAidScheduleCompleted = true,
          prevOverclaimedGiftAid = Some(BigDecimal(1.0)),
          includedAnyAdjustmentsInClaimPrompt = None,
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.ClaimDeclarationController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should redirect to ClaimsTaskListController when dataguard condition is not met" in {
        val sessionData                = SessionData
          .empty(testCharitiesReference)
          .copy(
            includedAnyAdjustmentsInClaimPrompt = Some("some text ....")
          )
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.ClaimDeclarationController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }
    }
  }

}

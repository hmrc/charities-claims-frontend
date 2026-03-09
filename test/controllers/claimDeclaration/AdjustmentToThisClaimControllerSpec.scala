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

import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import controllers.ControllerSpec
import views.html.AdjustmentToThisClaimView
import play.api.{inject, Application}
import forms.AdjustmentToThisClaimFormProvider
import models.*
import play.api.data.Form
import play.api.test.FakeRequest
import connectors.UnregulatedDonationsConnector
import uk.gov.hmrc.http.HeaderCarrier

import _root_.scala.concurrent.Future

class AdjustmentToThisClaimControllerSpec extends ControllerSpec {

  given HeaderCarrier                              = HeaderCarrier()
  val mockConnector: UnregulatedDonationsConnector = mock[UnregulatedDonationsConnector]

  val form: Form[Option[String]] = new AdjustmentToThisClaimFormProvider()(
    "adjustmentToThisClaim.error.required",
    (20, "adjustmentToThisClaim.error.length"),
    "adjustmentToThisClaim.error.regex",
    true
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

  val declarationDetailsAnswers = DeclarationDetailsAnswers(
    includedAnyAdjustmentsInClaimPrompt = Some("123456ABC")
  )

  "ClaimReferenceNumberInputController" - {

    "onPageLoad" - {
      "should render the page correctly when isClaimDetailsComplete condition is met due communityBuildingsScheduleCompleted " in {
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
            FakeRequest(GET, routes.AdjustmentToThisClaimController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[AdjustmentToThisClaimView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form).body
        }
      }

      "should render the page correctly when isClaimDetailsComplete condition is met due giftAidScheduleCompleted " in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(true),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(false),
          connectedToAnyOtherCharities = Some(false),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(
          giftAidScheduleCompleted = true,
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AdjustmentToThisClaimController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[AdjustmentToThisClaimView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form).body
        }
      }

      "should render the page correctly when isClaimDetailsComplete condition is met due otherIncomeScheduleCompleted " in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(true),
          claimingDonationsCollectedInCommunityBuildings = Some(false),
          connectedToAnyOtherCharities = Some(false),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(
          otherIncomeScheduleCompleted = true,
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AdjustmentToThisClaimController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[AdjustmentToThisClaimView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form).body
        }
      }

      "should render the page correctly when isClaimDetailsComplete condition is met due connectedCharitiesScheduleCompleted " in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(false),
          connectedToAnyOtherCharities = Some(true),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(
          connectedCharitiesScheduleCompleted = true,
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AdjustmentToThisClaimController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[AdjustmentToThisClaimView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form).body
        }
      }

      "should render the page correctly when isClaimDetailsComplete condition is met due connectedCharitiesScheduleCompleted and unregulatedLimitExceeded is true" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(false),
          connectedToAnyOtherCharities = Some(true),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          unregulatedLimitExceeded = true,
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(
          connectedCharitiesScheduleCompleted = true,
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AdjustmentToThisClaimController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[AdjustmentToThisClaimView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form).body
        }
      }

      "should render ClaimsTaskListController if isClaimDetailsComplete is false" in {
        val sessionData = DeclarationDetailsAnswers.setIncludedAnyAdjustmentsInClaimPrompt(Some("some text ...."))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AdjustmentToThisClaimController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should render the page and pre-populate correctly" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(false),
          connectedToAnyOtherCharities = Some(true),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers),
          declarationDetailsAnswers = Some(declarationDetailsAnswers)
        ).copy(
          connectedCharitiesScheduleCompleted = true,
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AdjustmentToThisClaimController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[AdjustmentToThisClaimView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(Some("123456ABC"))).body
        }
      }

      "should render the page and pre-populate correctly when Some(UnregulatedLimitExceeded) when total donations less than the Low Income limit" in {
        (mockConnector
          .getTotalUnregulatedDonations(_: String)(using _: HeaderCarrier))
          .expects(testCharitiesReference, *)
          .returning(Future.successful(Some(BigDecimal(3000))))
          .once()

        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(false),
          connectedToAnyOtherCharities = Some(true),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers),
          declarationDetailsAnswers = Some(declarationDetailsAnswers)
        ).copy(
          connectedCharitiesScheduleCompleted = true,
          organisationDetailsAnswers = Some(organisationDetailsAnswers2)
        )

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[UnregulatedDonationsConnector].toInstance(mockConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AdjustmentToThisClaimController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[AdjustmentToThisClaimView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(Some("123456ABC"))).body
        }
      }

      "should render the page and pre-populate correctly when Some(UnregulatedLimitExceeded) when total donations exceed the Low Income limit" in {
        (mockConnector
          .getTotalUnregulatedDonations(_: String)(using _: HeaderCarrier))
          .expects(testCharitiesReference, *)
          .returning(Future.successful(Some(BigDecimal(5001))))
          .once()

        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(false),
          connectedToAnyOtherCharities = Some(true),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers),
          declarationDetailsAnswers = Some(declarationDetailsAnswers)
        ).copy(
          connectedCharitiesScheduleCompleted = true,
          organisationDetailsAnswers = Some(organisationDetailsAnswers2)
        )

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[UnregulatedDonationsConnector].toInstance(mockConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AdjustmentToThisClaimController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.RegisterCharityWithARegulatorController.onPageLoad.url
          )

        }
      }

      "should render the ClaimsTaskListController and incorrectly pre-populate data" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          repaymentClaimDetailsAnswers = Some(
            RepaymentClaimDetailsAnswers(
              claimingReferenceNumber = Some(false),
              claimReferenceNumber = Some("123456")
            )
          )
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AdjustmentToThisClaimController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }
    }

    "onSubmit" - {
      "should redirect to ClaimsTaskListController page when dataguard condition is met" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(false),
          connectedToAnyOtherCharities = Some(true),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers),
          declarationDetailsAnswers = Some(declarationDetailsAnswers)
        ).copy(
          connectedCharitiesScheduleCompleted = true,
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.AdjustmentToThisClaimController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "123456ABC")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should redirect to ClaimsTaskListController when dataguard condition is not met" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.AdjustmentToThisClaimController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "123456")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should reload page with errors when required field is missing due to adjustmentForOtherIncomePreviousOverClaimed" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(false),
          connectedToAnyOtherCharities = Some(true),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers),
          adjustmentForOtherIncomePreviousOverClaimed = Some(BigDecimal(1001.1)),
          declarationDetailsAnswers = Some(declarationDetailsAnswers)
        ).copy(
          connectedCharitiesScheduleCompleted = true,
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.AdjustmentToThisClaimController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      "should reload page with errors when required field is missing due to  prevOverclaimedGiftAid " in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(false),
          connectedToAnyOtherCharities = Some(true),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers),
          prevOverclaimedGiftAid = Some(BigDecimal(201.1)),
          declarationDetailsAnswers = Some(declarationDetailsAnswers)
        ).copy(
          connectedCharitiesScheduleCompleted = true,
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.AdjustmentToThisClaimController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      "should reload page with errors when required field is missing due to adjustmentForOtherIncomePreviousOverClaimed & prevOverclaimedGiftAid " in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(false),
          connectedToAnyOtherCharities = Some(true),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers),
          adjustmentForOtherIncomePreviousOverClaimed = Some(BigDecimal(101.1)),
          prevOverclaimedGiftAid = Some(BigDecimal(201.1)),
          declarationDetailsAnswers = Some(declarationDetailsAnswers)
        ).copy(
          connectedCharitiesScheduleCompleted = true,
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.AdjustmentToThisClaimController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      "should reload page with without errors when required field is missing due no previous overpayment" in {

        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(false),
          connectedToAnyOtherCharities = Some(true),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers),
          declarationDetailsAnswers = Some(declarationDetailsAnswers)
        ).copy(
          connectedCharitiesScheduleCompleted = true,
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.AdjustmentToThisClaimController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "")

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

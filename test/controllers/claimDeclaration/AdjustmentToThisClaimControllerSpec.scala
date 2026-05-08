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

import connectors.UnregulatedDonationsConnector
import controllers.ControllerSpec
import forms.AdjustmentToThisClaimFormProvider
import models.*
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.{inject, Application}
import services.SaveService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.AdjustmentToThisClaimView

import scala.concurrent.Future
import uk.gov.hmrc.auth.core.AffinityGroup

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

  "ClaimReferenceNumberInputController" - {

    "onPageLoad" - {
      "organisation user" - {
        val isAgent = false
        "should render the page correctly when isClaimDetailsComplete condition is met due communityBuildingsScheduleCompleted " in {
          val answers     = repaymentClaimDetailsAnswersCompleted.copy(
            claimingUnderGiftAidSmallDonationsScheme = Some(false),
            claimingDonationsNotFromCommunityBuilding = Some(false),
            claimingDonationsCollectedInCommunityBuildings = Some(true),
            claimingGiftAid = Some(false),
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
            contentAsString(result) shouldEqual view(form, isAgent).body
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
            contentAsString(result) shouldEqual view(form, isAgent).body
          }
        }

        "should render the page correctly when isClaimDetailsComplete condition is met due otherIncomeScheduleCompleted " in {
          val answers     = repaymentClaimDetailsAnswersCompleted.copy(
            claimingUnderGiftAidSmallDonationsScheme = Some(false),
            claimingDonationsNotFromCommunityBuilding = Some(true),
            claimingDonationsCollectedInCommunityBuildings = Some(false),
            claimingGiftAid = Some(false),
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
            contentAsString(result) shouldEqual view(form, isAgent).body
          }
        }

        "should render the page correctly when isClaimDetailsComplete condition is met due connectedCharitiesScheduleCompleted " in {
          val answers     = repaymentClaimDetailsAnswersCompleted.copy(
            claimingUnderGiftAidSmallDonationsScheme = Some(false),
            claimingDonationsNotFromCommunityBuilding = Some(false),
            claimingDonationsCollectedInCommunityBuildings = Some(false),
            claimingGiftAid = Some(false),
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
            contentAsString(result) shouldEqual view(form, isAgent).body
          }
        }

        "should render the page correctly when isClaimDetailsComplete condition is met due unregulatedLimitExceeded is true" in {
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
            contentAsString(result) shouldEqual view(form, isAgent).body
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
            claimingGiftAid = Some(false),
            makingAdjustmentToPreviousClaim = Some(false)
          )
          val sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            unsubmittedClaimId = Some(testClaimId),
            repaymentClaimDetailsAnswers = Some(answers),
            includedAnyAdjustmentsInClaimPrompt = Some("123456ABC")
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
            contentAsString(result) shouldEqual view(form.fill(Some("123456ABC")), isAgent).body
          }
        }

        "should render the page when unregulatedWarningBypassed is true (user just bypassed WRN5)" in {
          val mockSaveService = mock[SaveService]
          (mockSaveService
            .save(_: SessionData)(using _: HeaderCarrier))
            .expects(where { (sessionData: SessionData, _: HeaderCarrier) =>
              !sessionData.unregulatedWarningBypassed
            })
            .returning(Future.successful(()))

          val answers     = repaymentClaimDetailsAnswersCompleted.copy(
            claimingUnderGiftAidSmallDonationsScheme = Some(false),
            claimingDonationsNotFromCommunityBuilding = Some(false),
            claimingDonationsCollectedInCommunityBuildings = Some(false),
            connectedToAnyOtherCharities = Some(true),
            claimingGiftAid = Some(true),
            makingAdjustmentToPreviousClaim = Some(false)
          )
          val sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            unsubmittedClaimId = Some(testClaimId),
            unregulatedLimitExceeded = true,
            unregulatedWarningBypassed = true,
            repaymentClaimDetailsAnswers = Some(answers)
          ).copy(
            connectedCharitiesScheduleCompleted = true,
            giftAidScheduleCompleted = true,
            organisationDetailsAnswers = Some(organisationDetailsAnswers2)
          )

          given application: Application = applicationBuilder(sessionData = sessionData)
            .overrides(inject.bind[SaveService].toInstance(mockSaveService))
            .build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.AdjustmentToThisClaimController.onPageLoad.url)

            val result = route(application, request).value
            val view   = application.injector.instanceOf[AdjustmentToThisClaimView]

            status(result) shouldEqual OK
            contentAsString(result) shouldEqual view(form, isAgent).body
          }
        }

        "should redirect to the warning page when unregulatedWarningBypassed is false and limit is exceeded (re-visit after bypass)" in {
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
            claimingGiftAid = Some(true),
            makingAdjustmentToPreviousClaim = Some(false)
          )
          val sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            unsubmittedClaimId = Some(testClaimId),
            unregulatedLimitExceeded = true,
            unregulatedWarningBypassed = false,
            repaymentClaimDetailsAnswers = Some(answers)
          ).copy(
            connectedCharitiesScheduleCompleted = true,
            giftAidScheduleCompleted = true,
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
            claimingGiftAid = Some(true),
            makingAdjustmentToPreviousClaim = Some(false)
          )
          val sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            unsubmittedClaimId = Some(testClaimId),
            repaymentClaimDetailsAnswers = Some(answers),
            includedAnyAdjustmentsInClaimPrompt = Some("123456ABC")
          ).copy(
            connectedCharitiesScheduleCompleted = true,
            giftAidScheduleCompleted = true,
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
            contentAsString(result) shouldEqual view(form.fill(Some("123456ABC")), isAgent).body
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
            claimingGiftAid = Some(true),
            makingAdjustmentToPreviousClaim = Some(false)
          )
          val sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            unsubmittedClaimId = Some(testClaimId),
            repaymentClaimDetailsAnswers = Some(answers),
            includedAnyAdjustmentsInClaimPrompt = Some("123456ABC")
          ).copy(
            connectedCharitiesScheduleCompleted = true,
            giftAidScheduleCompleted = true,
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
      "Agent user" - {
        val isAgent = true
        "should render the page correctly when isClaimDetailsComplete condition is met due communityBuildingsScheduleCompleted " in {
          val answers     = repaymentClaimDetailsAnswersCompleted.copy(
            claimingUnderGiftAidSmallDonationsScheme = Some(false),
            claimingDonationsNotFromCommunityBuilding = Some(false),
            claimingDonationsCollectedInCommunityBuildings = Some(true),
            claimingGiftAid = Some(false),
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

          given application: Application =
            applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.AdjustmentToThisClaimController.onPageLoad.url)

            val result = route(application, request).value
            val view   = application.injector.instanceOf[AdjustmentToThisClaimView]

            status(result) shouldEqual OK
            contentAsString(result) shouldEqual view(form, isAgent).body
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

          given application: Application =
            applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.AdjustmentToThisClaimController.onPageLoad.url)

            val result = route(application, request).value
            val view   = application.injector.instanceOf[AdjustmentToThisClaimView]

            status(result) shouldEqual OK
            contentAsString(result) shouldEqual view(form, isAgent).body
          }
        }

        "should render the page correctly when isClaimDetailsComplete condition is met due otherIncomeScheduleCompleted " in {
          val answers     = repaymentClaimDetailsAnswersCompleted.copy(
            claimingUnderGiftAidSmallDonationsScheme = Some(false),
            claimingDonationsNotFromCommunityBuilding = Some(true),
            claimingDonationsCollectedInCommunityBuildings = Some(false),
            claimingGiftAid = Some(false),
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

          given application: Application =
            applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.AdjustmentToThisClaimController.onPageLoad.url)

            val result = route(application, request).value
            val view   = application.injector.instanceOf[AdjustmentToThisClaimView]

            status(result) shouldEqual OK
            contentAsString(result) shouldEqual view(form, isAgent).body
          }
        }

        "should render the page correctly when isClaimDetailsComplete condition is met due connectedCharitiesScheduleCompleted " in {
          val answers     = repaymentClaimDetailsAnswersCompleted.copy(
            claimingUnderGiftAidSmallDonationsScheme = Some(false),
            claimingDonationsNotFromCommunityBuilding = Some(false),
            claimingDonationsCollectedInCommunityBuildings = Some(false),
            claimingGiftAid = Some(false),
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

          given application: Application =
            applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.AdjustmentToThisClaimController.onPageLoad.url)

            val result = route(application, request).value
            val view   = application.injector.instanceOf[AdjustmentToThisClaimView]

            status(result) shouldEqual OK
            contentAsString(result) shouldEqual view(form, isAgent).body
          }
        }

        "should render the page correctly when isClaimDetailsComplete condition is met due unregulatedLimitExceeded is true" in {
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

          given application: Application =
            applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.AdjustmentToThisClaimController.onPageLoad.url)

            val result = route(application, request).value
            val view   = application.injector.instanceOf[AdjustmentToThisClaimView]

            status(result) shouldEqual OK
            contentAsString(result) shouldEqual view(form, isAgent).body
          }
        }

        "should render ClaimsTaskListController if isClaimDetailsComplete is false" in {
          val sessionData = SessionData
            .empty(testCharitiesReference)
            .copy(
              includedAnyAdjustmentsInClaimPrompt = Some("some text ....")
            )

          given application: Application =
            applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

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
            claimingGiftAid = Some(false),
            makingAdjustmentToPreviousClaim = Some(false)
          )
          val sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            unsubmittedClaimId = Some(testClaimId),
            repaymentClaimDetailsAnswers = Some(answers),
            includedAnyAdjustmentsInClaimPrompt = Some("123456ABC")
          ).copy(
            connectedCharitiesScheduleCompleted = true,
            organisationDetailsAnswers = Some(organisationDetailsAnswers)
          )

          given application: Application =
            applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.AdjustmentToThisClaimController.onPageLoad.url)

            val result = route(application, request).value
            val view   = application.injector.instanceOf[AdjustmentToThisClaimView]

            status(result) shouldEqual OK
            contentAsString(result) shouldEqual view(form.fill(Some("123456ABC")), isAgent).body
          }
        }

        "should render the page when unregulatedWarningBypassed is true (user just bypassed WRN5)" in {
          val mockSaveService = mock[SaveService]
          (mockSaveService
            .save(_: SessionData)(using _: HeaderCarrier))
            .expects(where { (sessionData: SessionData, _: HeaderCarrier) =>
              !sessionData.unregulatedWarningBypassed
            })
            .returning(Future.successful(()))

          val answers     = repaymentClaimDetailsAnswersCompleted.copy(
            claimingUnderGiftAidSmallDonationsScheme = Some(false),
            claimingDonationsNotFromCommunityBuilding = Some(false),
            claimingDonationsCollectedInCommunityBuildings = Some(false),
            connectedToAnyOtherCharities = Some(true),
            claimingGiftAid = Some(true),
            makingAdjustmentToPreviousClaim = Some(false)
          )
          val sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            unsubmittedClaimId = Some(testClaimId),
            unregulatedLimitExceeded = true,
            unregulatedWarningBypassed = true,
            repaymentClaimDetailsAnswers = Some(answers)
          ).copy(
            connectedCharitiesScheduleCompleted = true,
            giftAidScheduleCompleted = true,
            organisationDetailsAnswers = Some(organisationDetailsAnswers2)
          )

          given application: Application =
            applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent)
              .overrides(inject.bind[SaveService].toInstance(mockSaveService))
              .build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.AdjustmentToThisClaimController.onPageLoad.url)

            val result = route(application, request).value
            val view   = application.injector.instanceOf[AdjustmentToThisClaimView]

            status(result) shouldEqual OK
            contentAsString(result) shouldEqual view(form, isAgent).body
          }
        }

        "should redirect to the warning page when unregulatedWarningBypassed is false and limit is exceeded (re-visit after bypass)" in {
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
            claimingGiftAid = Some(true),
            makingAdjustmentToPreviousClaim = Some(false)
          )
          val sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            unsubmittedClaimId = Some(testClaimId),
            unregulatedLimitExceeded = true,
            unregulatedWarningBypassed = false,
            repaymentClaimDetailsAnswers = Some(answers)
          ).copy(
            connectedCharitiesScheduleCompleted = true,
            giftAidScheduleCompleted = true,
            organisationDetailsAnswers = Some(organisationDetailsAnswers2)
          )

          given application: Application =
            applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent)
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
            claimingGiftAid = Some(true),
            makingAdjustmentToPreviousClaim = Some(false)
          )
          val sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            unsubmittedClaimId = Some(testClaimId),
            repaymentClaimDetailsAnswers = Some(answers),
            includedAnyAdjustmentsInClaimPrompt = Some("123456ABC")
          ).copy(
            connectedCharitiesScheduleCompleted = true,
            giftAidScheduleCompleted = true,
            organisationDetailsAnswers = Some(organisationDetailsAnswers2)
          )

          given application: Application =
            applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent)
              .overrides(inject.bind[UnregulatedDonationsConnector].toInstance(mockConnector))
              .build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.AdjustmentToThisClaimController.onPageLoad.url)

            val result = route(application, request).value
            val view   = application.injector.instanceOf[AdjustmentToThisClaimView]

            status(result) shouldEqual OK
            contentAsString(result) shouldEqual view(form.fill(Some("123456ABC")), isAgent).body
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
            claimingGiftAid = Some(true),
            makingAdjustmentToPreviousClaim = Some(false)
          )
          val sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            unsubmittedClaimId = Some(testClaimId),
            repaymentClaimDetailsAnswers = Some(answers),
            includedAnyAdjustmentsInClaimPrompt = Some("123456ABC")
          ).copy(
            connectedCharitiesScheduleCompleted = true,
            giftAidScheduleCompleted = true,
            organisationDetailsAnswers = Some(organisationDetailsAnswers2)
          )

          given application: Application =
            applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent)
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

          given application: Application =
            applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.AdjustmentToThisClaimController.onPageLoad.url)

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
          }
        }
      }
    }

    "onSubmit" - {
      "should redirect to next page when dataguard condition is met" in {
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
          includedAnyAdjustmentsInClaimPrompt = Some("123456ABC")
        ).copy(
          connectedCharitiesScheduleCompleted = true,
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveClaim.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.AdjustmentToThisClaimController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "123456ABC")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimDeclarationController.onPageLoad.url
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
          includedAnyAdjustmentsInClaimPrompt = Some("123456ABC")
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
          includedAnyAdjustmentsInClaimPrompt = Some("123456ABC")
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
          includedAnyAdjustmentsInClaimPrompt = Some("123456ABC")
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
          lastUpdatedReference = Some(testClaimId),
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers),
          includedAnyAdjustmentsInClaimPrompt = Some("123456ABC")
        ).copy(
          connectedCharitiesScheduleCompleted = true,
          organisationDetailsAnswers = Some(organisationDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveClaim.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.AdjustmentToThisClaimController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimDeclarationController.onPageLoad.url
          )
        }
      }
    }
  }
}

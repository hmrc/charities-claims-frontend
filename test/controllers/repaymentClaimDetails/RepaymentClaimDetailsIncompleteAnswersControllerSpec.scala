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

package controllers.repaymentClaimDetails

import play.api.test.FakeRequest
import play.api.mvc.AnyContentAsEmpty
import controllers.ControllerSpec
import views.html.RepaymentClaimDetailsIncompleteAnswersView
import play.api.Application
import models.{RepaymentClaimDetailsAnswers, SessionData}
import uk.gov.hmrc.auth.core.AffinityGroup

class RepaymentClaimDetailsIncompleteAnswersControllerSpec extends ControllerSpec {

  "RepaymentClaimDetailsIncompleteAnswersController" - {
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
            FakeRequest(GET, routes.RepaymentClaimDetailsIncompleteAnswersController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
      "should render the page with default missing fields when no session data present" in {

        given application: Application = applicationBuilder().build()

        val defaultMissingFields = RepaymentClaimDetailsAnswers.getMissingFields(None)

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RepaymentClaimDetailsIncompleteAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RepaymentClaimDetailsIncompleteAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url,
            defaultMissingFields,
            isAgent = false
          ).body
        }
      }

      "should render the page with missing fields when answers are incomplete" in {
        val incompleteAnswers = RepaymentClaimDetailsAnswers()
        val sessionData       = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some("123"),
          lastUpdatedReference = Some("123"),
          repaymentClaimDetailsAnswers =
            Some(incompleteAnswers) // TODO: MIGRATION - change to: repaymentClaimDetailsAnswers = incompleteAnswers
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
            expectedMissingFields,
            isAgent = false
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
          repaymentClaimDetailsAnswers =
            Some(completeAnswers) // TODO: MIGRATION - change to: repaymentClaimDetailsAnswers = completeAnswers
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
            Seq.empty,
            isAgent = false
          ).body
        }
      }

      "should render the page with no missing fields when answers are complete and claimingUnderGiftAidSmallDonationsScheme is true " in {
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
          repaymentClaimDetailsAnswers =
            Some(completeAnswers) // TODO: MIGRATION - change to: repaymentClaimDetailsAnswers = completeAnswers
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
            Seq.empty,
            isAgent = false
          ).body
        }
      }

      "should render the page with expected fields missing when claimingUnderGiftAidSmallDonationsScheme is true" in {
        val incompleteAnswers = RepaymentClaimDetailsAnswers(
          claimingGiftAid = Some(true),
          claimingTaxDeducted = Some(false),
          claimingUnderGiftAidSmallDonationsScheme = Some(true),
          claimingReferenceNumber = Some(false)
        )
        val sessionData       = SessionData(
          unsubmittedClaimId = Some("123"),
          charitiesReference = testCharitiesReference,
          lastUpdatedReference = Some("123"),
          repaymentClaimDetailsAnswers =
            Some(incompleteAnswers) // TODO: MIGRATION - change to: repaymentClaimDetailsAnswers = incompleteAnswers
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        val expectedMissingFields = incompleteAnswers.missingFields

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RepaymentClaimDetailsIncompleteAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RepaymentClaimDetailsIncompleteAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url,
            expectedMissingFields,
            isAgent = false
          ).body

          expectedMissingFields should contain("claimGASDS.missingDetails")
          expectedMissingFields should contain("claimingCommunityBuildingDonations.missingDetails")
          expectedMissingFields should contain("connectedToAnyOtherCharities.missingDetails")
        }
      }

      "should render the page with only claimingReferenceNumber check missing" in {
        val incompleteAnswers = RepaymentClaimDetailsAnswers(
          claimingGiftAid = Some(true),
          claimingTaxDeducted = Some(false),
          claimingUnderGiftAidSmallDonationsScheme = Some(false)
        )
        val sessionData       = SessionData(
          unsubmittedClaimId = Some("123"),
          charitiesReference = testCharitiesReference,
          lastUpdatedReference = Some("123"),
          repaymentClaimDetailsAnswers =
            Some(incompleteAnswers) // TODO: MIGRATION - change to: repaymentClaimDetailsAnswers = incompleteAnswers
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        val expectedMissingFields = incompleteAnswers.missingFields

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RepaymentClaimDetailsIncompleteAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RepaymentClaimDetailsIncompleteAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url,
            expectedMissingFields,
            isAgent = false
          ).body

          expectedMissingFields        should contain("claimReferenceNumberCheck.missingDetails")
          expectedMissingFields.size shouldBe 1
        }
      }

      "should render the page with reference number input field missing when claimingReferenceNumber is true" in {
        val incompleteAnswers = RepaymentClaimDetailsAnswers(
          claimingGiftAid = Some(true),
          claimingTaxDeducted = Some(false),
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingReferenceNumber = Some(true)
        )
        val sessionData       = SessionData(
          unsubmittedClaimId = Some("123"),
          charitiesReference = testCharitiesReference,
          lastUpdatedReference = Some("123"),
          repaymentClaimDetailsAnswers =
            Some(incompleteAnswers) // TODO: MIGRATION - change to: repaymentClaimDetailsAnswers = incompleteAnswers
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        val expectedMissingFields = incompleteAnswers.missingFields

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RepaymentClaimDetailsIncompleteAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RepaymentClaimDetailsIncompleteAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url,
            expectedMissingFields,
            isAgent = false
          ).body

          expectedMissingFields        should contain("claimReferenceNumberInput.missingDetails")
          expectedMissingFields.size shouldBe 1
        }
      }

      "should render agent page with default missing fields when no session data present" in {

        given application: Application = applicationBuilder(affinityGroup = AffinityGroup.Agent).build()

        val defaultMissingFields = RepaymentClaimDetailsAnswers.getMissingFieldsForAgent(None)

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RepaymentClaimDetailsIncompleteAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RepaymentClaimDetailsIncompleteAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url,
            defaultMissingFields,
            isAgent = true
          ).body
        }
      }

      "should render agent page with agent-specific missing fields when answers are incomplete" in {
        val incompleteAnswers = RepaymentClaimDetailsAnswers()
        val sessionData       = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some("123"),
          lastUpdatedReference = Some("123"),
          repaymentClaimDetailsAnswers = Some(incompleteAnswers)
        )

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RepaymentClaimDetailsIncompleteAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view                  = application.injector.instanceOf[RepaymentClaimDetailsIncompleteAnswersView]
          val expectedMissingFields = incompleteAnswers.agentMissingFields

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url,
            expectedMissingFields,
            isAgent = true
          ).body

          expectedMissingFields should contain("claimingTaxDeducted.agent.missingDetails")
          expectedMissingFields should contain("claimingUnderGiftAidSmallDonationsScheme.agent.missingDetails")
          expectedMissingFields should contain("claimReferenceNumber.agent.missingDetails")
        }
      }
    }
  }
}

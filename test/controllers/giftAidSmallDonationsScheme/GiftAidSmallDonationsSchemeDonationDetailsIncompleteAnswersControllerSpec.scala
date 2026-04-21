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

package controllers.giftAidSmallDonationsScheme

import play.api.test.FakeRequest
import play.api.mvc.AnyContentAsEmpty
import controllers.ControllerSpec
import views.html.GiftAidSmallDonationsSchemeDonationDetailsIncompleteAnswersView
import play.api.Application
import models.{
  GiftAidSmallDonationsSchemeClaim,
  GiftAidSmallDonationsSchemeDonationDetailsAnswers,
  RepaymentClaimDetailsAnswers,
  SessionData
}

class GiftAidSmallDonationsSchemeDonationDetailsIncompleteAnswersControllerSpec extends ControllerSpec {

  private val checkYourGasdsDonationDetailsUrl = "/charities-claims/check-your-GASDS-donation-details"

  private val completeRepaymentClaimDetailsAnswers = RepaymentClaimDetailsAnswers(
    claimingGiftAid = Some(false),
    claimingTaxDeducted = Some(false),
    claimingUnderGiftAidSmallDonationsScheme = Some(true),
    claimingReferenceNumber = Some(false),
    claimingDonationsNotFromCommunityBuilding = Some(true),
    claimingDonationsCollectedInCommunityBuildings = Some(false),
    connectedToAnyOtherCharities = Some(false),
    makingAdjustmentToPreviousClaim = Some(false)
  )

  "GiftAidSmallDonationsSchemeDonationDetailsIncompleteAnswersController" - {
    "onPageLoad" - {
      "should redirect to claim complete when submissionReference is defined" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some("123"),
          lastUpdatedReference = Some("123"),
          repaymentClaimDetailsAnswers = Some(completeRepaymentClaimDetailsAnswers),
          submissionReference = Some(testCharitiesReference)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(
              GET,
              routes.GiftAidSmallDonationsSchemeDonationDetailsIncompleteAnswersController.onPageLoad.url
            )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }

      "should render the page with default missing fields when no GASDS answers present" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some("123"),
          lastUpdatedReference = Some("123"),
          repaymentClaimDetailsAnswers = Some(completeRepaymentClaimDetailsAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(
              GET,
              routes.GiftAidSmallDonationsSchemeDonationDetailsIncompleteAnswersController.onPageLoad.url
            )

          val result = route(application, request).value

          val view                 = application.injector.instanceOf[GiftAidSmallDonationsSchemeDonationDetailsIncompleteAnswersView]
          val defaultMissingFields = GiftAidSmallDonationsSchemeDonationDetailsAnswers.getMissingFields(None)

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(
            checkYourGasdsDonationDetailsUrl,
            defaultMissingFields
          ).body
        }
      }

      "should render the page with missing fields when GASDS answers have no claims" in {
        val incompleteAnswers = GiftAidSmallDonationsSchemeDonationDetailsAnswers()
        val sessionData       = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some("123"),
          lastUpdatedReference = Some("123"),
          repaymentClaimDetailsAnswers = Some(completeRepaymentClaimDetailsAnswers),
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(incompleteAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(
              GET,
              routes.GiftAidSmallDonationsSchemeDonationDetailsIncompleteAnswersController.onPageLoad.url
            )

          val result = route(application, request).value

          val view                  = application.injector.instanceOf[GiftAidSmallDonationsSchemeDonationDetailsIncompleteAnswersView]
          val expectedMissingFields = incompleteAnswers.missingFields

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(
            checkYourGasdsDonationDetailsUrl,
            expectedMissingFields
          ).body

          expectedMissingFields should contain("giftAidSmallDonationsSchemeDonationDetails.missingDetails")
        }
      }

      "should render the page with missing field for an incomplete claim slot" in {
        val incompleteAnswers = GiftAidSmallDonationsSchemeDonationDetailsAnswers(
          claims = Some(
            Seq(
              Some(
                GiftAidSmallDonationsSchemeClaim(taxYear = 2025, amountOfDonationsReceived = Some(BigDecimal(1000.00)))
              ),
              None
            )
          )
        )
        val sessionData       = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some("123"),
          lastUpdatedReference = Some("123"),
          repaymentClaimDetailsAnswers = Some(completeRepaymentClaimDetailsAnswers),
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(incompleteAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(
              GET,
              routes.GiftAidSmallDonationsSchemeDonationDetailsIncompleteAnswersController.onPageLoad.url
            )

          val result = route(application, request).value

          val view                  = application.injector.instanceOf[GiftAidSmallDonationsSchemeDonationDetailsIncompleteAnswersView]
          val expectedMissingFields = incompleteAnswers.missingFields

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(
            checkYourGasdsDonationDetailsUrl,
            expectedMissingFields
          ).body

          expectedMissingFields        should contain("giftAidSmallDonationsSchemeDonationDetails.claim2.missingDetails")
          expectedMissingFields.size shouldBe 1
        }
      }

      "should render the page with no missing fields when all claims are complete" in {
        val completeAnswers = GiftAidSmallDonationsSchemeDonationDetailsAnswers(
          claims = Some(
            Seq(
              Some(
                GiftAidSmallDonationsSchemeClaim(taxYear = 2025, amountOfDonationsReceived = Some(BigDecimal(1000.00)))
              )
            )
          )
        )
        val sessionData     = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some("123"),
          lastUpdatedReference = Some("123"),
          repaymentClaimDetailsAnswers = Some(completeRepaymentClaimDetailsAnswers),
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(completeAnswers)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(
              GET,
              routes.GiftAidSmallDonationsSchemeDonationDetailsIncompleteAnswersController.onPageLoad.url
            )

          val result = route(application, request).value

          val view = application.injector.instanceOf[GiftAidSmallDonationsSchemeDonationDetailsIncompleteAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(
            checkYourGasdsDonationDetailsUrl,
            Seq.empty
          ).body
        }
      }

      "should redirect when repayment claim details are not complete" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(
              GET,
              routes.GiftAidSmallDonationsSchemeDonationDetailsIncompleteAnswersController.onPageLoad.url
            )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
        }
      }
    }
  }
}

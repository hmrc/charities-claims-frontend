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

import controllers.ControllerSpec
import models.{RepaymentClaimDetailsAnswers, SessionData}
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.GasdsAdjustmentAmountCheckYourAnswersView

class GasdsAdjustmentAmountCheckYourAnswersControllerSpec extends ControllerSpec {

  val sessionData: SessionData = SessionData(
    charitiesReference = testCharitiesReference,
    unsubmittedClaimId = Some("test-claim-id"),
    repaymentClaimDetailsAnswers = Some(
      RepaymentClaimDetailsAnswers(
        claimingGiftAid = Some(false),
        claimingTaxDeducted = Some(false),
        claimingUnderGiftAidSmallDonationsScheme = Some(true),
        claimingDonationsNotFromCommunityBuilding = Some(false),
        claimingDonationsCollectedInCommunityBuildings = Some(false),
        makingAdjustmentToPreviousClaim = Some(true),
        connectedToAnyOtherCharities = Some(false),
        claimingReferenceNumber = Some(false)
      )
    )
  )

  "GasdsAdjustmentAmountCheckYourAnswersController" - {
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
            FakeRequest(GET, routes.GasdsAdjustmentAmountCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
      "should render the page correctly when the guard condition is met" in {
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.GasdsAdjustmentAmountCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[GasdsAdjustmentAmountCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual
            view(sessionData.giftAidSmallDonationsSchemeDonationDetailsAnswers).body
        }
      }

      "should redirect to task list page when makingAdjustmentToPreviousClaim is false" in {
        given application: Application = applicationBuilder(sessionData =
          sessionData.copy(
            repaymentClaimDetailsAnswers = completeGasdsSession.repaymentClaimDetailsAnswers.map(
              _.copy(makingAdjustmentToPreviousClaim = Some(false))
            )
          )
        ).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.GasdsAdjustmentAmountCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result).value shouldEqual controllers.routes.ClaimsTaskListController.onPageLoad.url
        }
      }

      "should redirect to task list page when repaymentClaimsDetails is incomplete" in {
        given application: Application = applicationBuilder(sessionData =
          sessionData.copy(
            repaymentClaimDetailsAnswers = None
          )
        ).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.GasdsAdjustmentAmountCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result).value shouldEqual controllers.routes.ClaimsTaskListController.onPageLoad.url
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
            FakeRequest(GET, routes.GasdsAdjustmentAmountCheckYourAnswersController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
      "redirect to WhichTaxYearAreYouClaimingFor when claiming under GASDS" in {
        given application: Application = applicationBuilder(sessionData =
          sessionData.copy(
            repaymentClaimDetailsAnswers = sessionData.repaymentClaimDetailsAnswers.map(
              _.copy(claimingUnderGiftAidSmallDonationsScheme = Some(true))
            )
          )
        ).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.GasdsAdjustmentAmountCheckYourAnswersController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result).value shouldEqual
            routes.WhichTaxYearAreYouClaimingForController.onPageLoad(1).url
        }
      }

      "redirect to GASDS donation details check page when not claiming under GASDS" in {
        given application: Application = applicationBuilder(sessionData =
          sessionData.copy(
            repaymentClaimDetailsAnswers = sessionData.repaymentClaimDetailsAnswers.map(
              _.copy(claimingUnderGiftAidSmallDonationsScheme = Some(false))
            )
          )
        ).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.GasdsAdjustmentAmountCheckYourAnswersController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result).value shouldEqual
            "/charities-claims/check-your-GASDS-donation-details"
        }
      }
    }
  }
}

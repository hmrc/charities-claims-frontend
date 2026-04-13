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
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.AdjustmentToGiftAidOverclaimedView
import models.RepaymentClaimDetailsAnswers
import models.SessionData
import forms.AmountFormProvider
import models.GiftAidSmallDonationsSchemeDonationDetailsAnswers
import play.api.mvc.AnyContentAsFormUrlEncoded

class AdjustmentToGiftAidOverclaimedControllerSpec extends ControllerSpec {

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

  "AdjustmentToGiftAidOverclaimedController" - {
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
            FakeRequest(GET, routes.AdjustmentToGiftAidOverclaimedController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
      "should render the page correctly first time" in {
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AdjustmentToGiftAidOverclaimedController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AdjustmentToGiftAidOverclaimedView]

          status(result) shouldEqual OK

          val form = application.injector
            .instanceOf[AmountFormProvider]
            .apply(
              errorRequired = "adjustmentToGiftAidOverclaimed.error.required",
              formatErrorMsg = "adjustmentToGiftAidOverclaimed.error.invalid",
              allowZero = true
            )

          contentAsString(result) shouldEqual view(form).body
        }
      }

      "should render the page correctly with an existing adjustment amount" in {
        given application: Application = applicationBuilder(sessionData =
          GiftAidSmallDonationsSchemeDonationDetailsAnswers.setAdjustmentForGiftAidOverClaimed(BigDecimal(123.45))(using
            sessionData
          )
        ).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AdjustmentToGiftAidOverclaimedController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AdjustmentToGiftAidOverclaimedView]

          status(result) shouldEqual OK

          val form = application.injector
            .instanceOf[AmountFormProvider]
            .apply(
              errorRequired = "adjustmentToGiftAidOverclaimed.error.required",
              formatErrorMsg = "adjustmentToGiftAidOverclaimed.error.invalid",
              allowZero = true
            )

          contentAsString(result) shouldEqual view(form.fill(BigDecimal(123.45))).body
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
            FakeRequest(GET, routes.AdjustmentToGiftAidOverclaimedController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
      "should redirect to the next page when provided amount is valid" in {
        val sessionData =
          RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(true)(using completeGasdsSession)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.AdjustmentToGiftAidOverclaimedController.onSubmit.url)
              .withFormUrlEncodedBody("amount" -> "123.45")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            "/charities-claims/check-gasds-adjustment-amount"
          )
        }
      }

      "should render error page when provided amount is empty" in {
        val sessionData =
          RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(true)(using completeGasdsSession)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.AdjustmentToGiftAidOverclaimedController.onSubmit.url)
              .withFormUrlEncodedBody("amount" -> "")

          val result = route(application, request).value

          val view = application.injector.instanceOf[AdjustmentToGiftAidOverclaimedView]

          status(result) shouldEqual BAD_REQUEST

          val form = application.injector
            .instanceOf[AmountFormProvider]
            .apply(
              errorRequired = "adjustmentToGiftAidOverclaimed.error.required",
              formatErrorMsg = "adjustmentToGiftAidOverclaimed.error.invalid",
              allowZero = true
            )

          contentAsString(result) shouldEqual view(form.bind(Map("amount" -> ""))).body
        }
      }

      "should render error page when provided amount is invalid" in {
        val sessionData =
          RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(true)(using completeGasdsSession)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.AdjustmentToGiftAidOverclaimedController.onSubmit.url)
              .withFormUrlEncodedBody("amount" -> "invalid")

          val result = route(application, request).value

          val view = application.injector.instanceOf[AdjustmentToGiftAidOverclaimedView]

          status(result) shouldEqual BAD_REQUEST

          val form = application.injector
            .instanceOf[AmountFormProvider]
            .apply(
              errorRequired = "adjustmentToGiftAidOverclaimed.error.required",
              formatErrorMsg = "adjustmentToGiftAidOverclaimed.error.invalid",
              allowZero = true
            )

          contentAsString(result) shouldEqual view(form.bind(Map("amount" -> "invalid"))).body
        }
      }

    }
  }

}

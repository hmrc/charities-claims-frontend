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
import forms.AmountFormProvider
import models.*
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import views.html.DonationAmountYouAreClaimingView

class DonationAmountYouAreClaimingControllerSpec extends ControllerSpec {

  private val index = 1

  private val validTaxYear = 2026

  val baseSession: SessionData = SessionData(
    charitiesReference = testCharitiesReference,
    unsubmittedClaimId = Some("test-claim-id"),
    repaymentClaimDetailsAnswers = Some(
      RepaymentClaimDetailsAnswers(
        claimingGiftAid = Some(false),
        claimingTaxDeducted = Some(false),
        claimingUnderGiftAidSmallDonationsScheme = Some(true),
        claimingDonationsNotFromCommunityBuilding = Some(false),
        claimingDonationsCollectedInCommunityBuildings = Some(false),
        makingAdjustmentToPreviousClaim = Some(false),
        connectedToAnyOtherCharities = Some(false),
        claimingReferenceNumber = Some(false)
      )
    ),
    giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
      GiftAidSmallDonationsSchemeDonationDetailsAnswers(
        claims = Some(Seq(Some(GiftAidSmallDonationsSchemeClaimAnswers(validTaxYear, None))))
      )
    )
  )

  private def withClaim(session: SessionData, amount: BigDecimal = 0): SessionData =
    GiftAidSmallDonationsSchemeDonationDetailsAnswers.setClaim(
      index - 1,
      GiftAidSmallDonationsSchemeClaimAnswers(
        taxYear = validTaxYear,
        amountOfDonationsReceived = Some(amount)
      )
    )(using session)

  "DonationAmountYouAreClaimingController" - {

    "onPageLoad" - {

      "should render the page correctly first time" in {
        given application: Application =
          applicationBuilder(sessionData = baseSession).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.DonationAmountYouAreClaimingController.onPageLoad(index).url)

          val result = route(application, request).value

          val view         = application.injector.instanceOf[DonationAmountYouAreClaimingView]
          val formProvider = application.injector.instanceOf[AmountFormProvider]
          given Messages   = messages(application)

          val label = messages("taxYear.first")

          val form = formProvider(
            errorRequired = messages("donationAmountYouAreClaiming.error.required", label),
            formatErrorMsg = messages("donationAmountYouAreClaiming.error.invalid", label),
            maxLengthErrorMsg = messages("donationAmountYouAreClaiming.error.maxLength"),
            allowZero = true
          )

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, index).body
        }
      }

      "should pre-fill existing amount" in {
        val session = withClaim(baseSession, BigDecimal(123.45))

        given application: Application =
          applicationBuilder(sessionData = session).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.DonationAmountYouAreClaimingController.onPageLoad(index).url)

          val result = route(application, request).value

          val view         = application.injector.instanceOf[DonationAmountYouAreClaimingView]
          val formProvider = application.injector.instanceOf[AmountFormProvider]
          given Messages   = messages(application)

          val label = messages("taxYear.first")

          val form = formProvider(
            errorRequired = messages("donationAmountYouAreClaiming.error.required", label),
            formatErrorMsg = messages("donationAmountYouAreClaiming.error.invalid", label),
            maxLengthErrorMsg = messages("donationAmountYouAreClaiming.error.maxLength"),
            allowZero = true
          )

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(BigDecimal(123.45)), index).body
        }
      }
    }

    "onSubmit" - {

      "should redirect when valid amount is submitted" in {
        val session = withClaim(baseSession)

        given application: Application =
          applicationBuilder(sessionData = session).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DonationAmountYouAreClaimingController.onSubmit(index).url)
              .withFormUrlEncodedBody("amount" -> "123.45")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimDetailsForTaxYearCheckYourAnswersController.onPageLoad(index).url
          )
        }
      }

      "should return BAD_REQUEST when amount is empty" in {
        val session = withClaim(baseSession)

        given application: Application =
          applicationBuilder(sessionData = session).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DonationAmountYouAreClaimingController.onSubmit(index).url)
              .withFormUrlEncodedBody("amount" -> "")

          val result = route(application, request).value

          val view         = application.injector.instanceOf[DonationAmountYouAreClaimingView]
          val formProvider = application.injector.instanceOf[AmountFormProvider]
          given Messages   = messages(application)

          val label = messages("taxYear.first")

          val form = formProvider(
            errorRequired = messages("donationAmountYouAreClaiming.error.required", label),
            formatErrorMsg = messages("donationAmountYouAreClaiming.error.invalid", label),
            maxLengthErrorMsg = messages("donationAmountYouAreClaiming.error.maxLength"),
            allowZero = true
          )

          status(result) shouldEqual BAD_REQUEST
          contentAsString(result) shouldEqual view(form.bind(Map("amount" -> "")), index).body
        }
      }

      "should redirect to task list when claim is missing" in {
        val sessionWithoutClaim = baseSession.copy(
          giftAidSmallDonationsSchemeDonationDetailsAnswers = None
        )

        given application: Application =
          applicationBuilder(sessionData = sessionWithoutClaim).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DonationAmountYouAreClaimingController.onSubmit(index).url)
              .withFormUrlEncodedBody("amount" -> "123")

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

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
import forms.TaxYearFormProvider
import models.*
import models.Mode.{CheckMode, NormalMode}
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import views.html.WhichTaxYearAreYouClaimingForView

class WhichTaxYearAreYouClaimingForControllerSpec extends ControllerSpec {

  private val index = 1

  val baseSession: SessionData = SessionData(
    charitiesReference = testCharitiesReference,
    unsubmittedClaimId = Some("test-claim-id"),
    repaymentClaimDetailsAnswers = Some(
      RepaymentClaimDetailsAnswers(
        claimingGiftAid = Some(false),
        claimingTaxDeducted = Some(false),
        claimingUnderGiftAidSmallDonationsScheme = Some(true),
        claimingDonationsNotFromCommunityBuilding = Some(true),
        claimingDonationsCollectedInCommunityBuildings = Some(false),
        makingAdjustmentToPreviousClaim = Some(false),
        connectedToAnyOtherCharities = Some(false),
        claimingReferenceNumber = Some(false)
      )
    )
  )

  private def withClaim(session: SessionData, year: Int): SessionData =
    GiftAidSmallDonationsSchemeDonationDetailsAnswers.setClaim(
      index - 1,
      GiftAidSmallDonationsSchemeClaimAnswers(
        taxYear = year,
        amountOfDonationsReceived = None
      )
    )(using session)

  private val validYear  = 2026
  private val tooOldYear = 2023
  private val futureYear = 2028

  "WhichTaxYearAreYouClaimingForController" - {

    "onPageLoad" - {

      "should render page for first year" in {
        given application: Application =
          applicationBuilder(sessionData = baseSession).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.WhichTaxYearAreYouClaimingForController.onPageLoad(index, NormalMode).url)

          val result = route(application, request).value

          val view         = application.injector.instanceOf[WhichTaxYearAreYouClaimingForView]
          val formProvider = application.injector.instanceOf[TaxYearFormProvider]
          given Messages   = messages(application)

          val label = messages("taxYear.first")

          val form = formProvider(
            requiredKey = messages("whichTaxYearAreYouClaimingFor.error.required", label),
            invalidKey = messages("whichTaxYearAreYouClaimingFor.error.invalid", label)
          )

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, index, NormalMode).body
        }
      }

      "should pre-fill existing tax year" in {
        val session = withClaim(baseSession, validYear)

        given application: Application =
          applicationBuilder(sessionData = session).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.WhichTaxYearAreYouClaimingForController.onPageLoad(index, NormalMode).url)

          val result = route(application, request).value

          val view         = application.injector.instanceOf[WhichTaxYearAreYouClaimingForView]
          val formProvider = application.injector.instanceOf[TaxYearFormProvider]
          given Messages   = messages(application)

          val label = messages("taxYear.first")

          val form = formProvider(
            requiredKey = messages("whichTaxYearAreYouClaimingFor.error.required", label),
            invalidKey = messages("whichTaxYearAreYouClaimingFor.error.invalid", label)
          )

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(validYear), index, NormalMode).body
        }
      }
    }

    "onSubmit" - {

      "should redirect to donation amount page when valid tax year submitted in NormalMode" in {
        given application: Application =
          applicationBuilder(sessionData = baseSession).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.WhichTaxYearAreYouClaimingForController.onSubmit(index, NormalMode).url)
              .withFormUrlEncodedBody("value" -> validYear.toString)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.DonationAmountYouAreClaimingController.onPageLoad(index, NormalMode).url
          )
        }
      }

      "should redirect to CheckYourAnswers page in CheckMode" in {
        given application: Application =
          applicationBuilder(sessionData = baseSession).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(
              POST,
              routes.WhichTaxYearAreYouClaimingForController.onSubmit(index, CheckMode).url
            ).withFormUrlEncodedBody("value" -> validYear.toString)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result).value shouldEqual
            controllers.giftAidSmallDonationsScheme.routes.ClaimDetailsForTaxYearCheckYourAnswersController
              .onPageLoad(index)
              .url
        }
      }

      "should return BAD_REQUEST when empty" in {
        given application: Application =
          applicationBuilder(sessionData = baseSession).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.WhichTaxYearAreYouClaimingForController.onSubmit(index, NormalMode).url)
              .withFormUrlEncodedBody("value" -> "")

          val result = route(application, request).value

          val view         = application.injector.instanceOf[WhichTaxYearAreYouClaimingForView]
          val formProvider = application.injector.instanceOf[TaxYearFormProvider]
          given Messages   = messages(application)

          val label = messages("taxYear.first")

          val form = formProvider(
            requiredKey = messages("whichTaxYearAreYouClaimingFor.error.required", label),
            invalidKey = messages("whichTaxYearAreYouClaimingFor.error.invalid", label)
          )

          status(result) shouldEqual BAD_REQUEST
          contentAsString(result) shouldEqual view(form.bind(Map("value" -> "")), index, NormalMode).body
        }
      }

      "should return BAD_REQUEST for duplicate year" in {
        val session =
          GiftAidSmallDonationsSchemeDonationDetailsAnswers
            .setClaim(0, GiftAidSmallDonationsSchemeClaimAnswers(validYear, None))(using baseSession)

        given application: Application =
          applicationBuilder(sessionData = session).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.WhichTaxYearAreYouClaimingForController.onSubmit(2, NormalMode).url)
              .withFormUrlEncodedBody("value" -> validYear.toString)

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
          contentAsString(result) should include(
            messages(application)("whichTaxYearAreYouClaimingFor.error.duplicate")
          )
        }
      }

      "should return BAD_REQUEST when year is too old" in {
        given application: Application =
          applicationBuilder(sessionData = baseSession).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.WhichTaxYearAreYouClaimingForController.onSubmit(index, NormalMode).url)
              .withFormUrlEncodedBody("value" -> tooOldYear.toString)

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
          contentAsString(result) should include(
            messages(application)("whichTaxYearAreYouClaimingFor.error.tooOld", "2024") // adjust min if needed
          )
        }
      }

      "should return BAD_REQUEST when year is in future/current" in {
        given application: Application =
          applicationBuilder(sessionData = baseSession).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.WhichTaxYearAreYouClaimingForController.onSubmit(index, NormalMode).url)
              .withFormUrlEncodedBody("value" -> futureYear.toString)

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
          contentAsString(result) should include(
            messages(application)("whichTaxYearAreYouClaimingFor.error.future")
          )
        }
      }
    }
  }
}

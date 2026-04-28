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
import forms.YesNoFormProvider
import models.*
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import views.html.ClaimAddedForTaxYearView

class ClaimAddedForTaxYearControllerSpec extends ControllerSpec {

  private val taxYear1 = 2024
  private val taxYear2 = 2025
  private val taxYear3 = 2026

  private def sessionWithTaxYears(years: Seq[Int]): SessionData =
    SessionData(
      charitiesReference = testCharitiesReference,
      unsubmittedClaimId = Some("test-claim-id"),
      repaymentClaimDetailsAnswers = Some(
        RepaymentClaimDetailsAnswers(
          claimingGiftAid = Some(true),
          claimingTaxDeducted = Some(true),
          claimingUnderGiftAidSmallDonationsScheme = Some(true),
          claimingDonationsNotFromCommunityBuilding = Some(true),
          claimingDonationsCollectedInCommunityBuildings = Some(true),
          makingAdjustmentToPreviousClaim = Some(false),
          connectedToAnyOtherCharities = Some(true),
          claimingReferenceNumber = Some(false)
        )
      ),
      giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
        GiftAidSmallDonationsSchemeDonationDetailsAnswers(
          claims = Some(
            years.map(y => Some(GiftAidSmallDonationsSchemeClaimAnswers(y, Some(BigDecimal(333.33)))))
          )
        )
      )
    )

  private def withInvalidTaxYear(session: SessionData): SessionData =
    session.copy(
      giftAidSmallDonationsSchemeDonationDetailsAnswers =
        session.giftAidSmallDonationsSchemeDonationDetailsAnswers.map { gasds =>
          gasds.copy(
            claims = gasds.claims.map { claims =>
              claims.map {
                case Some(claim) => Some(claim.copy(taxYear = 0))
                case None        => None
              }
            }
          )
        }
    )

  private def form(app: Application) = {
    given msgs: Messages = messages(app)

    new YesNoFormProvider()(msgs("claimAddedForTaxYear.error.required"))
  }

  "ClaimAddedForTaxYearController" - {

    "onPageLoad" - {

      "should render the page correctly" in {
        given application: Application =
          applicationBuilder(sessionData = sessionWithTaxYears(Seq(taxYear1, taxYear2))).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimAddedForTaxYearController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ClaimAddedForTaxYearView]
          status(result)          shouldBe OK
          contentAsString(result) shouldBe view(
            form(application),
            Some(
              Seq(
                (
                  "Tax year 2024",
                  Seq(
                    ("/charities-claims/check-claim-details-for-tax-year/1", "site.change", "Tax year 2024"),
                    ("/charities-claims/remove-claim-for-tax-year/1", "site.remove", "Tax year 2024")
                  )
                ),
                (
                  "Tax year 2025",
                  Seq(
                    ("/charities-claims/check-claim-details-for-tax-year/2", "site.change", "Tax year 2025"),
                    ("/charities-claims/remove-claim-for-tax-year/2", "site.remove", "Tax year 2025")
                  )
                )
              )
            ),
            2,
            messages(application)("claimAddedForTaxYear.singularOrPlural.plural")
          ).body
        }
      }
    }

    "onSubmit" - {

      "should redirect immediately when countOfTaxYears == 3" in {
        given application: Application =
          applicationBuilder(sessionData = sessionWithTaxYears(Seq(taxYear1, taxYear2, taxYear3))).build()

        running(application) {
          val request =
            FakeRequest(POST, routes.ClaimAddedForTaxYearController.onSubmit.url)

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(
            "/charities-claims/check-your-gift-aid-small-donations-scheme-donation-details"
          )
        }
      }

      "should redirect to next tax year when 'Yes' selected" in {
        given application: Application =
          applicationBuilder(sessionData = sessionWithTaxYears(Seq(taxYear1))).build()

        running(application) {
          import models.Mode.NormalMode
          val request =
            FakeRequest(POST, routes.ClaimAddedForTaxYearController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(
            controllers.giftAidSmallDonationsScheme.routes.WhichTaxYearAreYouClaimingForController
              .onPageLoad(2, NormalMode)
              .url
          )
        }
      }

      "should redirect to check details when 'No' selected" in {
        given application: Application =
          applicationBuilder(sessionData = sessionWithTaxYears(Seq(taxYear1))).build()

        running(application) {
          val request =
            FakeRequest(POST, routes.ClaimAddedForTaxYearController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(
            "/charities-claims/check-your-gift-aid-small-donations-scheme-donation-details"
          )
        }
      }

      "should return BAD_REQUEST when form is invalid" in {
        given application: Application =
          applicationBuilder(sessionData = withInvalidTaxYear(sessionWithTaxYears(Seq(taxYear1)))).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimAddedForTaxYearController.onSubmit.url)
              .withFormUrlEncodedBody()

          val result = route(application, request).value

          status(result) shouldBe BAD_REQUEST
        }
      }
    }

    "guard behaviour" - {

      "should redirect when session is missing required data" in {
        given application: Application =
          applicationBuilder().build()

        running(application) {
          val request =
            FakeRequest(GET, routes.ClaimAddedForTaxYearController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldBe SEE_OTHER
        }
      }
    }
  }
}

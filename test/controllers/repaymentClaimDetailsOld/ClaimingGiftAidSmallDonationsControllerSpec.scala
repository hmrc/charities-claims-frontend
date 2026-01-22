/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.repaymentclaimdetailsold

import play.api.test.FakeRequest
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import controllers.ControllerSpec
import views.html.ClaimingGiftAidSmallDonationsView
import play.api.Application
import forms.YesNoFormProvider
import models.{GiftAidSmallDonationsSchemeDonationDetailsAnswers, RepaymentClaimDetailsAnswersOld, SessionData}
import play.api.data.Form
import models.Mode.*

class ClaimingGiftAidSmallDonationsControllerSpec extends ControllerSpec {

  private val form: Form[Boolean] = new YesNoFormProvider()()

  "ClaimingGiftAidSmallDonationsController" - {
    "onPageLoad" - {
      "should render the page correctly" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingGiftAidSmallDonationsController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimingGiftAidSmallDonationsView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with true value" in {

        val sessionData = RepaymentClaimDetailsAnswersOld.setClaimingUnderGiftAidSmallDonationsScheme(true)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingGiftAidSmallDonationsController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimingGiftAidSmallDonationsView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(true), NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with false value" in {

        val sessionData = RepaymentClaimDetailsAnswersOld.setClaimingUnderGiftAidSmallDonationsScheme(false)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingGiftAidSmallDonationsController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimingGiftAidSmallDonationsView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(false), NormalMode).body
        }
      }
    }

    "onSubmit" - {
      "should redirect to the next page when the value is true" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingGiftAidSmallDonationsController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimingReferenceNumberCheckController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to the next page when the value is false" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingGiftAidSmallDonationsController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimingReferenceNumberCheckController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect back to cya page when the value is true" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingGiftAidSmallDonationsController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.CheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "should redirect back to cya page when the value is false" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingGiftAidSmallDonationsController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.CheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "should reload the page with errors when a required field is missing" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingGiftAidSmallDonationsController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      "onSubmit with warning" - {
        "should trigger warning when changing to false when GASDS schedule data present" in {
          val sessionData = SessionData.empty.copy(
            repaymentClaimDetailsAnswersOld =
              RepaymentClaimDetailsAnswersOld(claimingUnderGiftAidSmallDonationsScheme = Some(true)),
            giftAidSmallDonationsSchemeDonationDetailsAnswers =
              Some(GiftAidSmallDonationsSchemeDonationDetailsAnswers())
          )

          given application: Application = applicationBuilder(sessionData = sessionData).build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.ClaimingGiftAidSmallDonationsController.onSubmit(NormalMode).url)
                .withFormUrlEncodedBody("value" -> "false")

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              routes.ClaimingGiftAidSmallDonationsController.onPageLoad(NormalMode).url
            )
            flash(result).get("warning") shouldEqual Some("true")
          }
        }

        "should redirect to the next page in NormalMode when value is false and warning has been shown" in {
          val sessionData = SessionData.empty.copy(
            repaymentClaimDetailsAnswersOld =
              RepaymentClaimDetailsAnswersOld(claimingUnderGiftAidSmallDonationsScheme = Some(true)),
            giftAidSmallDonationsSchemeDonationDetailsAnswers =
              Some(GiftAidSmallDonationsSchemeDonationDetailsAnswers())
          )

          given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.ClaimingGiftAidSmallDonationsController.onSubmit(NormalMode).url)
                .withFormUrlEncodedBody(
                  "value"        -> "false",
                  "warningShown" -> "true"
                )

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              routes.ClaimingReferenceNumberCheckController.onPageLoad(NormalMode).url
            )
            flash(result).get("warning") shouldEqual None
          }
        }

        "should not show warning when no previous GASDS schedule data exists" in {
          val sessionData = SessionData.empty.copy(
            repaymentClaimDetailsAnswersOld =
              RepaymentClaimDetailsAnswersOld(claimingUnderGiftAidSmallDonationsScheme = Some(false))
          )

          given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.ClaimingGiftAidSmallDonationsController.onSubmit(NormalMode).url)
                .withFormUrlEncodedBody("value" -> "false")

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            flash(result).get("warning") shouldEqual None
          }
        }

        "should redirect to CheckYourAnswers in CheckMode after warning confirmation" in {
          val sessionData = SessionData.empty.copy(
            repaymentClaimDetailsAnswersOld =
              RepaymentClaimDetailsAnswersOld(claimingUnderGiftAidSmallDonationsScheme = Some(true)),
            giftAidSmallDonationsSchemeDonationDetailsAnswers =
              Some(GiftAidSmallDonationsSchemeDonationDetailsAnswers())
          )

          given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.ClaimingGiftAidSmallDonationsController.onSubmit(CheckMode).url)
                .withFormUrlEncodedBody("value" -> "false", "warningShown" -> "true")

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result).value shouldEqual routes.CheckYourAnswersController.onPageLoad.url
          }
        }

        "should render warning parameter hidden field when warning flash present" in {
          val sessionData = SessionData.empty.copy(
            repaymentClaimDetailsAnswersOld =
              RepaymentClaimDetailsAnswersOld(claimingUnderGiftAidSmallDonationsScheme = Some(true)),
            giftAidSmallDonationsSchemeDonationDetailsAnswers =
              Some(GiftAidSmallDonationsSchemeDonationDetailsAnswers())
          )

          given application: Application = applicationBuilder(sessionData = sessionData).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.ClaimingGiftAidSmallDonationsController.onPageLoad(NormalMode).url)
                .withFlash("warning" -> "true", "warningAnswer" -> "false")

            val result = route(application, request).value

            contentAsString(result) should include("warningShown")
          }
        }
      }
    }
  }
}

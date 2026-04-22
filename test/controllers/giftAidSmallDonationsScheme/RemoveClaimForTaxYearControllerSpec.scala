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
import views.html.RemoveClaimForTaxYearView

class RemoveClaimForTaxYearControllerSpec extends ControllerSpec {

  private val index        = 1
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
        claims = Some(Seq(Some(GiftAidSmallDonationsSchemeClaim(validTaxYear, None))))
      )
    )
  )

  private def form(app: Application) = {
    given msgs: Messages = messages(app)
    new YesNoFormProvider()(msgs("removeClaimForTaxYear.error.required", validTaxYear))
  }

  "RemoveClaimForTaxYearController" - {

    "onPageLoad" - {

      "should render the page with the correct tax year in the heading" in {
        given application: Application =
          applicationBuilder(sessionData = baseSession).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RemoveClaimForTaxYearController.onPageLoad(index).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RemoveClaimForTaxYearView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form(application), index, validTaxYear).body
        }
      }

      "should redirect to guard page when there is no claim for the given index" in {
        val sessionWithoutClaim = baseSession.copy(
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
            GiftAidSmallDonationsSchemeDonationDetailsAnswers(claims = None)
          )
        )

        given application: Application =
          applicationBuilder(sessionData = sessionWithoutClaim).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RemoveClaimForTaxYearController.onPageLoad(index).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
        }
      }

      "should redirect to guard page for an invalid index" in {
        given application: Application =
          applicationBuilder(sessionData = baseSession).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RemoveClaimForTaxYearController.onPageLoad(4).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
        }
      }
    }

    "onSubmit" - {

      "should remove claim and redirect when 'Yes' is selected" in {
        given application: Application =
          applicationBuilder(sessionData = baseSession).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RemoveClaimForTaxYearController.onSubmit(index).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            "/check-your-donation-details" // TODO redirect to the correct url once it is implemented
          )
        }
      }

      "should redirect without removing claim when 'No' is selected" in {
        given application: Application =
          applicationBuilder(sessionData = baseSession).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RemoveClaimForTaxYearController.onSubmit(index).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            "/check-your-donation-details" // TODO redirect to the correct url once it is implemented
          )
        }
      }

      "should return BAD_REQUEST when no option is selected" in {
        given application: Application =
          applicationBuilder(sessionData = baseSession).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RemoveClaimForTaxYearController.onSubmit(index).url)
              .withFormUrlEncodedBody()

          val result = route(application, request).value

          val view = application.injector.instanceOf[RemoveClaimForTaxYearView]

          status(result) shouldEqual BAD_REQUEST
          contentAsString(result) shouldEqual view(
            form(application).bind(Map.empty[String, String]),
            index,
            validTaxYear
          ).body
        }
      }
    }
  }
}

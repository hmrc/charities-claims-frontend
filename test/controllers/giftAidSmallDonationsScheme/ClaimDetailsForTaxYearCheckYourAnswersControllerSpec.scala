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
import models.*
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup
import views.html.ClaimDetailsForTaxYearCheckYourAnswersView

class ClaimDetailsForTaxYearCheckYourAnswersControllerSpec extends ControllerSpec {

  private val claim = GiftAidSmallDonationsSchemeClaimAnswers(
    taxYear = 2024,
    amountOfDonationsReceived = Some(BigDecimal(100))
  )

  private val baseSession: SessionData = SessionData(
    charitiesReference = testCharitiesReference,
    unsubmittedClaimId = Some("test-id"),
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

  "ClaimDetailsForTaxYearCheckYourAnswersController" - {

    "onPageLoad" - {

      "should render page with claim when present (index = 1)" in {
        val sessionData = baseSession.copy(
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
            GiftAidSmallDonationsSchemeDonationDetailsAnswers(
              claims = Some(Seq(Some(claim)))
            )
          )
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimDetailsForTaxYearCheckYourAnswersController.onPageLoad(1).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ClaimDetailsForTaxYearCheckYourAnswersView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(Some(claim), 1, false).body
        }
      }

      "should render page with claim when present (index = 1) - for Agent" in {
        val sessionData = baseSession.copy(
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
            GiftAidSmallDonationsSchemeDonationDetailsAnswers(
              claims = Some(Seq(Some(claim)))
            )
          )
        )

        given application: Application = applicationBuilder(sessionData = sessionData, AffinityGroup.Agent).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimDetailsForTaxYearCheckYourAnswersController.onPageLoad(1).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ClaimDetailsForTaxYearCheckYourAnswersView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(Some(claim), 1, true).body
        }
      }

      "should redirect to task list when guard fails" in {
        val sessionData = baseSession.copy(
          repaymentClaimDetailsAnswers = None
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimDetailsForTaxYearCheckYourAnswersController.onPageLoad(1).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result).value shouldEqual
            controllers.routes.ClaimsTaskListController.onPageLoad.url
        }
      }
    }

    "onSubmit" - {

      "should redirect to claim added page" in {
        given application: Application = applicationBuilder(sessionData = baseSession).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.ClaimDetailsForTaxYearCheckYourAnswersController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result).value shouldEqual
            "/charities-claims/claim-added-for-tax-year"
        }
      }

      "should redirect to task list when guard fails" in {
        val sessionData = baseSession.copy(
          repaymentClaimDetailsAnswers = None
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.ClaimDetailsForTaxYearCheckYourAnswersController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result).value shouldEqual
            controllers.routes.ClaimsTaskListController.onPageLoad.url
        }
      }
    }
  }
}

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

package controllers.repaymentclaimdetails

import play.api.test.FakeRequest
import play.api.mvc.AnyContentAsEmpty
import controllers.ControllerSpec
import views.html.CheckYourAnswersView
import play.api.Application
import models.RepaymentClaimDetailsAnswers
import models.*

class CheckYourAnswersControllerSpec extends ControllerSpec {

  "CheckYourAnswersController" - {
    "onPageLoad" - {
      "should render the page correctly when claiming reference number is true" in {

        val sessionData = SessionData(
          repaymentClaimDetailsAnswers = RepaymentClaimDetailsAnswers(
            claimingGiftAid = Some(true),
            claimingTaxDeducted = Some(false),
            claimingUnderGiftAidSmallDonationsScheme = Some(true),
            claimingReferenceNumber = Some(true),
            claimReferenceNumber = Some("12345678AB")
          )
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.repaymentClaimDetailsAnswers).body
        }
      }

      "should render the page correctly when claiming reference number is false " in {

        val sessionData = SessionData(
          repaymentClaimDetailsAnswers = RepaymentClaimDetailsAnswers(
            claimingGiftAid = Some(true),
            claimingTaxDeducted = Some(false),
            claimingUnderGiftAidSmallDonationsScheme = Some(true),
            claimingReferenceNumber = Some(false),
            claimReferenceNumber = None
          )
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.repaymentClaimDetailsAnswers).body
        }
      }

      "should render the page correctly when some answers are missing" in {

        val sessionData = SessionData(
          repaymentClaimDetailsAnswers = RepaymentClaimDetailsAnswers(
            claimingGiftAid = Some(true),
            claimingTaxDeducted = None,
            claimingUnderGiftAidSmallDonationsScheme = Some(true),
            claimingReferenceNumber = None,
            claimReferenceNumber = Some("12345678AB")
          )
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.repaymentClaimDetailsAnswers).body

        }
      }
    }
  }

  "onSubmit" - {
    "should save the claim and redirect to the next page" in {

      val sessionData = SessionData(
        repaymentClaimDetailsAnswers = RepaymentClaimDetailsAnswers(
          claimingGiftAid = Some(true),
          claimingTaxDeducted = Some(false),
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingReferenceNumber = Some(true),
          claimReferenceNumber = Some("12345678AB")
        )
      )

      given application: Application =
        applicationBuilder(sessionData = sessionData).mockSaveClaim.build()

      running(application) {
        given request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(POST, routes.CheckYourAnswersController.onSubmit.url)

        val result = route(application, request).value

        status(result) shouldEqual SEE_OTHER

        redirectLocation(result) shouldEqual Some("next-page-after-check-your-answers")

      }
    }

    "should redirect to the incomplete answers page if the answers are not complete" in {

      val sessionData = SessionData(
        repaymentClaimDetailsAnswers = RepaymentClaimDetailsAnswers(
          claimingGiftAid = Some(true),
          claimingTaxDeducted = None,
          claimingUnderGiftAidSmallDonationsScheme = Some(true),
          claimingReferenceNumber = Some(true),
          claimReferenceNumber = Some("12345678AB")
        )
      )

      given application: Application =
        applicationBuilder(sessionData = sessionData).build()

      running(application) {
        given request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(POST, routes.CheckYourAnswersController.onSubmit.url)

        val result = route(application, request).value

        status(result) shouldEqual SEE_OTHER

        redirectLocation(result) shouldEqual Some(routes.IncompleteAnswersController.onPageLoad.url)

      }
    }
  }
}

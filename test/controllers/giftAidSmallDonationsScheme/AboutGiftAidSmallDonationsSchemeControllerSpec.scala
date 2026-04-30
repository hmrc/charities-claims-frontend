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
import play.api.mvc.{AnyContentAsEmpty, BodyParsers}
import play.api.test.{FakeRequest, Helpers}
import views.html.AboutGiftAidSmallDonationsSchemeView
import models.{RepaymentClaimDetailsAnswers, SessionData}
import models.Mode.NormalMode

import uk.gov.hmrc.auth.core.AffinityGroup

class AboutGiftAidSmallDonationsSchemeControllerSpec extends ControllerSpec {
  val bodyParser: BodyParsers.Default = BodyParsers.Default(Helpers.stubPlayBodyParsers)

  "AboutGiftAidSmallDonationsSchemeController" - {
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
            FakeRequest(GET, routes.AboutGiftAidSmallDonationsSchemeController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
      "should render the page correctly when user is Organisation (isAgent = false)" in {
        val sessionData = completeGasdsSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Organisation).build()

        val isAgent = false
        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AboutGiftAidSmallDonationsSchemeController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AboutGiftAidSmallDonationsSchemeView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(isAgent).body
        }
      }

      "should render the page correctly when user is Agent" in {
        val sessionData = completeGasdsSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        val isAgent = true
        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AboutGiftAidSmallDonationsSchemeController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AboutGiftAidSmallDonationsSchemeView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(isAgent).body
        }
      }

      "should render ClaimsTaskListController if completeGasdsSession is false" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AboutGiftAidSmallDonationsSchemeController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
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
            FakeRequest(POST, routes.AboutGiftAidSmallDonationsSchemeController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
      "should redirect to the next page when makingAdjustmentToPreviousClaim is true" in {
        val sessionData =
          RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(true)(using completeGasdsSession)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.AboutGiftAidSmallDonationsSchemeController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.giftAidSmallDonationsScheme.routes.AdjustmentToGiftAidOverclaimedController
              .onPageLoad(NormalMode)
              .url
          )
        }
      }

      "should redirect to the next page when makingAdjustmentToPreviousClaim is false" in {
        val sessionData =
          RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(false)(using completeGasdsSession)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.AboutGiftAidSmallDonationsSchemeController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.giftAidSmallDonationsScheme.routes.WhichTaxYearAreYouClaimingForController
              .onPageLoad(1, NormalMode)
              .url
          )
        }
      }

      "should redirect to the ClaimsTaskListController when completeGasdsSession is false" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.AboutGiftAidSmallDonationsSchemeController.onSubmit.url)

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

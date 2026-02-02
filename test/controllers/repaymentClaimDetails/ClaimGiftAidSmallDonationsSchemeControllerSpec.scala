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

package controllers.repaymentClaimDetails

import play.api.test.FakeRequest
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import controllers.ControllerSpec
import play.api.Application
import forms.YesNoFormProvider
import play.api.data.Form
import models.Mode.*
import models.RepaymentClaimDetailsAnswers
import views.html.ClaimGiftAidSmallDonationsSchemeView

class ClaimGiftAidSmallDonationsSchemeControllerSpec extends ControllerSpec {

  private val form: Form[Boolean] = new YesNoFormProvider()("claimGASDS.error.required")

  "ClaimGiftAidSmallDonationsSchemeController" - {
    "onPageLoad" - {

      "should render the page correctly when claimingDonationsNotFromCommunityBuilding is true" in {
        val sessionData = RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(true)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimGiftAidSmallDonationsSchemeController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimGiftAidSmallDonationsSchemeView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(true), NormalMode).body
        }
      }

      "should render page not found if claimingDonationsNotFromCommunityBuilding is false" in {
        val sessionData = RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(false)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimGiftAidSmallDonationsSchemeController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.PageNotFoundController.onPageLoad.url
          )
        }
      }

      "should render page not found if claimingDonationsNotFromCommunityBuilding is empty (None)" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimGiftAidSmallDonationsSchemeController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.PageNotFoundController.onPageLoad.url
          )
        }
      }
    }

    "onSubmit" - {
      "should redirect to the next page (ClaimingCommunityBuildingDonations) when the value is true" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimGiftAidSmallDonationsSchemeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimingCommunityBuildingDonationsController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to the next page (ClaimingCommunityBuildingDonations) when the value is false" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimGiftAidSmallDonationsSchemeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimingCommunityBuildingDonationsController.onPageLoad(NormalMode).url
          )
        }
      }

      "should reload the page with errors when a required field is missing" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimGiftAidSmallDonationsSchemeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }
    }
  }
}

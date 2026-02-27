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
import models.SessionData.and
import views.html.ClaimGiftAidSmallDonationsSchemeView

class ClaimGiftAidSmallDonationsSchemeControllerSpec extends ControllerSpec {
  private val form: Form[Boolean] = new YesNoFormProvider()("claimGASDS.error.required")

  "ClaimGiftAidSmallDonationsSchemeController" - {
    "onPageLoad" - {
      "should render the page correctly when claimingUnderGiftAidSmallDonationsScheme is true" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimGiftAidSmallDonationsSchemeController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimGiftAidSmallDonationsSchemeView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, NormalMode).body
        }
      }

      "should render ClaimsTaskListController if claimingDonationsNotFromCommunityBuilding is false" in {
        val sessionData = RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(false)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimGiftAidSmallDonationsSchemeController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should render ClaimsTaskListController if sessionData is empty" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimGiftAidSmallDonationsSchemeController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }
    }

    "onSubmit" - {

      // Normal Mode Tests:

      "normalMode: should redirect to ClaimingCommunityBuildingDonationsController when the value is true" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

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

      "normalMode: should redirect to ClaimingCommunityBuildingDonationsController when the value is false" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

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

      "normalMode: should reload the page with errors when a required field is missing" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimGiftAidSmallDonationsSchemeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      "normalMode: should NOT show WRN3 confirmation when submitting in NormalMode" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(true, None))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

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

      // Check Mode Tests:

      "checkMode: should redirect to the next page when the value is true" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimGiftAidSmallDonationsSchemeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimingCommunityBuildingDonationsController.onPageLoad(CheckMode).url
          )
        }
      }

      "checkMode: should redirect to the next page when the value is false" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimGiftAidSmallDonationsSchemeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimingCommunityBuildingDonationsController.onPageLoad(CheckMode).url
          )
        }
      }

      "checkMode: should redirect to the next page when the value is changed to true from false" in {
        val sessionData                = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(false))
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimGiftAidSmallDonationsSchemeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimingCommunityBuildingDonationsController.onPageLoad(CheckMode).url
          )
        }
      }

      "checkMode: should redirect to the CYA page when the value is not changed from false" in {
        val sessionData                = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(false))
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimGiftAidSmallDonationsSchemeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "checkMode: should show WRN3 confirmation when the value is changed from true to false" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(true))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimGiftAidSmallDonationsSchemeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include("Do you want to update this repayment claim?")
        }
      }

      "checkMode: should redirect to the CYA page when the value is not changed from true" in {
        val sessionData                = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(true))
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimGiftAidSmallDonationsSchemeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "checkMode: should show WRN3 confirmation view when changing Yes to No in CheckMode" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(true, None))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimGiftAidSmallDonationsSchemeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include("Do you want to update this repayment claim?")
          contentAsString(result) should include("confirmingUpdate")
        }
      }

      // WRN3 Confirmation Screen Tests:

      "WRN3: should NOT show confirmation when answer unchanged in CheckMode" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(false))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimGiftAidSmallDonationsSchemeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "WRN3: should NOT show confirmation when changing No to Yes in CheckMode" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(false))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimGiftAidSmallDonationsSchemeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimingCommunityBuildingDonationsController.onPageLoad(CheckMode).url
          )
        }
      }

      "WRN3: should redirect to CYA without saving when user selects No on WRN3" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(true))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimGiftAidSmallDonationsSchemeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody(
                "confirmingUpdate" -> "true",
                "value"            -> "false"
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "WRN3: should show errors when no radio selected on confirmation screen" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(true))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimGiftAidSmallDonationsSchemeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody(
                "confirmingUpdate" -> "true"
              )

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
          contentAsString(result) should include("Do you want to update this repayment claim?")
          contentAsString(result) should include("Select \u2018Yes\u2019 if you want to update this repayment claim")
        }
      }

      "should redirect to ClaimsTaskListController when claimingUnderGiftAidSmallDonationsScheme is false" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(false)
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimGiftAidSmallDonationsSchemeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should redirect to ClaimsTaskListController when claimingUnderGiftAidSmallDonationsScheme is None" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimGiftAidSmallDonationsSchemeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "true")

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

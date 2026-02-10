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
import views.html.ClaimingCommunityBuildingDonationsView

class ClaimingCommunityBuildingDonationsControllerSpec extends ControllerSpec {
  private val form: Form[Boolean] = new YesNoFormProvider()()
  "ClaimingCommunityBuildingDonationsController" - {
    "onPageLoad" - {
      "should render the page correctly setClaimingUnderGiftAidSmallDonationsScheme is true" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingCommunityBuildingDonationsController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimingCommunityBuildingDonationsView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, NormalMode).body
        }
      }

      "should render page not found if setClaimingUnderGiftAidSmallDonationsScheme is false" in {
        val sessionData = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(false)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingCommunityBuildingDonationsController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.PageNotFoundController.onPageLoad.url
          )
        }
      }

      "should render the page and pre-populate correctly with true value when setClaimingUnderGiftAidSmallDonationsScheme is true" in {

        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingCommunityBuildingDonationsController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimingCommunityBuildingDonationsView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(true), NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with false value when setClaimingUnderGiftAidSmallDonationsScheme is true" in {

        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(false, None))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimingCommunityBuildingDonationsController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ClaimingCommunityBuildingDonationsView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(false), NormalMode).body
        }
      }
    }

    "onSubmit" - {
      // Normal Mode Tests:

      "normalMode: should redirect to ChangePreviousGASDSClaimController when the value is true in normalMode" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ChangePreviousGASDSClaimController.onPageLoad(NormalMode).url
          )
        }
      }

      "normalMode: should skip ChangePreviousGASDSClaim and go to ConnectedToAnyOtherCharities when false and GASDS question is false" in {
        val sessionData                = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(false))
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ConnectedToAnyOtherCharitiesController.onPageLoad(NormalMode).url
          )
        }
      }

      "normalMode: should redirect to ChangePreviousGASDSClaimController when the value is false but claimingDonationsNotFromCommunityBuilding is true" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(true))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ChangePreviousGASDSClaimController.onPageLoad(NormalMode).url
          )
        }
      }

      "normalMode: should reload the page with errors when a required field is missing" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      "normalMode: should NOT show WRN3 confirmation when submitting in NormalMode" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(false))
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, Some(false)))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ConnectedToAnyOtherCharitiesController.onPageLoad(NormalMode).url
          )
        }
      }

      // Check Mode Tests:

      "checkMode: should redirect to the next page when the value is true" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ChangePreviousGASDSClaimController.onPageLoad(CheckMode).url
          )
        }
      }

      "checkMode: should redirect to the next page (connected charities) when the value is false" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(false))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ConnectedToAnyOtherCharitiesController.onPageLoad(CheckMode).url
          )
        }
      }

      "checkMode: should redirect to the next page (change community building donation) when the value is false but claimingDonationsNotFromCommunityBuilding is true" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(true))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ChangePreviousGASDSClaimController.onPageLoad(CheckMode).url
          )
        }
      }

      "checkMode: should redirect to the next page when the value is changed to true from false" in {
        val sessionDataGasds = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        val sessionData      =
          RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(false, None)(using
            sessionDataGasds
          )

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ChangePreviousGASDSClaimController.onPageLoad(CheckMode).url
          )
        }
      }

      "checkMode: should redirect to the next page when the value is not changed from false" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(false, None))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(CheckMode).url)
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
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include("Do you want to update this repayment claim?")
        }
      }

      "checkMode: should redirect to the next page when the value is not changed from true and next screen (R1.4) is is not defined" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ChangePreviousGASDSClaimController.onPageLoad(CheckMode).url
          )
        }
      }
      "checkMode: should redirect to back to CYA when the value is not changed from false and next screen is defined" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(false))
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(false, None))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "checkMode: should redirect to back to CYA when the value is not changed from false and next screen is not defined" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(false, None))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "checkMode: should redirect to back to CYA when the value is not changed from true and next screen is defined" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(true))
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(CheckMode).url)
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
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(CheckMode).url)
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
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(false, None))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(CheckMode).url)
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
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(false, None))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ChangePreviousGASDSClaimController.onPageLoad(CheckMode).url
          )
        }
      }

      "WRN3: should save false to original question and redirect when user confirms Yes on WRN3" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody(
                "confirmingUpdate" -> "true",
                "value"            -> "true"
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result).isDefined shouldBe true
        }
      }

      "WRN3: should redirect to CYA without saving when user selects No on WRN3" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(CheckMode).url)
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
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody(
                "confirmingUpdate" -> "true"
              )

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
          contentAsString(result) should include("Do you want to update this repayment claim?")
          contentAsString(result) should include("Select \u2018Yes\u2019 if you want to update this repayment claim")
        }
      }

      // Navigation Tests:

      "CheckMode: should redirect to CYA when value unchanged (false) and all downstream questions answered" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setConnectedToAnyOtherCharities(true))
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(false))
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(false, Some(false)))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "should redirect to page not found when claimingUnderGiftAidSmallDonationsScheme is false" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(false)
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.PageNotFoundController.onPageLoad.url
          )
        }
      }

      "should redirect to page not found when claimingUnderGiftAidSmallDonationsScheme is None" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.PageNotFoundController.onPageLoad.url
          )
        }
      }

      "CheckMode: should redirect to ConnectedToAnyOtherCharities when value unchanged (false) and question not changed" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingUnderGiftAidSmallDonationsScheme(true)
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(false))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ConnectedToAnyOtherCharitiesController.onPageLoad(CheckMode).url
          )
        }
      }
    }
  }
}

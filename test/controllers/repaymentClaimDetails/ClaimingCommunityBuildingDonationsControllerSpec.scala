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

package controllers.repaymentClaimDetails

import play.api.test.FakeRequest
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import controllers.ControllerSpec
import play.api.Application
import forms.YesNoFormProvider
import play.api.data.Form
import models.Mode.*
import models.RepaymentClaimDetailsAnswers
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

        val sessionDataUnderGASDS = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        val sessionData           = RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None)(
          using sessionDataUnderGASDS
        )

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

        val sessionDataUnderGASDS = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        val sessionData           = RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(false, None)(
          using sessionDataUnderGASDS
        )

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
      "should redirect to the next page (change community building donation) when the value is true" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

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

      "should redirect to the next page (connected charities) when the value is false" in {
        val sessionData                =
          RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(false)
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

      "should redirect to the next page (change community building donation) when the value is false but claimingDonationsNotFromCommunityBuilding is true" in {
        val sessionData =
          RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(true)

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

      "checkmode: should redirect to the next page when the value is true" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

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

      "checkmode: should redirect to the next page (connected charities) when the value is false" in {
        val sessionData =
          RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(false)

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

      "checkmode:should redirect to the next page (change community building donation) when the value is false but claimingDonationsNotFromCommunityBuilding is true" in {
        val sessionData =
          RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(true)

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

      "Checkmode: should redirect to the next page when the value is changed to false from true" in {
        val sessionDataClaimDonations  =
          RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(false)
        val sessionData                =
          RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, Some(false))(using
            sessionDataClaimDonations
          )
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

      "Checkmode: should redirect to the next page when the value is unchanged and false" in {

        val sessionDataConnectedToCharity = RepaymentClaimDetailsAnswers.setConnectedToAnyOtherCharities(true)
        val sessionDataNotFromCommunity   =
          RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(false)(using
            sessionDataConnectedToCharity
          )

        val claimingDonationsNotFromCommunityBuildingAnswer = false
        val sessionData                                     =
          RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(
            false,
            Some(claimingDonationsNotFromCommunityBuildingAnswer)
          )(using
            sessionDataNotFromCommunity
          )

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

      "Checkmode: should redirect to the next page (connectedToAnyOtherCharities) when the value is unchanged and false & claimingDonationsNotFromCommunityBuilding =false " in {
        val sessionDataClaimDonations =
          RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(false)
        val sessionData               =
          RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(false, Some(true))(using
            sessionDataClaimDonations
          )

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

      "Checkmode: should redirect to the next page when the value is changed to true from false" in {
        val sessionData = RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(false, None)

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

      "Checkmode: should redirect to the next page when the value is not changed from false" in {
        val sessionData = RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(false, None)

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

      "Checkmode: should redirect to the next page when the value is not changed from false and claimingDonationsCollectedInCommunityBuildings=true" in {
        val sessionData =
          RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, Some(false))

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

      "Checkmode: should redirect to the next page when the value is not changed from true" in {
        val sessionData = RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None)

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

      "should reload the page with errors when a required field is missing" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ClaimingCommunityBuildingDonationsController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      // TODO: Add WRN3 confirmation tests:
      // - should show WRN3 confirmation when changing Yes to No in CheckMode
      // - should NOT show WRN3 when submitting in NormalMode
      // - should NOT show WRN3 when answer made no change in CheckMode
      // - should NOT show WRN3 when changing No to Yes in CheckMode
      // - should save false and redirect when user confirms Yes on WRN3
      // - should redirect to CYA without saving when user selects No on WRN3
      // - should show WRN3 with errors when no radio selected on confirmation
      // - needs to have updated sessionData if user confirmed yes adn continue on WRN3
      // - check all redirects all work in all tests after navigation all confirmed correct
    }
  }
}

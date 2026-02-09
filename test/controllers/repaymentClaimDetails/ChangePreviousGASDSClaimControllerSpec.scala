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

import controllers.ControllerSpec
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import models.RepaymentClaimDetailsAnswers
import forms.YesNoFormProvider
import models.Mode.*
import play.api.data.Form
import views.html.ChangePreviousGASDSClaimView

class ChangePreviousGASDSClaimControllerSpec extends ControllerSpec {
  private val form: Form[Boolean] = new YesNoFormProvider()()
  "ChangePreviousGASDSClaimController" - {
    "onPageLoad" - {
      "should render the page correctly when setClaimingUnderGiftAidSmallDonationsScheme & setClaimingDonationsCollectedInCommunityBuildings are true" in {
        val sessionDataUnderGASDS = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        val sessionData           = RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None)(
          using sessionDataUnderGASDS
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ChangePreviousGASDSClaimController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ChangePreviousGASDSClaimView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, NormalMode).body
        }
      }

      "should render the page correctly when setClaimingUnderGiftAidSmallDonationsScheme & setClaimingDonationsNotFromCommunityBuilding are true" in {
        val sessionDataUnderGASDS = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        val sessionData           =
          RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(true, None)(using
            sessionDataUnderGASDS
          )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ChangePreviousGASDSClaimController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ChangePreviousGASDSClaimView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with true value" in {
        val sessionDataUnderGASDS    = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        val sessionDataWithDonations =
          RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None)(using
            sessionDataUnderGASDS
          )
        val sessionData              = RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(true)(using
          sessionDataWithDonations
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ChangePreviousGASDSClaimController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ChangePreviousGASDSClaimView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(true), NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with false value" in {

        val sessionDataUnderGASDS    = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        val sessionDataWithDonations =
          RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None)(using
            sessionDataUnderGASDS
          )
        val sessionData              = RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(false)(using
          sessionDataWithDonations
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ChangePreviousGASDSClaimController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ChangePreviousGASDSClaimView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(false), NormalMode).body
        }
      }

      "should render page not found if setClaimingUnderGiftAidSmallDonationsScheme is false" in {
        val sessionData = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(false)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ChangePreviousGASDSClaimController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.PageNotFoundController.onPageLoad.url)
        }
      }
    }

    "onSubmit" - {
      // Normal Mode Tests:

      "normalMode: should redirect to ConnectedToAnyOtherCharitiesController when the value is true" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ChangePreviousGASDSClaimController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ConnectedToAnyOtherCharitiesController.onPageLoad(NormalMode).url
          )
        }
      }

      "normalMode: should redirect to ConnectedToAnyOtherCharitiesController when the value is false" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ChangePreviousGASDSClaimController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ConnectedToAnyOtherCharitiesController.onPageLoad(NormalMode).url
          )
        }
      }

      "normalMode: should reload the page with errors when a required field is missing" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ChangePreviousGASDSClaimController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      "normalMode: should NOT show WRN3 confirmation when submitting in NormalMode" in {
        val sessionDataUnderGASDS    = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        val sessionDataWithDonations =
          RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None)(using
            sessionDataUnderGASDS
          )
        val sessionData              = RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(true)(using
          sessionDataWithDonations
        )

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ChangePreviousGASDSClaimController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ConnectedToAnyOtherCharitiesController.onPageLoad(NormalMode).url
          )
        }
      }

      // Check Mode Tests:

      "checkMode: should redirect to ConnectedToAnyOtherCharitiesController when the value is true" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ChangePreviousGASDSClaimController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ConnectedToAnyOtherCharitiesController.onPageLoad(CheckMode).url
          )
        }
      }

      "checkMode: should redirect to ConnectedToAnyOtherCharitiesController when the value is false" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ChangePreviousGASDSClaimController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ConnectedToAnyOtherCharitiesController.onPageLoad(CheckMode).url
          )
        }
      }

      "checkMode: should redirect to ConnectedToAnyOtherCharitiesController when the value is changed to true from false" in {
        val sessionDataUnderGASDS    = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        val sessionDataWithDonations =
          RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None)(using
            sessionDataUnderGASDS
          )
        val sessionData              = RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(false)(using
          sessionDataWithDonations
        )

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ChangePreviousGASDSClaimController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ConnectedToAnyOtherCharitiesController.onPageLoad(CheckMode).url
          )
        }
      }

      "checkMode: should redirect to CYA when the value is not changed from false" in {
        val sessionDataUnderGASDS    = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        val sessionDataWithDonations =
          RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None)(using
            sessionDataUnderGASDS
          )
        val sessionData              = RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(false)(using
          sessionDataWithDonations
        )

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ChangePreviousGASDSClaimController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "checkMode: should show WRN3 confirmation when the value is changed from true to false" in {
        val sessionDataUnderGASDS    = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        val sessionDataWithDonations =
          RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None)(using
            sessionDataUnderGASDS
          )
        val sessionData              = RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(true)(using
          sessionDataWithDonations
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ChangePreviousGASDSClaimController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include("Do you want to update this repayment claim?")
        }
      }

      "checkMode: should redirect to CYA when the value is not changed from true" in {
        val sessionDataUnderGASDS    = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        val sessionDataWithDonations =
          RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None)(using
            sessionDataUnderGASDS
          )
        val sessionData              = RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(true)(using
          sessionDataWithDonations
        )

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ChangePreviousGASDSClaimController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "checkMode: should show WRN3 confirmation view when changing Yes to No in CheckMode" in {
        val sessionDataUnderGASDS    = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        val sessionDataWithDonations =
          RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None)(using
            sessionDataUnderGASDS
          )
        val sessionData              = RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(true)(using
          sessionDataWithDonations
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ChangePreviousGASDSClaimController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include("Do you want to update this repayment claim?")
          contentAsString(result) should include("confirmingUpdate")
        }
      }

      // WRN3 Confirmation Screen Tests:

      "WRN3: should NOT show confirmation when answer unchanged (false to false) in CheckMode" in {
        val sessionDataUnderGASDS    = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        val sessionDataWithDonations =
          RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None)(using
            sessionDataUnderGASDS
          )
        val sessionData              = RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(false)(using
          sessionDataWithDonations
        )

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ChangePreviousGASDSClaimController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "WRN3: should NOT show confirmation when changing No to Yes in CheckMode" in {
        val sessionDataUnderGASDS    = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        val sessionDataWithDonations =
          RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None)(using
            sessionDataUnderGASDS
          )
        val sessionData              = RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(false)(using
          sessionDataWithDonations
        )

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ChangePreviousGASDSClaimController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ConnectedToAnyOtherCharitiesController.onPageLoad(CheckMode).url
          )
        }
      }

      "WRN3: should redirect to CYA without saving when user selects No on WRN3" in {
        val sessionDataUnderGASDS    = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        val sessionDataWithDonations =
          RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None)(using
            sessionDataUnderGASDS
          )
        val sessionData              = RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(true)(using
          sessionDataWithDonations
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ChangePreviousGASDSClaimController.onSubmit(CheckMode).url)
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

      "WRN3: should save and redirect to next page when user selects Yes on WRN3" in {
        val sessionDataUnderGASDS    = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        val sessionDataWithDonations =
          RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None)(using
            sessionDataUnderGASDS
          )
        val sessionData              = RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(true)(using
          sessionDataWithDonations
        )

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ChangePreviousGASDSClaimController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody(
                "confirmingUpdate" -> "true",
                "value"            -> "true"
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ConnectedToAnyOtherCharitiesController.onPageLoad(CheckMode).url
          )
        }
      }

      "WRN3: should show errors when no radio selected on confirmation screen" in {
        val sessionDataUnderGASDS    = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        val sessionDataWithDonations =
          RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None)(using
            sessionDataUnderGASDS
          )
        val sessionData              = RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(true)(using
          sessionDataWithDonations
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ChangePreviousGASDSClaimController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody(
                "confirmingUpdate" -> "true"
              )

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
          contentAsString(result) should include("Do you want to update this repayment claim?")
          contentAsString(result) should include("Select ‘Yes’ if you want to update this repayment claim")
        }
      }
    }
  }
}

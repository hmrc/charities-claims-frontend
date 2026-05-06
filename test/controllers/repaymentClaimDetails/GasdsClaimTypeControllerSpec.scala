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
import forms.{GasdsClaimTypeFormProvider, YesNoFormProvider}
import models.Mode.*
import models.{GasdsClaimType, RepaymentClaimDetailsAnswers}
import play.api.Application
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import views.html.{GasdsClaimTypeView, UpdateRepaymentClaimView}
import uk.gov.hmrc.auth.core.AffinityGroup

class GasdsClaimTypeControllerSpec extends ControllerSpec {

  private val form: Form[GasdsClaimType] = new GasdsClaimTypeFormProvider()()

  private val baseSessionData =
    RepaymentClaimDetailsAnswers
      .setClaimingUnderGiftAidSmallDonationsScheme(true)

  "GasdsClaimTypeController" - {

    "onPageLoad" - {
      "organisation user" - {
        val isAgent = false

        "should render the page with empty form when no existing data" in {
          given application: Application =
            applicationBuilder(sessionData = baseSessionData).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.GasdsClaimTypeController.onPageLoad(NormalMode).url)

            val result = route(application, request).value
            val view   = application.injector.instanceOf[GasdsClaimTypeView]

            status(result) shouldEqual OK
            contentAsString(result) shouldEqual view(form, NormalMode, false, isAgent).body
          }
        }

        "should pre-populate form when data exists" in {
          val sessionData = baseSessionData.copy(
            repaymentClaimDetailsAnswers = baseSessionData.repaymentClaimDetailsAnswers.map(
              _.copy(
                claimingUnderGiftAidSmallDonationsScheme = Some(true),
                claimingDonationsNotFromCommunityBuilding = Some(false),
                claimingDonationsCollectedInCommunityBuildings = Some(false),
                connectedToAnyOtherCharities = Some(true)
              )
            )
          )

          given application: Application =
            applicationBuilder(sessionData = sessionData).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.GasdsClaimTypeController.onPageLoad(NormalMode).url)

            val result = route(application, request).value
            val view   = application.injector.instanceOf[GasdsClaimTypeView]

            status(result) shouldEqual OK
            contentAsString(result) shouldEqual view(
              form.fill(GasdsClaimType(false, false, true)),
              NormalMode,
              false,
              isAgent
            ).body
          }
        }

        "should remove communityBuildings when it is CASC reference" in {
          val sessionData = baseSessionData.copy(
            repaymentClaimDetailsAnswers = baseSessionData.repaymentClaimDetailsAnswers.map(
              _.copy(
                claimingUnderGiftAidSmallDonationsScheme = Some(true),
                claimingDonationsNotFromCommunityBuilding = Some(false),
                claimingDonationsCollectedInCommunityBuildings = Some(true),
                connectedToAnyOtherCharities = Some(false)
              )
            ),
            charitiesReference = "CH-123"
          )

          given application: Application =
            applicationBuilder(sessionData = sessionData).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.GasdsClaimTypeController.onPageLoad(NormalMode).url)

            val result = route(application, request).value
            val view   = application.injector.instanceOf[GasdsClaimTypeView]

            status(result) shouldEqual OK

            contentAsString(result) shouldEqual view(
              form.fill(GasdsClaimType(false, false, false)),
              NormalMode,
              true,
              isAgent
            ).body
          }
        }
      }
      "Agent user" - {
        val isAgent = true

        "should render the page with empty form when no existing data" in {
          given application: Application =
            applicationBuilder(sessionData = baseSessionData, AffinityGroup.Agent).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.GasdsClaimTypeController.onPageLoad(NormalMode).url)

            val result = route(application, request).value
            val view   = application.injector.instanceOf[GasdsClaimTypeView]

            status(result) shouldEqual OK
            contentAsString(result) shouldEqual view(form, NormalMode, false, isAgent).body
          }
        }

        "should pre-populate form when data exists" in {
          val sessionData = baseSessionData.copy(
            repaymentClaimDetailsAnswers = baseSessionData.repaymentClaimDetailsAnswers.map(
              _.copy(
                claimingUnderGiftAidSmallDonationsScheme = Some(true),
                claimingDonationsNotFromCommunityBuilding = Some(false),
                claimingDonationsCollectedInCommunityBuildings = Some(false),
                connectedToAnyOtherCharities = Some(true)
              )
            )
          )

          given application: Application =
            applicationBuilder(sessionData = sessionData, AffinityGroup.Agent).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.GasdsClaimTypeController.onPageLoad(NormalMode).url)

            val result = route(application, request).value
            val view   = application.injector.instanceOf[GasdsClaimTypeView]

            status(result) shouldEqual OK
            contentAsString(result) shouldEqual view(
              form.fill(GasdsClaimType(false, false, true)),
              NormalMode,
              false,
              isAgent
            ).body
          }
        }

        "should remove communityBuildings when it is CASC reference" in {
          val sessionData = baseSessionData.copy(
            repaymentClaimDetailsAnswers = baseSessionData.repaymentClaimDetailsAnswers.map(
              _.copy(
                claimingUnderGiftAidSmallDonationsScheme = Some(true),
                claimingDonationsNotFromCommunityBuilding = Some(false),
                claimingDonationsCollectedInCommunityBuildings = Some(true),
                connectedToAnyOtherCharities = Some(false)
              )
            ),
            charitiesReference = "CH-123"
          )

          given application: Application =
            applicationBuilder(sessionData = sessionData, AffinityGroup.Agent).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.GasdsClaimTypeController.onPageLoad(NormalMode).url)

            val result = route(application, request).value
            val view   = application.injector.instanceOf[GasdsClaimTypeView]

            status(result) shouldEqual OK

            contentAsString(result) shouldEqual view(
              form.fill(GasdsClaimType(false, false, false)),
              NormalMode,
              true,
              isAgent
            ).body
          }
        }
      }
    }

    "onSubmit" - {

      "should return BAD_REQUEST when form is invalid" in {
        given application: Application =
          applicationBuilder(sessionData = baseSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.GasdsClaimTypeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody()

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      "should redirect to ClaimingReferenceNumberController when only connectedCharity selected in NormalMode" in {
        given application: Application =
          applicationBuilder(sessionData = baseSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.GasdsClaimTypeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value[]" -> "connectedCharity")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimingReferenceNumberController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to ChangePreviousGASDSClaimController when topUp selected in NormalMode" in {
        given application: Application =
          applicationBuilder(sessionData = baseSessionData).build()

        running(application) {
          val request =
            FakeRequest(POST, routes.GasdsClaimTypeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value[]" -> "topUp")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ChangePreviousGASDSClaimController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to ChangePreviousGASDSClaimController when communityBuildings selected in NormalMode" in {
        given application: Application =
          applicationBuilder(sessionData = baseSessionData).build()

        running(application) {
          val request =
            FakeRequest(POST, routes.GasdsClaimTypeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value[]" -> "communityBuildings")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ChangePreviousGASDSClaimController.onPageLoad(NormalMode).url
          )
        }
      }

      "should render confirmation view when removing an option in CheckMode" in {
        val sessionData = baseSessionData.copy(
          repaymentClaimDetailsAnswers = baseSessionData.repaymentClaimDetailsAnswers.map(
            _.copy(
              claimingDonationsNotFromCommunityBuilding = Some(true)
            )
          )
        )

        given application: Application =
          applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.GasdsClaimTypeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value[]" -> "connectedCharity")

          val view              = application.injector.instanceOf[UpdateRepaymentClaimView]
          val yesNoFormProvider = application.injector.instanceOf[YesNoFormProvider]
          val confirmForm       = yesNoFormProvider("updateRepaymentClaim.error.required")

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(
            confirmForm,
            routes.GasdsClaimTypeController.onSubmit(CheckMode),
            Seq("connectedCharity")
          ).body
        }
      }

      "should return BAD_REQUEST and re-render confirmation view when confirmation missing" in {
        given application: Application =
          applicationBuilder(sessionData = baseSessionData).build()

        running(application) {
          val view              = application.injector.instanceOf[UpdateRepaymentClaimView]
          val yesNoFormProvider = application.injector.instanceOf[YesNoFormProvider]

          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.GasdsClaimTypeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody(
                "confirmingUpdate" -> "true",
                "value[]"          -> "topUp"
              )

          val result = route(application, request).value

          val expectedForm =
            yesNoFormProvider("updateRepaymentClaim.error.required").bind(Map("confirmingUpdate" -> "true"))

          status(result) shouldEqual BAD_REQUEST

          contentAsString(result) shouldEqual view(
            expectedForm,
            routes.GasdsClaimTypeController.onSubmit(CheckMode),
            Seq("topUp")
          ).body
        }
      }

      "should redirect to CYA when confirmation = NO" in {
        given application: Application =
          applicationBuilder(sessionData = baseSessionData).build()

        running(application) {
          val request =
            FakeRequest(POST, routes.GasdsClaimTypeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody(
                "confirmingUpdate" -> "true",
                "value[]"          -> "topUp",
                "value"            -> "false"
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "should save and redirect when confirmation = YES" in {
        given application: Application =
          applicationBuilder(sessionData = baseSessionData).build()

        running(application) {
          val request =
            FakeRequest(POST, routes.GasdsClaimTypeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody(
                "confirmingUpdate" -> "true",
                "value[]"          -> "topUp",
                "value"            -> "true"
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ChangePreviousGASDSClaimController.onPageLoad(CheckMode).url
          )
        }
      }
    }
  }
}

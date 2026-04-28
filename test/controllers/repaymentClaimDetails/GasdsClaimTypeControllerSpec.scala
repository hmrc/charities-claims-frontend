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
import forms.GasdsClaimTypeFormProvider
import models.Mode.*
import models.{GasdsClaimType, RepaymentClaimDetailsAnswers}
import play.api.Application
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import views.html.GasdsClaimTypeView

class GasdsClaimTypeControllerSpec extends ControllerSpec {

  private val form: Form[GasdsClaimType] =
    new GasdsClaimTypeFormProvider()()

  "GasdsClaimTypeController" - {

    "onPageLoad" - {

      "should render the page with empty form when no existing data" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.GasdsClaimTypeController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[GasdsClaimTypeView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, NormalMode, false).body
        }
      }

      "should pre-populate form when data exists" in {
        val sessionData =
          RepaymentClaimDetailsAnswers.setGasdsClaimType(
            GasdsClaimType(topUp = false, communityBuildings = false, connectedCharity = true)
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
            form.fill(GasdsClaimType(topUp = false, communityBuildings = false, connectedCharity = true)),
            NormalMode,
            false
          ).body
        }
      }

      "should remove communityBuildings when CASC reference" in {
        val sessionData =
          RepaymentClaimDetailsAnswers.setGasdsClaimType(
            GasdsClaimType(topUp = false, communityBuildings = true, connectedCharity = false)
          )

        given application: Application =
          applicationBuilder(sessionData = sessionData.copy(charitiesReference = "CH-123")).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.GasdsClaimTypeController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[GasdsClaimTypeView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(
            form.fill(GasdsClaimType(false, false, false)),
            NormalMode,
            true
          ).body
        }
      }
    }

    "onSubmit" - {

      "should return BAD_REQUEST when form is invalid" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.GasdsClaimTypeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      "should redirect to ClaimingReferenceNumberController when connectedCharity = true in NormalMode" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.GasdsClaimTypeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody(
                "value[2]" -> "connectedCharity"
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimingReferenceNumberController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to ChangePreviousGASDSClaimController when top up payment or community building is true in NormalMode" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.GasdsClaimTypeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value[0]" -> "topUp")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ChangePreviousGASDSClaimController.onPageLoad(NormalMode).url
          )
        }
      }

      "should always redirect to CYA in CheckMode" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.GasdsClaimTypeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value[1]" -> "communityBuildings")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }
    }
  }
}

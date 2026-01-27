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

import controllers.ControllerSpec
import forms.YesNoFormProvider
import models.RepaymentClaimDetailsAnswers
import play.api.Application
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import models.Mode.*
import views.html.ConnectedToAnyOtherCharitiesView

class ConnectedToAnyOtherCharitiesControllerSpec extends ControllerSpec {
  private val form: Form[Boolean] = new YesNoFormProvider()()
  "ConnectedToAnyOtherCharitiesController" - {
    "onPageLoad" - {
      "should render the page correctly setClaimingUnderGiftAidSmallDonationsScheme is true" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ConnectedToAnyOtherCharitiesController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ConnectedToAnyOtherCharitiesView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, NormalMode).body
        }
      }

      "should render page not found if setClaimingUnderGiftAidSmallDonationsScheme is false" in {
        val sessionData = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(false)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ConnectedToAnyOtherCharitiesController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.PageNotFoundController.onPageLoad.url)
        }
      }

      "should render the page and pre-populate correctly with true value when setClaimingUnderGiftAidSmallDonationsScheme is true" in {

        val sessionDataUnderGASDS = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        val sessionData           = RepaymentClaimDetailsAnswers.setConnectedToAnyOtherCharities(true)(using
          sessionDataUnderGASDS
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ConnectedToAnyOtherCharitiesController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ConnectedToAnyOtherCharitiesView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(true), NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with false value when setClaimingUnderGiftAidSmallDonationsScheme is true" in {

        val sessionDataUnderGASDS = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)
        val sessionData           = RepaymentClaimDetailsAnswers.setConnectedToAnyOtherCharities(false)(using
          sessionDataUnderGASDS
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ConnectedToAnyOtherCharitiesController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[ConnectedToAnyOtherCharitiesView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(false), NormalMode).body
        }
      }
    }

    "onSubmit" - {
      "should redirect to the next page when the value is true" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ConnectedToAnyOtherCharitiesController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ConnectedToAnyOtherCharitiesController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to the cya when the value is true if coming from CYA" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ConnectedToAnyOtherCharitiesController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "should redirect to the cya when the value is false if coming from CYA" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ConnectedToAnyOtherCharitiesController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "should redirect to the next page when the value is false" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ConnectedToAnyOtherCharitiesController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ConnectedToAnyOtherCharitiesController.onPageLoad(NormalMode).url
          )
        }
      }

      "should reload the page with errors when a required field is missing" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.ConnectedToAnyOtherCharitiesController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }
    }
  }
}

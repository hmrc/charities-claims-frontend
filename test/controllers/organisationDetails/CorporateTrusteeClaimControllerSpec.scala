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

package controllers.organisationDetails

import play.api.test.FakeRequest
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import controllers.ControllerSpec
import views.html.CorporateTrusteeClaimView
import play.api.Application
import forms.YesNoFormProvider
import models.OrganisationDetailsAnswers
import play.api.data.Form
import models.Mode.*

class CorporateTrusteeClaimControllerSpec extends ControllerSpec {
  private val form: Form[Boolean] = new YesNoFormProvider()()
  "CorporateTrusteeClaimController" - {
    "onPageLoad" - {
      "should render the page correctly" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CorporateTrusteeClaimController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[CorporateTrusteeClaimView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with true value" in {

        val sessionData = OrganisationDetailsAnswers.setAreYouACorporateTrustee(true)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CorporateTrusteeClaimController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[CorporateTrusteeClaimView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(true), NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with false value" in {

        val sessionData = OrganisationDetailsAnswers.setAreYouACorporateTrustee(false)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CorporateTrusteeClaimController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[CorporateTrusteeClaimView]

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
            FakeRequest(POST, routes.CorporateTrusteeClaimController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.CorporateTrusteeAddressController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to the next page when the value is false" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CorporateTrusteeClaimController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.AuthorisedOfficialAddressController.onPageLoad(NormalMode).url
          )
        }
      }

      "in CheckMode when changing from Yes to No" - {
        "should redirect to AuthorisedOfficialAddressController" in {
          // Previous answer was true (Yes), now changing to false (No)
          val sessionData = OrganisationDetailsAnswers.setAreYouACorporateTrustee(true)

          given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.CorporateTrusteeClaimController.onSubmit(CheckMode).url)
                .withFormUrlEncodedBody("value" -> "false")

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              routes.AuthorisedOfficialAddressController.onPageLoad(CheckMode).url
            )
          }
        }
      }

      "in CheckMode when changing from No to Yes" - {
        "should redirect to CorporateTrusteeAddressController" in {
          // Previous answer was false (No), now changing to true (Yes)
          val sessionData = OrganisationDetailsAnswers.setAreYouACorporateTrustee(false)

          given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.CorporateTrusteeClaimController.onSubmit(CheckMode).url)
                .withFormUrlEncodedBody("value" -> "true")

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              routes.CorporateTrusteeAddressController.onPageLoad(CheckMode).url
            )
          }
        }
      }

      "in CheckMode when answer is unchanged" - {
        "should redirect to CYA when value remains true" in {
          val sessionData = OrganisationDetailsAnswers.setAreYouACorporateTrustee(true)

          given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.CorporateTrusteeClaimController.onSubmit(CheckMode).url)
                .withFormUrlEncodedBody("value" -> "true")

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url
            )
          }
        }

        "should redirect to CYA when value remains false" in {
          val sessionData = OrganisationDetailsAnswers.setAreYouACorporateTrustee(false)

          given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.CorporateTrusteeClaimController.onSubmit(CheckMode).url)
                .withFormUrlEncodedBody("value" -> "false")

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url
            )
          }
        }
      }

      "in CheckMode with no previous answer (invalid state)" - {
        "should redirect to CYA when selecting Yes" in {
          given application: Application = applicationBuilder().mockSaveSession.build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.CorporateTrusteeClaimController.onSubmit(CheckMode).url)
                .withFormUrlEncodedBody("value" -> "true")

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url
            )
          }
        }

        "should redirect to CYA when selecting No" in {
          given application: Application = applicationBuilder().mockSaveSession.build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.CorporateTrusteeClaimController.onSubmit(CheckMode).url)
                .withFormUrlEncodedBody("value" -> "false")

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url
            )
          }
        }
      }

      "should reload the page with errors when a required field is missing" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CorporateTrusteeClaimController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }
    }
  }
}

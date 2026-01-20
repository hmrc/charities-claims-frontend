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
import views.html.AuthorisedOfficialAddressView
import play.api.Application
import forms.YesNoFormProvider
import models.OrganisationDetailsAnswers
import play.api.data.Form
import models.Mode.*

class AuthorisedOfficialAddressControllerSpec extends ControllerSpec {
  private val form: Form[Boolean] = new YesNoFormProvider()()
  "AuthorisedOfficialAddressController" - {
    "onPageLoad" - {
      "should render the page correctly if Corporate trustee is false" in {
        val sessionData                = OrganisationDetailsAnswers.setAreYouACorporateTrustee(false)
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AuthorisedOfficialAddressController.onPageLoad(NormalMode).url)
          val result                                         = route(application, request).value
          val view                                           = application.injector.instanceOf[AuthorisedOfficialAddressView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with true for UK Address if Corporate trustee is false" in {
        val sessionDataAreYouCorporateTrustee                = OrganisationDetailsAnswers.setAreYouACorporateTrustee(false)
        val sessionDataAreYouAuthorisedOfficialWithUKAddress =
          OrganisationDetailsAnswers.setDoYouHaveAuthorisedOfficialTrusteeUKAddress(true)(using
            sessionDataAreYouCorporateTrustee
          )

        given application: Application =
          applicationBuilder(sessionData = sessionDataAreYouAuthorisedOfficialWithUKAddress).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AuthorisedOfficialAddressController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[AuthorisedOfficialAddressView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(true), NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with false for UK Address if Corporate trustee is false" in {
        val sessionDataAreYouCorporateTrustee                = OrganisationDetailsAnswers.setAreYouACorporateTrustee(false)
        val sessionDataAreYouAuthorisedOfficialWithUKAddress =
          OrganisationDetailsAnswers.setDoYouHaveAuthorisedOfficialTrusteeUKAddress(false)(using
            sessionDataAreYouCorporateTrustee
          )

        given application: Application =
          applicationBuilder(sessionData = sessionDataAreYouAuthorisedOfficialWithUKAddress).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AuthorisedOfficialAddressController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[AuthorisedOfficialAddressView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(false), NormalMode).body
        }
      }
      "should render page not found if Corporate trustee is true" in {
        val sessionData = OrganisationDetailsAnswers.setAreYouACorporateTrustee(true)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AuthorisedOfficialAddressController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.PageNotFoundController.onPageLoad.url)
        }
      }
    }

    "onSubmit" - {
      "should redirect to the next page when the value is true" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.AuthorisedOfficialAddressController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.AuthorisedOfficialDetailsController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to the next page when the value is false" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.AuthorisedOfficialAddressController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.AuthorisedOfficialDetailsController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to back to authorised official details when the value is true" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.AuthorisedOfficialAddressController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "should redirect to back to CYA when the value is false" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.AuthorisedOfficialAddressController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "should reload the page with errors when a required field is missing" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.AuthorisedOfficialAddressController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }
    }
  }
}

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

import controllers.ControllerSpec
import forms.AuthorisedOfficialDetailsFormProvider
import models.Mode.NormalMode
import models.{AuthorisedOfficialDetails, OrganisationDetailsAnswers}
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import views.html.AuthorisedOfficialDetailsView

class AuthorisedOfficialDetailsControllerSpec extends ControllerSpec {

  val formProvider = new AuthorisedOfficialDetailsFormProvider()
  val form         = formProvider(isUkAddress = false)

  val validData = AuthorisedOfficialDetails(
    title = Some("Mr"),
    firstName = "John",
    lastName = "Doe",
    phoneNumber = "01234567890",
    postcode = None
  )

  val baseSession = OrganisationDetailsAnswers.setDoYouHaveAuthorisedOfficialTrusteeUKAddress(false)

  "AuthorisedOfficialDetailsController" - {

    "onPageLoad" - {
      "should render the page correctly when UK address is false (default)" in {
        given application: Application = applicationBuilder(sessionData = baseSession).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AuthorisedOfficialDetailsController.onPageLoad(NormalMode).url)

          val result   = route(application, request).value
          val view     = application.injector.instanceOf[AuthorisedOfficialDetailsView]
          val messages = application.injector.instanceOf[MessagesApi].preferred(request)

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, isUkAddress = false, NormalMode)(using request, messages).body
        }
      }

      "should render the page correctly when UK address is true" in {
        val sessionData                = OrganisationDetailsAnswers.setDoYouHaveAuthorisedOfficialTrusteeUKAddress(true)
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AuthorisedOfficialDetailsController.onPageLoad(NormalMode).url)

          val result   = route(application, request).value
          val view     = application.injector.instanceOf[AuthorisedOfficialDetailsView]
          val messages = application.injector.instanceOf[MessagesApi].preferred(request)

          val expectedForm = formProvider(isUkAddress = true)

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(expectedForm, isUkAddress = true, NormalMode)(using
            request,
            messages
          ).body
        }
      }

      "should render the page and pre-populate correctly when data exists" in {
        val sessionData = OrganisationDetailsAnswers.setAuthorisedOfficialDetails(validData)(using baseSession)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AuthorisedOfficialDetailsController.onPageLoad(NormalMode).url)

          val result   = route(application, request).value
          val view     = application.injector.instanceOf[AuthorisedOfficialDetailsView]
          val messages = application.injector.instanceOf[MessagesApi].preferred(request)

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(validData), isUkAddress = false, NormalMode)(using
            request,
            messages
          ).body
        }
      }

      "should redirect to AuthorisedOfficialAddress if UK address answer is missing" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.AuthorisedOfficialDetailsController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.AuthorisedOfficialAddressController.onPageLoad(NormalMode).url
          )
        }
      }
    }

    "onSubmit" - {
      "should redirect to the next page when valid data is submitted (No UK Address)" in {
        given application: Application = applicationBuilder(sessionData = baseSession).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.AuthorisedOfficialDetailsController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody(
                "firstName"   -> "John",
                "lastName"    -> "Doe",
                "phoneNumber" -> "01234567890"
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "should redirect to the next page when valid data is submitted (With UK Address)" in {
        val sessionData                = OrganisationDetailsAnswers.setDoYouHaveAuthorisedOfficialTrusteeUKAddress(true)
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.AuthorisedOfficialDetailsController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody(
                "firstName"   -> "John",
                "lastName"    -> "Doe",
                "phoneNumber" -> "01234567890",
                "postcode"    -> "SW1A 1AA"
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "should return BadRequest when invalid data (empty fields) is submitted" in {
        given application: Application = applicationBuilder(sessionData = baseSession).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.AuthorisedOfficialDetailsController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("firstName" -> "")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      "should return BadRequest when postcode is missing but required (isUkAddress = true)" in {
        val sessionData                = OrganisationDetailsAnswers.setDoYouHaveAuthorisedOfficialTrusteeUKAddress(true)
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.AuthorisedOfficialDetailsController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody(
                "firstName"   -> "John",
                "lastName"    -> "Doe",
                "phoneNumber" -> "01234567890"
              )

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }
    }
  }
}

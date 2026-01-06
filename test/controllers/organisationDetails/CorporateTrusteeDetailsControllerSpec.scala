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
import forms.CorporateTrusteeDetailsFormProvider
import models.{CorporateTrusteeDetails, OrganisationDetailsAnswers}
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import views.html.CorporateTrusteeDetailsView
import models.Mode.*
import play.api.Logger

class CorporateTrusteeDetailsControllerSpec extends ControllerSpec {
  val uKAddressTrue: Boolean  = true
  val uKAddressFalse: Boolean = false
  val formProvider            = new CorporateTrusteeDetailsFormProvider()
  val form                    = formProvider(
    uKAddressFalse,
    "corporateTrusteeDetails.name.error.required",
    "corporateTrusteeDetails.name.error.length",
    "corporateTrusteeDetails.name.error.regex",
    "corporateTrusteeDetails.phone.error.required",
    "corporateTrusteeDetails.phone.error.length",
    "corporateTrusteeDetails.phone.error.regex",
    "corporateTrusteeDetails.postCode.error.required",
    "corporateTrusteeDetails.postCode.error.length",
    "corporateTrusteeDetails.postCode.error.regex"
  )

  val validDataWithOutPostcode = CorporateTrusteeDetails(
    nameOfCorporateTrustee = "Corporate Trustee1",
    corporateTrusteeDaytimeTelephoneNumber = "01234567890"
  )

  val validDataWithPostcode = CorporateTrusteeDetails(
    nameOfCorporateTrustee = "Corporate Trustee1",
    corporateTrusteeDaytimeTelephoneNumber = "01234567890",
    corporateTrusteePostcode = Some("SW1A 1AA")
  )

  "CorporateTrusteeDetailsController" - {

    "onPageLoad" - {
      "should render the page correctly when UK address is false (default)" in {
        val sessionDataAreYouCorporateTrustee = OrganisationDetailsAnswers.setAreYouACorporateTrustee(true)
        val sessionData                       =
          OrganisationDetailsAnswers.setDoYouHaveCorporateTrusteeUKAddress(uKAddressFalse)(using
            sessionDataAreYouCorporateTrustee
          )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        Logger(getClass).warn(s"*** checking test: result corporate trustee Details controller test }")

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CorporateTrusteeDetailsController.onPageLoad(NormalMode).url)

          val result   = route(application, request).value
          val view     = application.injector.instanceOf[CorporateTrusteeDetailsView]
          val messages = application.injector.instanceOf[MessagesApi].preferred(request)
          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, uKAddressFalse, NormalMode)(using request, messages).body
        }
      }

      "should render the page correctly when UK address is true" in {
        val sessionDataAreYouCorporateTrustee = OrganisationDetailsAnswers.setAreYouACorporateTrustee(true)
        val sessionData                       =
          OrganisationDetailsAnswers.setDoYouHaveCorporateTrusteeUKAddress(uKAddressTrue)(using
            sessionDataAreYouCorporateTrustee
          )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CorporateTrusteeDetailsController.onPageLoad(NormalMode).url)

          val result   = route(application, request).value
          val view     = application.injector.instanceOf[CorporateTrusteeDetailsView]
          val messages = application.injector.instanceOf[MessagesApi].preferred(request)

          val expectedForm = formProvider(
            uKAddressTrue,
            "corporateTrusteeDetails.name.error.required",
            "corporateTrusteeDetails.name.error.length",
            "corporateTrusteeDetails.name.error.regex",
            "corporateTrusteeDetails.phone.error.required",
            "corporateTrusteeDetails.phone.error.length",
            "corporateTrusteeDetails.phone.error.regex",
            "corporateTrusteeDetails.postCode.error.required",
            "corporateTrusteeDetails.postCode.error.length",
            "corporateTrusteeDetails.postCode.error.regex"
          )

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(expectedForm, uKAddressTrue, NormalMode)(using
            request,
            messages
          ).body
        }
      }

      "should render the page and pre-populate correctly when data exists for Corporate Trustee and NOT UK address" in {
        val sessionDataAreYouCorporateTrustee        = OrganisationDetailsAnswers.setAreYouACorporateTrustee(true)
        val sessionDataCorporateTrusteeWithUKAddress =
          OrganisationDetailsAnswers.setDoYouHaveCorporateTrusteeUKAddress(uKAddressFalse)(using
            sessionDataAreYouCorporateTrustee
          )
        val sessionData                              = OrganisationDetailsAnswers.setCorporateTrusteeDetails(validDataWithOutPostcode)(using
          sessionDataCorporateTrusteeWithUKAddress
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CorporateTrusteeDetailsController.onPageLoad(NormalMode).url)

          val result   = route(application, request).value
          val view     = application.injector.instanceOf[CorporateTrusteeDetailsView]
          val messages = application.injector.instanceOf[MessagesApi].preferred(request)

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(validDataWithOutPostcode), uKAddressFalse, NormalMode)(
            using
            request,
            messages
          ).body
        }
      }

      "should render the page and pre-populate correctly when data exists for Corporate Trustee and UK address" in {
        val sessionDataAreYouCorporateTrustee        = OrganisationDetailsAnswers.setAreYouACorporateTrustee(true)
        val sessionDataCorporateTrusteeWithUKAddress =
          OrganisationDetailsAnswers.setDoYouHaveCorporateTrusteeUKAddress(uKAddressTrue)(using
            sessionDataAreYouCorporateTrustee
          )
        val sessionData                              = OrganisationDetailsAnswers.setCorporateTrusteeDetails(validDataWithPostcode)(using
          sessionDataCorporateTrusteeWithUKAddress
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CorporateTrusteeDetailsController.onPageLoad(NormalMode).url)

          val result   = route(application, request).value
          val view     = application.injector.instanceOf[CorporateTrusteeDetailsView]
          val messages = application.injector.instanceOf[MessagesApi].preferred(request)

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(validDataWithPostcode), uKAddressTrue, NormalMode)(using
            request,
            messages
          ).body
        }
      }

      "should render page not found if Corporate trustee is false" in {
        val sessionData = OrganisationDetailsAnswers.setAreYouACorporateTrustee(false)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CorporateTrusteeDetailsController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.PageNotFoundController.onPageLoad.url)

        }
      }
    }

    "onSubmit" - {
      "should redirect to the next page when valid data is submitted (without UK Address)" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CorporateTrusteeDetailsController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody(
                "nameOfCorporateTrustee"                 -> "Corporate Trustee1",
                "corporateTrusteeDaytimeTelephoneNumber" -> "01234567890"
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)
        }
      }

      "should redirect to the next page when valid data is submitted (with UK Address)" in {
        val sessionData                = OrganisationDetailsAnswers.setDoYouHaveCorporateTrusteeUKAddress(true)
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CorporateTrusteeDetailsController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody(
                "nameOfCorporateTrustee"                 -> "Corporate Trustee1",
                "corporateTrusteeDaytimeTelephoneNumber" -> "01234567890",
                "corporateTrusteePostcode"               -> "SW1A 1AA"
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)
        }
      }

      "should return BadRequest when invalid data (trusteeName) is submitted" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CorporateTrusteeDetailsController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("nameOfCorporateTrustee" -> "")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      "should return BadRequest when invalid data (trusteePhoneNumber) is submitted" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CorporateTrusteeDetailsController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("trusteePhoneNumber" -> "")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      "should return BadRequest when postcode is missing but required (isUkAddress = true)" in {
        val sessionData                = OrganisationDetailsAnswers.setDoYouHaveCorporateTrusteeUKAddress(uKAddressTrue)
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CorporateTrusteeDetailsController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody(
                "nameOfCorporateTrustee"                 -> "Corporate Trustee1",
                "corporateTrusteeDaytimeTelephoneNumber" -> "01234567890"
              )

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }
    }
  }
}

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
import models.{OrganisationDetailsAnswers, SessionData}
import play.api.data.Form
import models.Mode.*

class CorporateTrusteeClaimControllerSpec extends ControllerSpec {
  private val form: Form[Boolean] = new YesNoFormProvider()()
  "CorporateTrusteeClaimController" - {
    "onPageLoad" - {
      "should render ClaimCompleteController if submissionReference is defined" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          lastUpdatedReference = Some(testCharitiesReference),
          submissionReference = Some(testCharitiesReference)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CorporateTrusteeClaimController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
      "should render the page correctly" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
        given application: Application = applicationBuilder(sessionData = sessionData).build()

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

        val sessionData =
          completeRepaymentDetailsAnswersSession.and(OrganisationDetailsAnswers.setAreYouACorporateTrustee(true))

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

        val sessionData =
          completeRepaymentDetailsAnswersSession.and(OrganisationDetailsAnswers.setAreYouACorporateTrustee(false))

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
      "should render ClaimCompleteController if submissionReference is defined" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          lastUpdatedReference = Some(testCharitiesReference),
          submissionReference = Some(testCharitiesReference)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CorporateTrusteeClaimController.onSubmit(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
      "should redirect to the next page when the value is true" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

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
        val sessionData                = completeRepaymentDetailsAnswersSession
        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

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
          val sessionData =
            completeRepaymentDetailsAnswersSession.and(OrganisationDetailsAnswers.setAreYouACorporateTrustee(true))

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
          val sessionData =
            completeRepaymentDetailsAnswersSession.and(OrganisationDetailsAnswers.setAreYouACorporateTrustee(false))

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
          val sessionData =
            completeRepaymentDetailsAnswersSession.and(OrganisationDetailsAnswers.setAreYouACorporateTrustee(true))

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
          val sessionData =
            completeRepaymentDetailsAnswersSession.and(OrganisationDetailsAnswers.setAreYouACorporateTrustee(false))

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

      "in CheckMode with no previous answer (entering new data)" - {
        "should redirect to CorporateTrusteeAddressController when selecting Yes" in {
          val sessionData                = completeRepaymentDetailsAnswersSession
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

        "should redirect to AuthorisedOfficialAddressController when selecting No" in {
          val sessionData                = completeRepaymentDetailsAnswersSession
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

      "should reload the page with errors when a required field is missing" in {
        val sessionData                = completeRepaymentDetailsAnswersSession
        given application: Application = applicationBuilder(sessionData = sessionData).build()

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

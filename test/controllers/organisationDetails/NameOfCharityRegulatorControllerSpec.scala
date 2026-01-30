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
import views.html.NameOfCharityRegulatorView
import play.api.Application
import forms.RadioListFormProvider
import models.{NameOfCharityRegulator, OrganisationDetailsAnswers}
import play.api.data.Form
import models.Mode.*
import models.NameOfCharityRegulator.*

class NameOfCharityRegulatorControllerSpec extends ControllerSpec {

  private val form: Form[NameOfCharityRegulator] = new RadioListFormProvider()(
    "NameOfCharityRegulator.error.required"
  )

  "NameOfCharityRegulatorController" - {
    "onPageLoad" - {
      "should render the page correctly" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.NameOfCharityRegulatorController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[NameOfCharityRegulatorView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with England and Wales value" in {

        val sessionData = OrganisationDetailsAnswers.setNameOfCharityRegulator(EnglandAndWales)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.NameOfCharityRegulatorController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[NameOfCharityRegulatorView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(NameOfCharityRegulator.EnglandAndWales), NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with Northern Ireland value" in {

        val sessionData = OrganisationDetailsAnswers.setNameOfCharityRegulator(NorthernIreland)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.NameOfCharityRegulatorController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[NameOfCharityRegulatorView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(NorthernIreland), NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with Scottish value" in {

        val sessionData = OrganisationDetailsAnswers.setNameOfCharityRegulator(Scottish)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.NameOfCharityRegulatorController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[NameOfCharityRegulatorView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(Scottish), NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with None value" in {

        val sessionData = OrganisationDetailsAnswers.setNameOfCharityRegulator(None)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.NameOfCharityRegulatorController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[NameOfCharityRegulatorView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(None), NormalMode).body
        }
      }
    }

    "onSubmit" - {
      "should redirect to the next page when the value is EnglandAndWales" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.NameOfCharityRegulatorController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "EnglandAndWales")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.CharityRegulatorNumberController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to the next page when the value is NorthernIreland" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.NameOfCharityRegulatorController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "NorthernIreland")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.CharityRegulatorNumberController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to the next page when the value is Scottish" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.NameOfCharityRegulatorController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "Scottish")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.CharityRegulatorNumberController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to the next page when the value is None" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.NameOfCharityRegulatorController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "None")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ReasonNotRegisteredWithRegulatorController.onPageLoad(NormalMode).url
          )
        }
      }

      "in CheckMode when changing from regulator to None" - {
        "should redirect to ReasonNotRegisteredWithRegulatorController" in {
          // Previous answer was a regulator (EnglandAndWales), now changing to None
          val sessionData = OrganisationDetailsAnswers.setNameOfCharityRegulator(EnglandAndWales)

          given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.NameOfCharityRegulatorController.onSubmit(CheckMode).url)
                .withFormUrlEncodedBody("value" -> "None")

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              routes.ReasonNotRegisteredWithRegulatorController.onPageLoad(CheckMode).url
            )
          }
        }
      }

      "in CheckMode when changing from None to regulator" - {
        "should redirect to CharityRegulatorNumberController" in {
          // Previous answer was None, now changing to a regulator
          val sessionData = OrganisationDetailsAnswers.setNameOfCharityRegulator(None)

          given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.NameOfCharityRegulatorController.onSubmit(CheckMode).url)
                .withFormUrlEncodedBody("value" -> "EnglandAndWales")

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              routes.CharityRegulatorNumberController.onPageLoad(CheckMode).url
            )
          }
        }
      }

      "in CheckMode when answer is unchanged (same regulator)" - {
        "should redirect to CYA when value remains EnglandAndWales" in {
          val sessionData = OrganisationDetailsAnswers.setNameOfCharityRegulator(EnglandAndWales)

          given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.NameOfCharityRegulatorController.onSubmit(CheckMode).url)
                .withFormUrlEncodedBody("value" -> "EnglandAndWales")

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url
            )
          }
        }

        "should redirect to CYA when value remains None" in {
          val sessionData = OrganisationDetailsAnswers.setNameOfCharityRegulator(None)

          given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.NameOfCharityRegulatorController.onSubmit(CheckMode).url)
                .withFormUrlEncodedBody("value" -> "None")

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url
            )
          }
        }
      }

      "in CheckMode when changing between regulators" - {
        "should redirect to CYA when changing from EnglandAndWales to Scottish" in {
          val sessionData = OrganisationDetailsAnswers.setNameOfCharityRegulator(EnglandAndWales)

          given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.NameOfCharityRegulatorController.onSubmit(CheckMode).url)
                .withFormUrlEncodedBody("value" -> "Scottish")

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url
            )
          }
        }
      }

      "in CheckMode with no previous answer (invalid state)" - {
        "should redirect to CYA when selecting regulator" in {
          given application: Application = applicationBuilder().mockSaveSession.build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.NameOfCharityRegulatorController.onSubmit(CheckMode).url)
                .withFormUrlEncodedBody("value" -> "EnglandAndWales")

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url
            )
          }
        }

        "should redirect to CYA when selecting None" in {
          given application: Application = applicationBuilder().mockSaveSession.build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.NameOfCharityRegulatorController.onSubmit(CheckMode).url)
                .withFormUrlEncodedBody("value" -> "None")

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
            FakeRequest(POST, routes.NameOfCharityRegulatorController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }
    }
  }
}

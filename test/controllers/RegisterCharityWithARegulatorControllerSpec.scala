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

package controllers

import controllers.ControllerSpec
import forms.YesNoFormProvider
import models.requests.DataRequest
import models.{OrganisationDetailsAnswers, ReasonNotRegisteredWithRegulator, SessionData}
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import services.{SaveService, UnregulatedDonationsService, UnregulatedLimitExceeded}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class RegisterCharityWithARegulatorControllerSpec extends ControllerSpec {

  val form: Form[Boolean] = new YesNoFormProvider()()

  class FakeUnregulatedDonationsService(
    limitExceeded: Option[UnregulatedLimitExceeded],
    applicableLimit: Option[String]
  ) extends UnregulatedDonationsService {
    def checkUnregulatedLimit(using DataRequest[?], HeaderCarrier): Future[Option[UnregulatedLimitExceeded]] =
      Future.successful(limitExceeded)
    def getApplicableLimit(using DataRequest[?]): Option[String]                                             = applicableLimit
  }

  def appWithFakeService(sessionData: SessionData, service: UnregulatedDonationsService): Application =
    applicationBuilder(sessionData = sessionData)
      .overrides(bind[UnregulatedDonationsService].toInstance(service))
      .build()

  // session data with unregulatedLimitExceeded = true so the data guard allows access
  val sessionDataLowIncome: SessionData = OrganisationDetailsAnswers
    .setReasonNotRegisteredWithRegulator(
      ReasonNotRegisteredWithRegulator.LowIncome
    )(using SessionData.empty(testCharitiesReference))
    .copy(unregulatedLimitExceeded = true)

  val sessionDataExcepted: SessionData = OrganisationDetailsAnswers
    .setReasonNotRegisteredWithRegulator(
      ReasonNotRegisteredWithRegulator.Excepted
    )(using SessionData.empty(testCharitiesReference))
    .copy(unregulatedLimitExceeded = true)

  // session data with flag = false - should be blocked by data guard
  val sessionDataNotExceeded: SessionData = OrganisationDetailsAnswers
    .setReasonNotRegisteredWithRegulator(
      ReasonNotRegisteredWithRegulator.LowIncome
    )(using SessionData.empty(testCharitiesReference))
    .copy(unregulatedLimitExceeded = false)

  // session data with defaultFormattedLimit fallback
  val sessionDataNoReason: SessionData = SessionData
    .empty(testCharitiesReference)
    .copy(unregulatedLimitExceeded = true)

  "RegisterCharityWithARegulatorController" - {
    "onPageLoad" - {
      "should render the page with LowIncome limit (£5,000) for LowIncome charity" in {
        val fakeService                = new FakeUnregulatedDonationsService(
          limitExceeded = Some(UnregulatedLimitExceeded(5000, "5,000")),
          applicableLimit = Some("5,000")
        )
        given application: Application = appWithFakeService(sessionDataLowIncome, fakeService)

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RegisterCharityWithARegulatorController.onPageLoad.url)

          val result = route(application, request).value

          status(result)        shouldBe OK
          contentAsString(result) should include("5,000")
        }
      }

      "should render the page with Excepted limit (£100,000) for Excepted charity" in {
        val fakeService                = new FakeUnregulatedDonationsService(
          limitExceeded = Some(UnregulatedLimitExceeded(100000, "100,000")),
          applicableLimit = Some("100,000")
        )
        given application: Application = appWithFakeService(sessionDataExcepted, fakeService)

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RegisterCharityWithARegulatorController.onPageLoad.url)

          val result = route(application, request).value

          status(result)        shouldBe OK
          contentAsString(result) should include("100,000")
        }
      }

      "should render the page with default Excepted limit (£100,000) when charity reason is not set (defaultFormattedLimit fallback)" in {
        val fakeService                = new FakeUnregulatedDonationsService(
          limitExceeded = Some(UnregulatedLimitExceeded(100000, "100,000")),
          applicableLimit = None
        )
        given application: Application = appWithFakeService(sessionDataNoReason, fakeService)

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RegisterCharityWithARegulatorController.onPageLoad.url)

          val result = route(application, request).value

          status(result)        shouldBe OK
          contentAsString(result) should include("100,000")
        }
      }

      "should redirect to Claims Task List when unregulatedLimitExceeded flag is false (data guard)" in {
        given application: Application = applicationBuilder(sessionData = sessionDataNotExceeded).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RegisterCharityWithARegulatorController.onPageLoad.url)

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ClaimsTaskListController.onPageLoad.url)
        }
      }
    }

    "onSubmit" - {
      "should reload the page with errors when a required field is missing" in {
        given application: Application = applicationBuilder(sessionData = sessionDataExcepted).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RegisterCharityWithARegulatorController.onSubmit.url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldBe BAD_REQUEST
        }
      }

      "should set unregulatedWarningBypassed and redirect to declaration screen when No is selected" in {
        val mockSaveService: SaveService = mock[SaveService]
        (mockSaveService
          .save(_: SessionData)(using _: HeaderCarrier))
          .expects(where { (sessionData: SessionData, _: HeaderCarrier) =>
            sessionData.unregulatedWarningBypassed
          })
          .returning(Future.successful(()))

        given application: Application =
          applicationBuilder(sessionData = sessionDataExcepted)
            .overrides(bind[SaveService].toInstance(mockSaveService))
            .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RegisterCharityWithARegulatorController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(claimDeclaration.routes.AdjustmentToThisClaimController.onPageLoad.url)
        }
      }

      "should reset flag and redirect to (Claims Task List) when Yes is selected" in {
        val mockSaveService: SaveService = mock[SaveService]
        (mockSaveService
          .save(_: SessionData)(using _: HeaderCarrier))
          .expects(where { (sessionData: SessionData, _: HeaderCarrier) =>
            !sessionData.unregulatedLimitExceeded
          })
          .returning(Future.successful(()))

        given application: Application =
          applicationBuilder(sessionData = sessionDataExcepted)
            .overrides(bind[SaveService].toInstance(mockSaveService))
            .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RegisterCharityWithARegulatorController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }
    }

    "WRN5 screen content" - {

      "should display WRN5 page title and LowIncome limit content" in {
        val fakeService                = new FakeUnregulatedDonationsService(
          limitExceeded = Some(UnregulatedLimitExceeded(5000, "5,000")),
          applicableLimit = Some("5,000")
        )
        given application: Application = appWithFakeService(sessionDataLowIncome, fakeService)

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RegisterCharityWithARegulatorController.onPageLoad.url)

          val result = route(application, request).value

          status(result)        shouldBe OK
          contentAsString(result) should include("5,000")
          contentAsString(result) should include("Registering your charity with a regulator")
          contentAsString(result) should include("Do you need to register your charity with a regulator")
        }
      }

      "should display WRN5 page title and Excepted limit content" in {
        val fakeService                = new FakeUnregulatedDonationsService(
          limitExceeded = Some(UnregulatedLimitExceeded(100000, "100,000")),
          applicableLimit = Some("100,000")
        )
        given application: Application = appWithFakeService(sessionDataExcepted, fakeService)

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RegisterCharityWithARegulatorController.onPageLoad.url)

          val result = route(application, request).value

          status(result)        shouldBe OK
          contentAsString(result) should include("100,000")
          contentAsString(result) should include("Registering your charity with a regulator")
          contentAsString(result) should include("Do you need to register your charity with a regulator")
        }
      }
    }
  }
}

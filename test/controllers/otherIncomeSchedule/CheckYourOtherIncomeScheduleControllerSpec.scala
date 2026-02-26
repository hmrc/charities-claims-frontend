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

package controllers.otherIncomeSchedule

import controllers.ControllerSpec
import controllers.otherIncomeSchedule.routes
import forms.YesNoFormProvider
import models.*
import models.requests.DataRequest
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import services.{ClaimsService, ClaimsValidationService, SaveService}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class CheckYourOtherIncomeScheduleControllerSpec extends ControllerSpec {

  val form: Form[Boolean] = new YesNoFormProvider()("checkYourOtherIncomeSchedule.error.required")

  val sessionData: SessionData =
    completeRepaymentDetailsAnswersSession.and(RepaymentClaimDetailsAnswers.setClaimingTaxDeducted(true))

  val testOtherIncomeScheduleData: OtherIncomeScheduleData = OtherIncomeScheduleData(
    adjustmentForOtherIncomePreviousOverClaimed = BigDecimal(0.00),
    totalOfGrossPayments = BigDecimal(100.00),
    totalOfTaxDeducted = BigDecimal(50.00),
    otherIncomes = Seq(
      OtherIncome(
        otherIncomeItem = 1,
        payerName = "Test Payer",
        paymentDate = "2025-01-24",
        grossPayment = BigDecimal(100.00),
        taxDeducted = BigDecimal(50.00)
      )
    )
  )

  "CheckYourOtherIncomeScheduleController" - {
    "onPageLoad" - {
      "should redirect to ClaimsTaskListController when data guard fails" in {
        val guardFailSessionData = RepaymentClaimDetailsAnswers.setClaimingTaxDeducted(false)

        given application: Application = applicationBuilder(sessionData = guardFailSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CheckYourOtherIncomeScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should render the page correctly when schedule data is available" in {
        val mockClaimsValidationService: ClaimsValidationService = mock[ClaimsValidationService]

        (mockClaimsValidationService
          .getOtherIncomeScheduleData(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(testOtherIncomeScheduleData))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CheckYourOtherIncomeScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result)        shouldBe OK
          contentAsString(result) should include("Check your Other Income schedule")
          contentAsString(result) should include("Test Payer")
        }
      }

      "should fail when no schedule data is available" in {
        val mockClaimsValidationService: ClaimsValidationService = mock[ClaimsValidationService]

        (mockClaimsValidationService
          .getOtherIncomeScheduleData(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.failed(new RuntimeException("No Other Income schedule data found")))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CheckYourOtherIncomeScheduleController.onPageLoad.url)

          val result = route(application, request).value

          a[RuntimeException] should be thrownBy result.futureValue
        }
      }
    }

    "onSubmit" - {
      "should return BadRequest when form has errors" in {
        val mockClaimsValidationService: ClaimsValidationService = mock[ClaimsValidationService]

        (mockClaimsValidationService
          .getOtherIncomeScheduleData(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(testOtherIncomeScheduleData))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CheckYourOtherIncomeScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result)        shouldBe BAD_REQUEST
          contentAsString(result) should include("Check your Other Income schedule")
        }
      }

      "should redirect to update other income schedule when Yes is selected" in {
        val mockClaimsValidationService: ClaimsValidationService = mock[ClaimsValidationService]

        (mockClaimsValidationService
          .getOtherIncomeScheduleData(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(testOtherIncomeScheduleData))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(bind[ClaimsValidationService].toInstance(mockClaimsValidationService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CheckYourOtherIncomeScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.UpdateOtherIncomeScheduleController.onPageLoad.url)
        }
      }

      "should redirect to claims task list when No is selected and schedule is already completed" in {
        val mockClaimsValidationService: ClaimsValidationService = mock[ClaimsValidationService]

        val completedSessionData = sessionData.copy(otherIncomeScheduleCompleted = true)

        (mockClaimsValidationService
          .getOtherIncomeScheduleData(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(testOtherIncomeScheduleData))

        given application: Application = applicationBuilder(sessionData = completedSessionData)
          .overrides(
            bind[ClaimsValidationService].toInstance(mockClaimsValidationService)
          )
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CheckYourOtherIncomeScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should save and redirect to success page when No is selected and schedule is not yet completed" in {
        val mockClaimsValidationService: ClaimsValidationService = mock[ClaimsValidationService]
        val mockClaimsService: ClaimsService                     = mock[ClaimsService]
        val mockSaveService: SaveService                         = mock[SaveService]

        (mockClaimsValidationService
          .getOtherIncomeScheduleData(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(testOtherIncomeScheduleData))

        (mockSaveService
          .save(_: SessionData)(using _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        (mockClaimsService
          .save(using _: HeaderCarrier))
          .expects(*)
          .returning(Future.successful(()))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(
            bind[ClaimsValidationService].toInstance(mockClaimsValidationService),
            bind[ClaimsService].toInstance(mockClaimsService),
            bind[SaveService].toInstance(mockSaveService)
          )
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CheckYourOtherIncomeScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.OtherIncomeScheduleUploadSuccessfulController.onPageLoad.url)
        }
      }
    }
  }
}

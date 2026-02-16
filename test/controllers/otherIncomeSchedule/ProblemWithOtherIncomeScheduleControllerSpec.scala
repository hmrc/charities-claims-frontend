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

import connectors.ClaimsValidationConnector
import controllers.ControllerSpec
import controllers.otherIncomeSchedule.routes
import models.*
import models.requests.DataRequest
import play.api.{inject, Application}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import services.ClaimsValidationService
import uk.gov.hmrc.http.HeaderCarrier
import util.TestResources

import scala.concurrent.Future

class ProblemWithOtherIncomeScheduleControllerSpec extends ControllerSpec {

  val mockClaimsValidationConnector: ClaimsValidationConnector = mock[ClaimsValidationConnector]
  val mockClaimsValidationService: ClaimsValidationService     = mock[ClaimsValidationService]

  override protected val additionalBindings: List[GuiceableModule] = List(
    bind[ClaimsValidationConnector].toInstance(mockClaimsValidationConnector),
    bind[ClaimsValidationService].toInstance(mockClaimsValidationService)
  )

  val testClaimId: String                          = "test-claim-id-123"
  val testFileUploadReference: FileUploadReference = FileUploadReference("test-file-upload-ref")

  lazy val testValidationFailedJsonString: String =
    TestResources.readTestResource("/test-get-upload-result-validation-failed-other-income.json")

  lazy val testValidatedJsonString: String =
    TestResources.readTestResource("/test-get-upload-result-validated-other-income.json")

  // parse JSON into model objects
  lazy val testValidationFailedResponse: GetUploadResultValidationFailedOtherIncome =
    Json.parse(testValidationFailedJsonString).as[GetUploadResultValidationFailedOtherIncome]

  lazy val testValidatedResponse: GetUploadResultValidatedOtherIncome =
    Json.parse(testValidatedJsonString).as[GetUploadResultValidatedOtherIncome]

  "ProblemWithOtherIncomeScheduleController" - {

    "onPageLoad" - {

      // Redirect Tests:

      "should redirect to RepaymentClaimDetailsController when no claimId in session" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingTaxDeducted(true)
          .copy(unsubmittedClaimId = None)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithOtherIncomeScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.repaymentClaimDetails.routes.RepaymentClaimDetailsController.onPageLoad.url
          )
        }
      }

      "should redirect to UploadOtherIncomeScheduleController when no file upload reference in session" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingTaxDeducted(true)
          .copy(
            unsubmittedClaimId = Some(testClaimId),
            otherIncomeScheduleFileUploadReference = None
          )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithOtherIncomeScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadOtherIncomeScheduleController.onPageLoad.url
          )
        }
      }

      // Page Render Tests:

      "should render page with validation errors when upload has VALIDATION_FAILED status" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingTaxDeducted(true)
          .copy(
            unsubmittedClaimId = Some(testClaimId),
            otherIncomeScheduleFileUploadReference = Some(testFileUploadReference)
          )

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithOtherIncomeScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include("There is a problem with the data in your Other Income schedule")
        }
      }

      "should display error messages in the table" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingTaxDeducted(true)
          .copy(
            unsubmittedClaimId = Some(testClaimId),
            otherIncomeScheduleFileUploadReference = Some(testFileUploadReference)
          )

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithOtherIncomeScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("ERROR: The amount overclaimed is in the wrong format. Please amend your entry.")
          content should include("ERROR: Item 1 Name of payer is missing.")
          content should include(
            "ERROR: Item 5 Name of payer format is invalid. You can only supply a maximum of 40 valid characters."
          )
          content should include("ERROR: Item 9 The tax deducted you have provided is in an invalid format.")
        }
      }

      "should display all 10 errors from test schedule data" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingTaxDeducted(true)
          .copy(
            unsubmittedClaimId = Some(testClaimId),
            otherIncomeScheduleFileUploadReference = Some(testFileUploadReference)
          )

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithOtherIncomeScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("ERROR: The amount overclaimed is in the wrong format. Please amend your entry.")
          content should include("ERROR: Item 1 Name of payer is missing.")
          content should include("ERROR: Item 2 Income date is missing.")
          content should include("ERROR: Item 3 Gross payment is missing.")
          content should include("ERROR: Item 4 Tax deducted is missing.")
          content should include(
            "ERROR: Item 5 Name of payer format is invalid. You can only supply a maximum of 40 valid characters."
          )
          content should include("ERROR: Item 6 Income date is in an invalid format. It should be DD/MM/YY.")
          content should include("ERROR: Item 7 Other income date of payment cannot be in the future.")
          content should include("ERROR: Item 8 The gross payment you have provided is in an invalid format.")
          content should include("ERROR: Item 9 The tax deducted you have provided is in an invalid format.")
        }
      }

      "should display number 24 in row column exactly 1 time from FAILED_VALIDATION test data" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingTaxDeducted(true)
          .copy(
            unsubmittedClaimId = Some(testClaimId),
            otherIncomeScheduleFileUploadReference = Some(testFileUploadReference)
          )

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithOtherIncomeScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          val zeroRowPattern = ">24<".r
          zeroRowPattern.findAllIn(content).length shouldEqual 1
        }
      }

      "should display number 32 in row column exactly 1 time from FAILED_VALIDATION test data" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingTaxDeducted(true)
          .copy(
            unsubmittedClaimId = Some(testClaimId),
            otherIncomeScheduleFileUploadReference = Some(testFileUploadReference)
          )

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithOtherIncomeScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          val zeroRowPattern = ">32<".r
          zeroRowPattern.findAllIn(content).length shouldEqual 1
        }
      }

      "should default to page 1 of pagination when page query parameter is not a valid integer" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingTaxDeducted(true)
          .copy(
            unsubmittedClaimId = Some(testClaimId),
            otherIncomeScheduleFileUploadReference = Some(testFileUploadReference)
          )

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithOtherIncomeScheduleController.onPageLoad.url + "?page=invalid")

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include("There is a problem with the data in your Other Income schedule")
        }
      }

      "should include the otherIncomeScheduleSpreadsheetGuidanceUrl link" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingTaxDeducted(true)
          .copy(
            unsubmittedClaimId = Some(testClaimId),
            otherIncomeScheduleFileUploadReference = Some(testFileUploadReference)
          )

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithOtherIncomeScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("schedule-spreadsheet-to-reclaim-tax-on-interest-and-other-income")
        }
      }

      "should include the (attach updated schedule) button" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingTaxDeducted(true)
          .copy(
            unsubmittedClaimId = Some(testClaimId),
            otherIncomeScheduleFileUploadReference = Some(testFileUploadReference)
          )

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithOtherIncomeScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("Attach an updated Other Income schedule")
        }
      }

      "should include the delete schedule link" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingTaxDeducted(true)
          .copy(
            unsubmittedClaimId = Some(testClaimId),
            otherIncomeScheduleFileUploadReference = Some(testFileUploadReference)
          )

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithOtherIncomeScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("Delete schedule")
          content should include("delete-other-income-schedule")
        }
      }

      "should redirect to YourOtherIncomeScheduleUploadController when upload result is VALIDATED" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingTaxDeducted(true)
          .copy(
            unsubmittedClaimId = Some(testClaimId),
            otherIncomeScheduleFileUploadReference = Some(testFileUploadReference)
          )

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidatedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithOtherIncomeScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.YourOtherIncomeScheduleUploadController.onPageLoad.url
          )
        }
      }

      "should NOT render problem page when upload has VALIDATED status (should redirect)" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingTaxDeducted(true)
          .copy(
            unsubmittedClaimId = Some(testClaimId),
            otherIncomeScheduleFileUploadReference = Some(testFileUploadReference)
          )

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidatedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithOtherIncomeScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual SEE_OTHER
          (content should not).include("There is a problem with the data in your Other Income schedule")
        }
      }
    }

    "onSubmit" - {

      "should delete the schedule and redirect to UploadOtherIncomeScheduleController in Attach updated schedule path" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingTaxDeducted(true)
          .copy(
            unsubmittedClaimId = Some(testClaimId),
            otherIncomeScheduleFileUploadReference = Some(testFileUploadReference)
          )

        (mockClaimsValidationService
          .deleteOtherIncomeSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.ProblemWithOtherIncomeScheduleController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadOtherIncomeScheduleController.onPageLoad.url
          )
        }
      }

      "should handle when delete fails" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingTaxDeducted(true)
          .copy(
            unsubmittedClaimId = Some(testClaimId),
            otherIncomeScheduleFileUploadReference = Some(testFileUploadReference)
          )

        (mockClaimsValidationService
          .deleteOtherIncomeSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.failed(new RuntimeException("Delete failed")))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.ProblemWithOtherIncomeScheduleController.onSubmit.url)

          val result = route(application, request).value

          a[RuntimeException] should be thrownBy result.futureValue
        }
      }
    }
  }
}

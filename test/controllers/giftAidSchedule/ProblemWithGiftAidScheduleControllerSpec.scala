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

package controllers.giftAidSchedule

import connectors.ClaimsValidationConnector
import controllers.ControllerSpec
import controllers.giftAidSchedule.routes
import models.*
import models.requests.DataRequest
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.inject
import services.ClaimsValidationService
import uk.gov.hmrc.http.HeaderCarrier
import util.TestResources

import scala.concurrent.Future

class ProblemWithGiftAidScheduleControllerSpec extends ControllerSpec {

  val mockClaimsValidationConnector: ClaimsValidationConnector = mock[ClaimsValidationConnector]
  val mockClaimsValidationService: ClaimsValidationService     = mock[ClaimsValidationService]

  override protected val additionalBindings: List[GuiceableModule] = List(
    bind[ClaimsValidationConnector].toInstance(mockClaimsValidationConnector),
    bind[ClaimsValidationService].toInstance(mockClaimsValidationService)
  )

  val testClaimId: String                          = "test-claim-id"
  val testFileUploadReference: FileUploadReference = FileUploadReference("test-file-upload-ref")

  lazy val testValidationFailedJsonString: String =
    TestResources.readTestResource("/test-get-upload-result-validation-failed-gift-aid.json")

  lazy val testValidatedJsonString: String =
    TestResources.readTestResource("/test-get-upload-result-validated-gift-aid.json")

  // parse JSON into model objects
  lazy val testValidationFailedResponse: GetUploadResultValidationFailedGiftAid =
    Json.parse(testValidationFailedJsonString).as[GetUploadResultValidationFailedGiftAid]

  lazy val testValidatedResponse: GetUploadResultValidatedGiftAid =
    Json.parse(testValidatedJsonString).as[GetUploadResultValidatedGiftAid]

  "ProblemWithGiftAidScheduleController" - {

    "onPageLoad" - {

      // Redirect Tests:

      "should decline when no claimId in session" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingGiftAid(true)
          .copy(unsubmittedClaimId = None)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithGiftAidScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should redirect to UploadGiftAidScheduleController when no file upload reference in session" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
          .copy(giftAidScheduleFileUploadReference = None)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithGiftAidScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadGiftAidScheduleController.onPageLoad.url
          )
        }
      }

      // Page Render Tests:

      "should render page with validation errors when upload has VALIDATION_FAILED status" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
          .copy(giftAidScheduleFileUploadReference = Some(testFileUploadReference))

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithGiftAidScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include("There is a problem with the data in your Gift Aid schedule")
        }
      }

      "should display error messages in the table" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
          .copy(giftAidScheduleFileUploadReference = Some(testFileUploadReference))

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithGiftAidScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("ERROR: Earliest donation date is missing.")
          content should include("ERROR: Item You have entered data in an invalid area of the form.")
          content should include(
            "ERROR: Item 3 You cannot provide both a title and an entry for an aggregated donation box."
          )
        }
      }

      "should display all 5 errors from test schedule data" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
          .copy(giftAidScheduleFileUploadReference = Some(testFileUploadReference))

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithGiftAidScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("ERROR: Earliest donation date is missing.")
          content should include("ERROR: Item You have entered data in an invalid area of the form.")
          content should include("You cannot provide both a title and an entry for an aggregated donation box")
          content should include("You cannot provide both a first name and an entry for an aggregated donation")
          content should include("You cannot provide both a last name and an entry for an aggregated donation")
        }
      }

      "should display number 0 in row column for earliestDonationDate error (string field defaults to 0)" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
          .copy(giftAidScheduleFileUploadReference = Some(testFileUploadReference))

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithGiftAidScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          val zeroRowPattern = ">0<".r
          zeroRowPattern.findAllIn(content).length should be >= 1
        }
      }

      "should display row column with number 3 at least 3 occurrences using test data" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
          .copy(giftAidScheduleFileUploadReference = Some(testFileUploadReference))

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithGiftAidScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          val threeRowPattern = ">3<".r
          threeRowPattern.findAllIn(content).length should be >= 3
        }
      }

      "should default to page 1 of pagination when page query parameter is not a valid integer" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
          .copy(giftAidScheduleFileUploadReference = Some(testFileUploadReference))

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithGiftAidScheduleController.onPageLoad.url + "?page=invalid")

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("ERROR:")
          // The first error message should be visible on page 1
          content should include("Earliest donation date is missing")
        }
      }

      "should include the giftAidScheduleSpreadsheetGuidanceUrl link" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
          .copy(giftAidScheduleFileUploadReference = Some(testFileUploadReference))

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithGiftAidScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("schedule-spreadsheet-to-claim-back-tax-on-gift-aid-donations")
        }
      }

      "should include the (attach updated schedule) button" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
          .copy(giftAidScheduleFileUploadReference = Some(testFileUploadReference))

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithGiftAidScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("Attach an updated Gift Aid schedule")
        }
      }

      "should include the delete schedule link" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
          .copy(giftAidScheduleFileUploadReference = Some(testFileUploadReference))

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithGiftAidScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("Delete schedule")
          content should include("delete-gift-aid-schedule")
        }
      }

      "should redirect to YourGiftAidScheduleUploadController when upload result is VALIDATED" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
          .copy(giftAidScheduleFileUploadReference = Some(testFileUploadReference))

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidatedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithGiftAidScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.YourGiftAidScheduleUploadController.onPageLoad.url
          )
        }
      }

      "should NOT render problem page when upload has VALIDATED status (should redirect)" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
          .copy(giftAidScheduleFileUploadReference = Some(testFileUploadReference))

        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidatedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithGiftAidScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual SEE_OTHER
          (content should not).include("There is a problem with the data in your Gift Aid schedule")
        }
      }
    }

    "onSubmit" - {

      "should delete the schedule and redirect to UploadGiftAidScheduleController in Attach updated schedule path" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
          .copy(giftAidScheduleFileUploadReference = Some(testFileUploadReference))

        (mockClaimsValidationService
          .deleteGiftAidSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.ProblemWithGiftAidScheduleController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadGiftAidScheduleController.onPageLoad.url
          )
        }
      }

      "should handle when delete fails" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingGiftAid(true))
          .copy(giftAidScheduleFileUploadReference = Some(testFileUploadReference))

        (mockClaimsValidationService
          .deleteGiftAidSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.failed(new RuntimeException("Delete failed")))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.ProblemWithGiftAidScheduleController.onSubmit.url)

          val result = route(application, request).value

          a[RuntimeException] should be thrownBy result.futureValue
        }
      }
    }
  }
}

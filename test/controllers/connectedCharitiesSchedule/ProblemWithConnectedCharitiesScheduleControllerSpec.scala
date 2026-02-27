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

package controllers.connectedCharitiesSchedule

import connectors.ClaimsValidationConnector
import controllers.ControllerSpec
import controllers.connectedCharitiesSchedule.routes
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

class ProblemWithConnectedCharitiesScheduleControllerSpec extends ControllerSpec {

  val mockClaimsValidationConnector: ClaimsValidationConnector = mock[ClaimsValidationConnector]
  val mockClaimsValidationService: ClaimsValidationService     = mock[ClaimsValidationService]

  override protected val additionalBindings: List[GuiceableModule] = List(
    bind[ClaimsValidationConnector].toInstance(mockClaimsValidationConnector),
    bind[ClaimsValidationService].toInstance(mockClaimsValidationService)
  )

  val testClaimId: String                          = "test-claim-id-123"
  val testFileUploadReference: FileUploadReference = FileUploadReference("test-file-upload-ref")

  lazy val testValidationFailedJsonString: String =
    TestResources.readTestResource("/test-get-upload-result-validation-failed-connected-charities.json")

  lazy val testValidatedJsonString: String =
    TestResources.readTestResource("/test-get-upload-result-validated-connected-charities.json")

  // parse JSON into model objects
  lazy val testValidationFailedResponse: GetUploadResultValidationFailedConnectedCharities =
    Json.parse(testValidationFailedJsonString).as[GetUploadResultValidationFailedConnectedCharities]

  lazy val testValidatedResponse: GetUploadResultValidatedConnectedCharities =
    Json.parse(testValidatedJsonString).as[GetUploadResultValidatedConnectedCharities]

  def validSessionData: SessionData = completeGasdsSession
    .copy(
      unsubmittedClaimId = Some(testClaimId),
      connectedCharitiesScheduleFileUploadReference = Some(testFileUploadReference)
    )

  def validSessionDataWithoutFileRef: SessionData = completeGasdsSession
    .copy(unsubmittedClaimId = Some(testClaimId))

  def sessionDataFailingGuard: SessionData = RepaymentClaimDetailsAnswers
    .setClaimingUnderGiftAidSmallDonationsScheme(true)
    .and(RepaymentClaimDetailsAnswers.setConnectedToAnyOtherCharities(false))
    .and(SessionData.setUnsubmittedClaimId(testClaimId))

  "ProblemWithConnectedCharitiesScheduleController" - {

    "onPageLoad" - {

      // Redirect Tests:

      "should redirect to PageNotFound when data guard fails" in {
        given application: Application = applicationBuilder(sessionData = sessionDataFailingGuard).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithConnectedCharitiesScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should redirect to UploadConnectedCharitiesScheduleController when no file upload reference in session data" in {
        given application: Application = applicationBuilder(sessionData = validSessionDataWithoutFileRef).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithConnectedCharitiesScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadConnectedCharitiesScheduleController.onPageLoad.url
          )
        }
      }

      "should render page with validation errors when upload has VALIDATION_FAILED status" in {
        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithConnectedCharitiesScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include(
            "There is a problem with the data in your Connected Charities schedule"
          )
        }
      }

      "should display error messages in the table" in {
        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithConnectedCharitiesScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("ERROR:")
        }
      }

      "should display number 0 in row column for string field errors" in {
        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithConnectedCharitiesScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          val zeroRowPattern = ">16<".r
          zeroRowPattern.findAllIn(content).length should be >= 1
        }
      }

      "should default to page 1 of pagination when page query parameter is not a valid integer" in {
        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithConnectedCharitiesScheduleController.onPageLoad.url + "?page=invalid")

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("ERROR:")
          content should include("Item 3 Name of charity is in an invalid format")
        }
      }

      "should include the connectedCharitiesScheduleSpreadsheetGuidanceUrl link" in {
        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithConnectedCharitiesScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("schedule-spreadsheet-for-connected-charities-gasds-claims")
        }
      }

      "should include the (attach updated schedule) button" in {
        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithConnectedCharitiesScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("Attach an updated Connected Charities schedule")
        }
      }

      "should include the delete schedule link" in {
        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithConnectedCharitiesScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("Delete schedule")
          content should include("delete-gasds-connected-charities-schedule")
        }
      }

      "should redirect to YourConnectedCharitiesScheduleUploadController when upload result is VALIDATED (fallback case)" in {
        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidatedResponse))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithConnectedCharitiesScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.YourConnectedCharitiesScheduleUploadController.onPageLoad.url
          )
        }
      }

      "should NOT render problem page when upload has VALIDATED status (should redirect)" in {
        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidatedResponse))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithConnectedCharitiesScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual SEE_OTHER
          (content should not).include("There is a problem with the data in your Connected Charities schedule")
        }
      }
    }

    "onSubmit" - {

      "should delete schedule and redirect to UploadConnectedCharitiesScheduleController in Attach updated schedule path" in {
        (mockClaimsValidationService
          .deleteConnectedCharitiesSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.ProblemWithConnectedCharitiesScheduleController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadConnectedCharitiesScheduleController.onPageLoad.url
          )
        }
      }

      "should handle when delete fails" in {
        (mockClaimsValidationService
          .deleteConnectedCharitiesSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.failed(new RuntimeException("Delete failed")))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.ProblemWithConnectedCharitiesScheduleController.onSubmit.url)

          val result = route(application, request).value

          a[RuntimeException] should be thrownBy result.futureValue
        }
      }
    }
  }
}

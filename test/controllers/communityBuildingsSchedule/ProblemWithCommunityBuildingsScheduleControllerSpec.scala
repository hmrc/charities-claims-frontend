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

package controllers.communityBuildingsSchedule

import connectors.ClaimsValidationConnector
import controllers.ControllerSpec
import controllers.communityBuildingsSchedule.routes
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

class ProblemWithCommunityBuildingsScheduleControllerSpec extends ControllerSpec {

  val mockClaimsValidationConnector: ClaimsValidationConnector = mock[ClaimsValidationConnector]
  val mockClaimsValidationService: ClaimsValidationService     = mock[ClaimsValidationService]

  override protected val additionalBindings: List[GuiceableModule] = List(
    bind[ClaimsValidationConnector].toInstance(mockClaimsValidationConnector),
    bind[ClaimsValidationService].toInstance(mockClaimsValidationService)
  )

  val testClaimId: String                          = "test-claim-id-123"
  val testFileUploadReference: FileUploadReference = FileUploadReference("test-file-upload-ref")

  lazy val testValidationFailedJsonString: String =
    TestResources.readTestResource("/test-get-upload-result-validation-failed-community-buildings.json")

  lazy val testValidatedJsonString: String =
    TestResources.readTestResource("/test-get-upload-result-validated-community-buildings.json")

  // parse JSON into model objects
  lazy val testValidationFailedResponse: GetUploadResultValidationFailedCommunityBuildings =
    Json.parse(testValidationFailedJsonString).as[GetUploadResultValidationFailedCommunityBuildings]

  lazy val testValidatedResponse: GetUploadResultValidatedCommunityBuildings =
    Json.parse(testValidatedJsonString).as[GetUploadResultValidatedCommunityBuildings]

  def validSessionData: SessionData = completeGasdsSession
    .copy(
      unsubmittedClaimId = Some(testClaimId),
      communityBuildingsScheduleFileUploadReference = Some(testFileUploadReference)
    )

  def validSessionDataWithoutFileRef: SessionData = completeGasdsSession
    .copy(unsubmittedClaimId = Some(testClaimId))

  def sessionDataFailingGuard: SessionData = RepaymentClaimDetailsAnswers
    .setClaimingUnderGiftAidSmallDonationsScheme(true)
    .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(false, None))
    .and(SessionData.setUnsubmittedClaimId(testClaimId))

  "ProblemWithCommunityBuildingsScheduleController" - {

    "onPageLoad" - {

      // Redirect Tests:

      "should redirect to ClaimsTaskListController when data guard fails" in {
        given application: Application = applicationBuilder(sessionData = sessionDataFailingGuard).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithCommunityBuildingsScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should redirect to UploadCommunityBuildingsScheduleController when no file upload reference in session data" in {
        given application: Application = applicationBuilder(sessionData = validSessionDataWithoutFileRef).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithCommunityBuildingsScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadCommunityBuildingsScheduleController.onPageLoad.url
          )
        }
      }

      // Page Render Tests:

      "should render page with validation errors when upload has VALIDATION_FAILED status" in {
        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithCommunityBuildingsScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include(
            "There is a problem with the data in your Community Buildings schedule"
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
            FakeRequest(GET, routes.ProblemWithCommunityBuildingsScheduleController.onPageLoad.url)

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
            FakeRequest(GET, routes.ProblemWithCommunityBuildingsScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          val zeroRowPattern = ">0<".r
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
            FakeRequest(GET, routes.ProblemWithCommunityBuildingsScheduleController.onPageLoad.url + "?page=invalid")

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("ERROR:")
          // the first error message from test json file should be visible on page 1
          content should include("If donations under the Gift Aid Small Donations Scheme is being claimed")
        }
      }

      "should include the communityBuildingsScheduleSpreadsheetGuidanceUrl link" in {
        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithCommunityBuildingsScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("schedule-spreadsheet-for-community-building-gasds-claims")
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
            FakeRequest(GET, routes.ProblemWithCommunityBuildingsScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("Attach an updated Community Buildings schedule")
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
            FakeRequest(GET, routes.ProblemWithCommunityBuildingsScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("Delete schedule")
          content should include("delete-gasds-community-buildings-schedule")
        }
      }

      "should redirect to YourCommunityBuildingsScheduleUploadController when upload result is VALIDATED (fallback case)" in {
        (mockClaimsValidationConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(testClaimId, testFileUploadReference, *)
          .returning(Future.successful(testValidatedResponse))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithCommunityBuildingsScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.YourCommunityBuildingsScheduleUploadController.onPageLoad.url
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
            FakeRequest(GET, routes.ProblemWithCommunityBuildingsScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual SEE_OTHER
          (content should not).include("There is a problem with the data in your Community Buildings schedule")
        }
      }
    }

    "onSubmit" - {

      "should delete schedule and redirect to UploadCommunityBuildingsScheduleController in Attach updated schedule path" in {
        (mockClaimsValidationService
          .deleteCommunityBuildingsSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.ProblemWithCommunityBuildingsScheduleController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadCommunityBuildingsScheduleController.onPageLoad.url
          )
        }
      }

      "should handle when delete fails" in {
        (mockClaimsValidationService
          .deleteCommunityBuildingsSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.failed(new RuntimeException("Delete failed")))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.ProblemWithCommunityBuildingsScheduleController.onSubmit.url)

          val result = route(application, request).value

          a[RuntimeException] should be thrownBy result.futureValue
        }
      }
    }
  }
}

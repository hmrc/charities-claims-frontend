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
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import services.{ClaimsService, ClaimsValidationService, SaveService}
import uk.gov.hmrc.http.HeaderCarrier
import util.TestResources

import scala.concurrent.Future

class CheckYourCommunityBuildingsScheduleControllerSpec extends ControllerSpec {

  val mockClaimsValidationConnector: ClaimsValidationConnector = mock[ClaimsValidationConnector]
  val mockClaimsValidationService: ClaimsValidationService     = mock[ClaimsValidationService]
  val mockClaimsService: ClaimsService                         = mock[ClaimsService]
  val mockSaveService: SaveService                             = mock[SaveService]

  override protected val additionalBindings: List[GuiceableModule] = List(
    bind[ClaimsValidationConnector].toInstance(mockClaimsValidationConnector),
    bind[ClaimsValidationService].toInstance(mockClaimsValidationService),
    bind[ClaimsService].toInstance(mockClaimsService),
    bind[SaveService].toInstance(mockSaveService)
  )

  val testClaimId: String                          = "test-claim-id-123"
  val testFileUploadReference: FileUploadReference = FileUploadReference("test-file-upload-ref")

  lazy val testValidatedJsonString: String =
    TestResources.readTestResource("/test-get-upload-result-validated-community-buildings.json")

  // parse JSON into model objects
  lazy val testValidatedResponse: GetUploadResultValidatedCommunityBuildings =
    Json.parse(testValidatedJsonString).as[GetUploadResultValidatedCommunityBuildings]

  // test session data that passes DataGuard (shouldUploadCommunityBuildingsSchedule) using completeGasdsSession as base
  def validSessionData: SessionData = completeGasdsSession
    .copy(
      unsubmittedClaimId = Some(testClaimId),
      communityBuildingsScheduleFileUploadReference = Some(testFileUploadReference)
    )

  def validSessionDataCompleted: SessionData = validSessionData.copy(communityBuildingsScheduleCompleted = true)

  def sessionDataFailingGuard: SessionData = RepaymentClaimDetailsAnswers
    .setClaimingUnderGiftAidSmallDonationsScheme(true)
    .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(false, None))
    .and(SessionData.setUnsubmittedClaimId(testClaimId))

  "CheckYourCommunityBuildingsScheduleController" - {

    "onPageLoad" - {

      "should redirect to PageNotFound when data guard is triggered" in {
        given application: Application = applicationBuilder(sessionData = sessionDataFailingGuard).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CheckYourCommunityBuildingsScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.PageNotFoundController.onPageLoad.url
          )
        }
      }

      "should render the page when schedule data is available" in {
        (mockClaimsValidationService
          .getCommunityBuildingsScheduleData(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(testValidatedResponse.communityBuildingsData))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CheckYourCommunityBuildingsScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include("Check your Community Buildings schedule")
        }
      }

      "should display community buildings data in the table" in {
        (mockClaimsValidationService
          .getCommunityBuildingsScheduleData(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(testValidatedResponse.communityBuildingsData))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CheckYourCommunityBuildingsScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("The Vault")
        }
      }

      "should default to page 1 when page query parameter is not a valid integer" in {
        (mockClaimsValidationService
          .getCommunityBuildingsScheduleData(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(testValidatedResponse.communityBuildingsData))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CheckYourCommunityBuildingsScheduleController.onPageLoad.url + "?page=invalid")

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          // the first community building from test json file should be visible on page 1
          content should include("The Vault")
        }
      }
    }

    "onSubmit" - {

      "should return BadRequest when form onSubmit has errors and display error message" in {
        (mockClaimsValidationService
          .getCommunityBuildingsScheduleData(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(testValidatedResponse.communityBuildingsData))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CheckYourCommunityBuildingsScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("other" -> "field")

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual BAD_REQUEST
          content should include("Check your Community Buildings schedule")
          content should include("Select ‘Yes’ if you need to update this Community Buildings schedule")
        }
      }

      "should redirect to update screen when Yes is selected" in {
        (mockClaimsValidationService
          .getCommunityBuildingsScheduleData(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(testValidatedResponse.communityBuildingsData))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CheckYourCommunityBuildingsScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UpdateCommunityBuildingsScheduleController.onPageLoad.url
          )
        }
      }

      "should redirect to Task List when No is selected and schedule is already completed previously" in {
        (mockClaimsValidationService
          .getCommunityBuildingsScheduleData(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(testValidatedResponse.communityBuildingsData))

        given application: Application = applicationBuilder(sessionData = validSessionDataCompleted).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CheckYourCommunityBuildingsScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }
// TODO: redirect should be updated to CommunityBuildingsScheduleUploadSuccessfulController when it is added
      "should save and redirect to UploadSuccessful when No is selected and schedule not yet completed before" in {
        (mockClaimsValidationService
          .getCommunityBuildingsScheduleData(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(testValidatedResponse.communityBuildingsData))

        (mockSaveService
          .save(_: SessionData)(using _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        (mockClaimsService
          .save(using _: HeaderCarrier))
          .expects(*)
          .returning(Future.successful(()))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CheckYourCommunityBuildingsScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            // TODO: CommunityBuildingsScheduleUploadSuccessfulController to be added in the below route
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }
    }
  }
}

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

  // Test session data for shouldUploadCommunityBuildingsSchedule DataGuard
  def validSessionData: SessionData = RepaymentClaimDetailsAnswers
    .setClaimingUnderGiftAidSmallDonationsScheme(true)
    .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, None))
    .and(SessionData.setUnsubmittedClaimId(testClaimId))
    .copy(communityBuildingsScheduleFileUploadReference = Some(testFileUploadReference))

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

      "should render the page correctly when schedule data is available" in {
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
    }

    "onSubmit" - {
    // TODO: content check TBC
      "should return BadRequest when form onSubmit has errors" in {
        (mockClaimsValidationService
          .getCommunityBuildingsScheduleData(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(testValidatedResponse.communityBuildingsData))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CheckYourCommunityBuildingsScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("other" -> "field")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
          contentAsString(result) should include("Check your Community Buildings schedule")
        }
      }

      "should redirect to upload page when Yes is selected" in {
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
            routes.UploadCommunityBuildingsScheduleController.onPageLoad.url
          )
        }
      }
// TODO: this is to be updated with correct route
      "should redirect correctly when No is selected and schedule is already completed" in {
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
// TODO: this is to be updated
      "should save and redirect when No is selected and schedule is not yet completed" in {
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
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }
    }
  }
}

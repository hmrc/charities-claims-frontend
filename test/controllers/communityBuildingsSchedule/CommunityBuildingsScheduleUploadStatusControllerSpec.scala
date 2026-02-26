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

import play.api.test.FakeRequest
import services.ClaimsValidationService
import util.TestResources
import play.api.mvc.AnyContentAsEmpty
import controllers.ControllerSpec
import uk.gov.hmrc.http.HeaderCarrier
import models.{FileUploadReference, RepaymentClaimDetailsAnswers, *}
import models.requests.DataRequest
import play.api.{inject, Application}
import play.api.libs.json.{JsBoolean, JsString, Json}

import scala.concurrent.Future

class CommunityBuildingsScheduleUploadStatusControllerSpec extends ControllerSpec {

  given HeaderCarrier = HeaderCarrier()

  val mockService: ClaimsValidationService = mock[ClaimsValidationService]

  private val claimId             = "test-claim-id"
  private val fileUploadReference = FileUploadReference("test-file-upload-reference")

  private def readJson(path: String) = Json.parse(TestResources.readTestResource(path))

  // parse JSON into model objects
  lazy val testValidationFailedResponse: GetUploadResultValidationFailedCommunityBuildings =
    readJson("/test-get-upload-result-validation-failed-community-buildings.json")
      .as[GetUploadResultValidationFailedCommunityBuildings]

  lazy val testValidatedResponse: GetUploadResultValidatedCommunityBuildings =
    readJson("/test-get-upload-result-validated-community-buildings.json")
      .as[GetUploadResultValidatedCommunityBuildings]

  lazy val testAwaitingResponse: GetUploadResultAwaitingUpload =
    readJson("/test-get-upload-result-awaiting-upload-community-buildings.json").as[GetUploadResultAwaitingUpload]

  lazy val testVerifyingResponse: GetUploadResultVeryfying =
    readJson("/test-get-upload-result-verifying-community-buildings.json").as[GetUploadResultVeryfying]

  lazy val testValidatingResponse: GetUploadResultValidating =
    readJson("/test-get-upload-result-validating-community-buildings.json").as[GetUploadResultValidating]

  lazy val testVerificationFailedRejectedResponse: GetUploadResultVeryficationFailed =
    readJson("/test-get-upload-result-verification-failed-community-buildings-rejected.json")
      .as[GetUploadResultVeryficationFailed]

  lazy val testVerificationFailedQuarantineResponse: GetUploadResultVeryficationFailed =
    readJson("/test-get-upload-result-verification-failed-community-buildings-quarantine.json")
      .as[GetUploadResultVeryficationFailed]

  lazy val testVerificationFailedUnknownResponse: GetUploadResultVeryficationFailed =
    readJson("/test-get-upload-result-verification-failed-community-buildings-unknown-error.json")
      .as[GetUploadResultVeryficationFailed]

  "CommunityBuildingsScheduleUploadStatusControllerSpec" - {

    "status" - {
      "unsubmitted Claim ID is not defined" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingDonationsCollectedInCommunityBuildings(true, Some(true))
          .and(RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CommunityBuildingsScheduleUploadStatusController.status.url)

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      "unsubmitted Claim ID is defined and file reference is not defined" in {
        val sessionData =
          completeRepaymentDetailsAnswersSession
            .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, Some(true)))
            .and(RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true))
            .copy(communityBuildingsScheduleFileUploadReference = None)

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationService].toInstance(mockService))
          .build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CommunityBuildingsScheduleUploadStatusController.status.url)

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = Awaiting (other) - display the screen" in {

        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, Some(true)))
          .and(RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true))
          .copy(communityBuildingsScheduleFileUploadReference = Some(fileUploadReference))

        (mockService
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testAwaitingResponse))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationService].toInstance(mockService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CommunityBuildingsScheduleUploadStatusController.status.url)

          val result = route(application, request).value
          status(result) shouldEqual OK
          contentAsJson(result) shouldEqual Json.obj(
            "uploadStatus" -> JsString("Uploading"),
            "isFinal"      -> JsBoolean(false)
          )

        }
      }

      "unsubmitted Claim ID & file reference are defined - result = Verifying" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, Some(true)))
          .and(RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true))
          .copy(communityBuildingsScheduleFileUploadReference = Some(fileUploadReference))

        (mockService
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testVerifyingResponse))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationService].toInstance(mockService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CommunityBuildingsScheduleUploadStatusController.status.url)

          val result = route(application, request).value
          status(result) shouldEqual OK
          contentAsJson(result) shouldEqual Json.obj(
            "uploadStatus" -> JsString("Uploading"),
            "isFinal"      -> JsBoolean(false)
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = Validating" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, Some(true)))
          .and(RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true))
          .copy(communityBuildingsScheduleFileUploadReference = Some(fileUploadReference))

        (mockService
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testValidatingResponse))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationService].toInstance(mockService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CommunityBuildingsScheduleUploadStatusController.status.url)

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsJson(result) shouldEqual Json.obj(
            "uploadStatus" -> JsString("Uploading"),
            "isFinal"      -> JsBoolean(false)
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = Validated" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, Some(true)))
          .and(RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true))
          .copy(communityBuildingsScheduleFileUploadReference = Some(fileUploadReference))

        (mockService
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testValidatedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationService].toInstance(mockService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CommunityBuildingsScheduleUploadStatusController.status.url)

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsJson(result) shouldEqual Json.obj(
            "uploadStatus" -> JsString("Uploaded"),
            "isFinal"      -> JsBoolean(true)
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = Validation Errors" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, Some(true)))
          .and(RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true))
          .copy(communityBuildingsScheduleFileUploadReference = Some(fileUploadReference))

        (mockService
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationService].toInstance(mockService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CommunityBuildingsScheduleUploadStatusController.status.url)

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsJson(result) shouldEqual Json.obj(
            "uploadStatus" -> JsString("Uploaded"),
            "isFinal"      -> JsBoolean(true)
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = Verification Failed - REJECTED" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, Some(true)))
          .and(RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true))
          .copy(communityBuildingsScheduleFileUploadReference = Some(fileUploadReference))

        (mockService
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testVerificationFailedRejectedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationService].toInstance(mockService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CommunityBuildingsScheduleUploadStatusController.status.url)

          val result = route(application, request).value
          status(result) shouldEqual OK
          contentAsJson(result) shouldEqual Json.obj(
            "uploadStatus" -> JsString("Failed"),
            "isFinal"      -> JsBoolean(true)
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined - recoverWith" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(true, Some(true)))
          .and(RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true))
          .copy(communityBuildingsScheduleFileUploadReference = Some(fileUploadReference))

        (mockService
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.failed(new Exception("CLAIM_REFERENCE_DOES_NOT_EXIST")))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationService].toInstance(mockService))
          .build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CommunityBuildingsScheduleUploadStatusController.status.url)

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }
    }
  }
}

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

import controllers.ControllerSpec
import models.{RepaymentClaimDetailsAnswers, UpscanInitiateRequest}
import play.api.{inject, Application}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.libs.json.Json
import util.HttpV2Support
import uk.gov.hmrc.http.HeaderCarrier
import views.html.YourGiftAidScheduleUploadView
import services.ClaimsValidationService
import models.*
import models.FileUploadReference
import connectors.ClaimsValidationConnector

import scala.concurrent.Future
import util.TestResources
import models.requests.DataRequest

class YourGiftAidScheduleUploadControllerSpec extends ControllerSpec with HttpV2Support {

  given HeaderCarrier = HeaderCarrier()

  val mockConnector: ClaimsValidationConnector = mock[ClaimsValidationConnector]
  val mockService: ClaimsValidationService     = mock[ClaimsValidationService]

  val uploadUrl   = "http://foo.bar.com/upscan-upload-proxy/bucketName"
  val callbackUrl = "http://example.com:1235/charities-claims-validation/claim-1234567890/upscan-callback"

  val upscanInitiateRequest =
    UpscanInitiateRequest(
      successRedirect = "http://foo.bar.com/success",
      errorRedirect = "http://foo.bar.com/error"
    )

//  val response = Json.parse(responseJson).as[UpscanInitiateResponse]

  val claimId             = "test-claim-123"
  val fileUploadReference = FileUploadReference("test-file-upload-reference")

  lazy val testValidationFailedJsonString: String =
    TestResources.readTestResource("/test-get-upload-result-validation-failed-gift-aid.json")

  lazy val testValidatedJsonString: String =
    TestResources.readTestResource("/test-get-upload-result-validated-gift-aid.json")

  lazy val testAwaitingJsonString: String =
    TestResources.readTestResource("/test-get-upload-result-awaiting-upload-gift-aid.json")

  lazy val testVerifyingJsonString: String =
    TestResources.readTestResource("/test-get-upload-result-verifying-upload-gift-aid.json")

  lazy val testValidatingJsonString: String =
    TestResources.readTestResource("/test-get-upload-result-validating-upload-gift-aid.json")

  lazy val testVerificationFailedJsonString: String =
    TestResources.readTestResource("/test-get-upload-result-verification-failed-gift-aid.json")

  // parse JSON into model objects
  lazy val testValidationFailedResponse: GetUploadResultValidationFailedGiftAid =
    Json.parse(testValidationFailedJsonString).as[GetUploadResultValidationFailedGiftAid]

  lazy val testValidatedResponse: GetUploadResultValidatedGiftAid =
    Json.parse(testValidationFailedJsonString).as[GetUploadResultValidatedGiftAid]

  lazy val testAwaitingResponse: GetUploadResultAwaitingUpload =
    Json.parse(testAwaitingJsonString).as[GetUploadResultAwaitingUpload]

  lazy val testVerifyingResponse: GetUploadResultVeryfying =
    Json.parse(testVerifyingJsonString).as[GetUploadResultVeryfying]

  lazy val testValidatingResponse: GetUploadResultValidating =
    Json.parse(testValidatingJsonString).as[GetUploadResultValidating]

  lazy val testVerificationFailedResponse: GetUploadResultVeryficationFailed =
    Json.parse(testVerificationFailedJsonString).as[GetUploadResultVeryficationFailed]

  "YourGiftAidScheduleUploadControllerSpec" - {

    "onPageLoad" - {
      "unsubmitted Claim ID is not defined" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.YourGiftAidScheduleUploadController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.repaymentClaimDetails.routes.RepaymentClaimDetailsController.onPageLoad.url
          )
        }
      }

      "unsubmitted Claim ID is defined and file reference is not defined" in {
        val sessionData =
          RepaymentClaimDetailsAnswers
            .setClaimingGiftAid(true)
            .copy(unsubmittedClaimId = Some(claimId), giftAidScheduleFileUploadReference = None)

        (mockService
          .getFileUploadReference(_: ValidationType, _: Boolean)(using _: DataRequest[?], _: HeaderCarrier))
          .expects(ValidationType.GiftAid, false, *, *)
          .returning(Future.successful(None))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationService].toInstance(mockService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.YourGiftAidScheduleUploadController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadGiftAidScheduleController.onPageLoad.url
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = Awaiting - display the screen" in {

        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingGiftAid(true)
          .copy(
            unsubmittedClaimId = Some(claimId),
            giftAidScheduleFileUploadReference = Some(fileUploadReference)
          )

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testAwaitingResponse))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.YourGiftAidScheduleUploadController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[YourGiftAidScheduleUploadView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(
            claimId,
            testAwaitingResponse,
            None,
            false
          ).body

        }
      }

      "unsubmitted Claim ID & file reference are defined - result = Verifying" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingGiftAid(true)
          .copy(
            unsubmittedClaimId = Some(claimId),
            giftAidScheduleFileUploadReference = Some(fileUploadReference)
          )

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testVerifyingResponse))

        (mockService
          .getFileUploadReference(_: ValidationType, _: Boolean)(using _: DataRequest[?], _: HeaderCarrier))
          .expects(ValidationType.GiftAid, true, *, *)
          .returning(Future.successful(None))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
          .overrides(inject.bind[ClaimsValidationService].toInstance(mockService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.YourGiftAidScheduleUploadController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[YourGiftAidScheduleUploadView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(
            claimId,
            testVerifyingResponse,
            None,
            true
          ).body
        }
      }

//      "unsubmitted Claim ID & file reference are defined - result = Validating" in {
//        val sessionData = RepaymentClaimDetailsAnswers
//          .setClaimingGiftAid(true)
//          .copy(
//            unsubmittedClaimId = Some(claimId),
//            giftAidScheduleFileUploadReference = Some(fileUploadReference)
//          )
//
//        (mockConnector
//          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
//          .expects(claimId, fileUploadReference, *)
//          .returning(Future.successful(testValidatingResponse))
//        (mockService
//          .getFileUploadReference(_: ValidationType, _: Boolean)(using _: DataRequest[?], _: HeaderCarrier))
//          .expects(ValidationType.GiftAid, true, *, *)
//          .returning(Future.successful(None))
//
//        given application: Application = applicationBuilder(sessionData = sessionData)
//          .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
//          .overrides(inject.bind[ClaimsValidationService].toInstance(mockService))
//          .build()
//
//        running(application) {
//          given request: FakeRequest[AnyContentAsEmpty.type] =
//            FakeRequest(GET, routes.YourGiftAidScheduleUploadController.onPageLoad.url)
//
//          val result = route(application, request).value
//          val view   = application.injector.instanceOf[YourGiftAidScheduleUploadView]
//
//          status(result) shouldEqual OK
//          contentAsString(result) shouldEqual view(
//            claimId,
//            testValidatingResponse,
//            None,
//            true
//          ).body
//
//        }
//
//      }
      //      "unsubmitted Claim ID & file reference are defined - result = Verification Failed" in {}
      //      "unsubmitted Claim ID & file reference are defined - result = Validated" in {}
      //      "unsubmitted Claim ID & file reference are defined - result = other" in {}
    }

    "onRemove - delete schedule and redirect" in {

      val sessionData = defaultSessionData.copy(
        unsubmittedClaimId = Some(claimId),
        giftAidScheduleFileUploadReference = Some(fileUploadReference)
      )

      (mockService
        .deleteGiftAidSchedule(using _: DataRequest[?], _: HeaderCarrier))
        .expects(*, *)
        .returning(Future.successful(()))

      given application: Application = applicationBuilder(sessionData = sessionData)
        .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
        .overrides(inject.bind[ClaimsValidationService].toInstance(mockService))
        .build()

      running(application) {
        given request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.YourGiftAidScheduleUploadController.onRemove.url)

        val result = route(application, request).value

        status(result) shouldEqual SEE_OTHER
        redirectLocation(result) shouldEqual Some(
          routes.UploadGiftAidScheduleController.onPageLoad.url
        )
      }
    }

    "onSubmit" - {
      "unsubmitted Claim ID is not defined" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.YourGiftAidScheduleUploadController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.repaymentClaimDetails.routes.RepaymentClaimDetailsController.onPageLoad.url
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined" in {
        val sessionData                = defaultSessionData.copy(unsubmittedClaimId = Some("test-claim-123"))
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.YourGiftAidScheduleUploadController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadGiftAidScheduleController.onPageLoad.url
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = Verification Failed" in {
        val sessionData = defaultSessionData.copy(
          unsubmittedClaimId = Some(claimId),
          giftAidScheduleFileUploadReference = Some(fileUploadReference)
        )

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testVerificationFailedResponse))

        (mockService
          .deleteGiftAidSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
          .overrides(inject.bind[ClaimsValidationService].toInstance(mockService))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.YourGiftAidScheduleUploadController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadGiftAidScheduleController.onPageLoad.url
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = passed Validation" in {
        val sessionData = defaultSessionData.copy(
          unsubmittedClaimId = Some(claimId),
          giftAidScheduleFileUploadReference = Some(fileUploadReference)
        )

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testValidatedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.YourGiftAidScheduleUploadController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.CheckYourGiftAidScheduleController.onPageLoad.url
          )
        }

      }

      "unsubmitted Claim ID & file reference are defined - result = failed Validation" in {
        val sessionData = defaultSessionData.copy(
          unsubmittedClaimId = Some(claimId),
          giftAidScheduleFileUploadReference = Some(fileUploadReference)
        )

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.YourGiftAidScheduleUploadController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ProblemWithGiftAidScheduleController.onPageLoad.url
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = other" in {
        val sessionData = defaultSessionData.copy(
          unsubmittedClaimId = Some(claimId),
          giftAidScheduleFileUploadReference = Some(fileUploadReference)
        )

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testAwaitingResponse))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.YourGiftAidScheduleUploadController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.YourGiftAidScheduleUploadController.onPageLoad.url
          )
        }
      }
    }
  }
}

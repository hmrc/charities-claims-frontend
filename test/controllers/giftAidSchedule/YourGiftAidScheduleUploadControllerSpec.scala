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
import models.RepaymentClaimDetailsAnswers
import play.api.{inject, Application}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import views.html.YourGiftAidScheduleUploadView
import services.ClaimsValidationService
import models.*
import models.FileUploadReference
import connectors.ClaimsValidationConnector

import scala.concurrent.Future
import util.TestResources
import models.requests.DataRequest

class YourGiftAidScheduleUploadControllerSpec extends ControllerSpec {

  given HeaderCarrier = HeaderCarrier()

  val mockConnector: ClaimsValidationConnector = mock[ClaimsValidationConnector]
  val mockService: ClaimsValidationService     = mock[ClaimsValidationService]

  private val claimId             = "test-claim-123"
  private val fileUploadReference = FileUploadReference("test-file-upload-reference")

  private def readJson(path: String) = Json.parse(TestResources.readTestResource(path))

  // parse JSON into model objects
  lazy val testValidationFailedResponse: GetUploadResultValidationFailedGiftAid =
    readJson("/test-get-upload-result-validation-failed-gift-aid.json").as[GetUploadResultValidationFailedGiftAid]

  lazy val testValidatedResponse: GetUploadResultValidatedGiftAid =
    readJson("/test-get-upload-result-validated-gift-aid.json").as[GetUploadResultValidatedGiftAid]

  lazy val testAwaitingResponse: GetUploadResultAwaitingUpload =
    readJson("/test-get-upload-result-awaiting-upload-gift-aid.json").as[GetUploadResultAwaitingUpload]

  lazy val testVerifyingResponse: GetUploadResultVeryfying =
    readJson("/test-get-upload-result-verifying-gift-aid.json").as[GetUploadResultVeryfying]

  lazy val testValidatingResponse: GetUploadResultValidating =
    readJson("/test-get-upload-result-validating-gift-aid.json").as[GetUploadResultValidating]

  lazy val testVerificationFailedRejectedResponse: GetUploadResultVeryficationFailed =
    readJson("/test-get-upload-result-verification-failed-gift-aid-rejected.json").as[GetUploadResultVeryficationFailed]

  lazy val testVerificationFailedQuarantineResponse: GetUploadResultVeryficationFailed =
    readJson("/test-get-upload-result-verification-failed-gift-aid-quarantine.json")
      .as[GetUploadResultVeryficationFailed]

  "YourGiftAidScheduleUploadControllerSpec" - {

    "onPageLoad" - {
      "unsubmitted Claim ID is not defined" in {
        val sessionData                = RepaymentClaimDetailsAnswers.setClaimingGiftAid(true)
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.YourGiftAidScheduleUploadController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.PageNotFoundController.onPageLoad.url
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
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.YourGiftAidScheduleUploadController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadGiftAidScheduleController.onPageLoad.url
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = Awaiting (other) - display the screen" in {

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
            testVerifyingResponse,
            None,
            true
          ).body
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = Validating" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingGiftAid(true)
          .copy(
            unsubmittedClaimId = Some(claimId),
            giftAidScheduleFileUploadReference = Some(fileUploadReference)
          )

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testValidatingResponse))

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
            testValidatingResponse,
            None,
            true
          ).body
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = Verification Failed - REJECTED" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingGiftAid(true)
          .copy(
            unsubmittedClaimId = Some(claimId),
            giftAidScheduleFileUploadReference = Some(fileUploadReference)
          )

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testVerificationFailedRejectedResponse))

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
            testVerificationFailedRejectedResponse,
            None,
            false
          ).body
        }
      }
      "unsubmitted Claim ID & file reference are defined - recoverWith" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingGiftAid(true)
          .copy(
            unsubmittedClaimId = Some(claimId),
            giftAidScheduleFileUploadReference = Some(fileUploadReference)
          )

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.failed(new Exception("CLAIM_REFERENCE_DOES_NOT_EXIST")))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
          .build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.YourGiftAidScheduleUploadController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadGiftAidScheduleController.onPageLoad.url
          )
        }
      }
    }

    "onRemove - delete schedule and redirect" in {

      val sessionData = RepaymentClaimDetailsAnswers
        .setClaimingGiftAid(true)
        .copy(
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
        val request: FakeRequest[AnyContentAsEmpty.type] =
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
        val sessionData                = RepaymentClaimDetailsAnswers
          .setClaimingGiftAid(true)
          .copy(unsubmittedClaimId = None)
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.YourGiftAidScheduleUploadController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.PageNotFoundController.onPageLoad.url
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined" in {
        val sessionData                = RepaymentClaimDetailsAnswers
          .setClaimingGiftAid(true)
          .copy(unsubmittedClaimId = Some("test-claim-123"))
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.YourGiftAidScheduleUploadController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadGiftAidScheduleController.onPageLoad.url
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = Verification Failed - REJECTED" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingGiftAid(true)
          .copy(
            unsubmittedClaimId = Some(claimId),
            giftAidScheduleFileUploadReference = Some(fileUploadReference)
          )

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testVerificationFailedRejectedResponse))

        (mockService
          .deleteGiftAidSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
          .overrides(inject.bind[ClaimsValidationService].toInstance(mockService))
          .build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.YourGiftAidScheduleUploadController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ProblemUpdatingGiftAidScheduleRejectedController.onPageLoad.url
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = Verification Failed - QUARANTINE" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingGiftAid(true)
          .copy(
            unsubmittedClaimId = Some(claimId),
            giftAidScheduleFileUploadReference = Some(fileUploadReference)
          )

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testVerificationFailedQuarantineResponse))

        (mockService
          .deleteGiftAidSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
          .overrides(inject.bind[ClaimsValidationService].toInstance(mockService))
          .build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.YourGiftAidScheduleUploadController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ProblemUpdatingGiftAidScheduleQuarantineController.onPageLoad.url
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = passed Validation" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingGiftAid(true)
          .copy(
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
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.YourGiftAidScheduleUploadController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.CheckYourGiftAidScheduleController.onPageLoad.url
          )
        }

      }

      "unsubmitted Claim ID & file reference are defined - result = failed Validation" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingGiftAid(true)
          .copy(
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
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.YourGiftAidScheduleUploadController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ProblemWithGiftAidScheduleController.onPageLoad.url
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = other" in {
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
          val request: FakeRequest[AnyContentAsEmpty.type] =
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

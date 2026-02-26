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
import models.*
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.{inject, Application}
import services.ClaimsValidationService
import uk.gov.hmrc.http.HeaderCarrier
import util.TestResources
import views.html.YourConnectedCharitiesScheduleUploadView

import scala.concurrent.Future

class YourConnectedCharitiesScheduleUploadControllerSpec extends ControllerSpec {

  given HeaderCarrier = HeaderCarrier()

  val mockConnector: ClaimsValidationConnector = mock[ClaimsValidationConnector]
  val mockService: ClaimsValidationService     = mock[ClaimsValidationService]

  private val claimId                                  = "test-claim-id"
  private val fileUploadReference: FileUploadReference = FileUploadReference("test-file-upload-reference")

  private def readJson(path: String) = Json.parse(TestResources.readTestResource(path))

  // parse JSON into model objects
  lazy val testValidationFailedResponse: GetUploadResultValidationFailedConnectedCharities =
    readJson("/test-get-upload-result-validation-failed-connected-charities.json")
      .as[GetUploadResultValidationFailedConnectedCharities]

  lazy val testValidatedResponse: GetUploadResultValidatedConnectedCharities =
    readJson("/test-get-upload-result-validated-connected-charities.json")
      .as[GetUploadResultValidatedConnectedCharities]

  lazy val testAwaitingResponse: GetUploadResultAwaitingUpload =
    readJson("/test-get-upload-result-awaiting-upload-connected-charities.json")
      .as[GetUploadResultAwaitingUpload]

  lazy val testVerifyingResponse: GetUploadResultVeryfying =
    readJson("/test-get-upload-result-verifying-connected-charities.json")
      .as[GetUploadResultVeryfying]

  lazy val testValidatingResponse: GetUploadResultValidating =
    readJson("/test-get-upload-result-validating-connected-charities.json")
      .as[GetUploadResultValidating]

  lazy val testVerificationFailedResponse: GetUploadResultVeryficationFailed =
    readJson("/test-get-upload-result-verification-failed-connected-charities.json")
      .as[GetUploadResultVeryficationFailed]

  lazy val testVerificationFailedQuarantineResponse: GetUploadResultVeryficationFailed =
    readJson("/test-get-upload-result-verification-failed-connected-charities-quarantine.json")
      .as[GetUploadResultVeryficationFailed]

  lazy val testVerificationFailedRejectedResponse: GetUploadResultVeryficationFailed =
    readJson("/test-get-upload-result-verification-failed-connected-charities-rejected.json")
      .as[GetUploadResultVeryficationFailed]

  lazy val testVerificationFailedUnknownResponse: GetUploadResultVeryficationFailed =
    readJson("/test-get-upload-result-verification-failed-connected-charities-unknown-error.json")
      .as[GetUploadResultVeryficationFailed]

  private def session(
    fileRef: Option[FileUploadReference] = None,
    claimingTax: Boolean = true
  ) =
    completeRepaymentDetailsAnswersSession
      .and(RepaymentClaimDetailsAnswers.setClaimingConnectedCharities(claimingTax))
      .copy(connectedCharitiesScheduleFileUploadReference = fileRef)

  "YourConnectedCharitiesScheduleUploadControllerSpec" - {

    "onPageLoad" - {
      "unsubmitted Claim ID is not defined" in {
        val sessionData =
          RepaymentClaimDetailsAnswers
            .setClaimingConnectedCharities(true)
            .copy(unsubmittedClaimId = None)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.YourConnectedCharitiesScheduleUploadController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "unsubmitted Claim ID is defined and file reference is not defined" in {
        val sessionData = session()

        (mockService
          .getFileUploadReference(_: ValidationType, _: Boolean)(using _: DataRequest[?], _: HeaderCarrier))
          .expects(ValidationType.ConnectedCharities, false, *, *)
          .returning(Future.successful(None))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationService].toInstance(mockService))
          .build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.YourConnectedCharitiesScheduleUploadController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadConnectedCharitiesScheduleController.onPageLoad.url
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = Awaiting (other) - display the screen" in {
        val sessionData = session(fileRef = Some(fileUploadReference))

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testAwaitingResponse))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.YourConnectedCharitiesScheduleUploadController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[YourConnectedCharitiesScheduleUploadView]

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
        val sessionData = session(fileRef = Some(fileUploadReference))

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testVerifyingResponse))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.YourConnectedCharitiesScheduleUploadController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[YourConnectedCharitiesScheduleUploadView]

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
        val sessionData = session(fileRef = Some(fileUploadReference))

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testValidatingResponse))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.YourConnectedCharitiesScheduleUploadController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[YourConnectedCharitiesScheduleUploadView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(
            claimId,
            testValidatingResponse,
            None,
            true
          ).body
        }
      }
      "unsubmitted Claim ID & file reference are defined - result = Verification Failed" in {
        val sessionData = session(fileRef = Some(fileUploadReference))

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testVerificationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.YourConnectedCharitiesScheduleUploadController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[YourConnectedCharitiesScheduleUploadView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(
            claimId,
            testVerificationFailedResponse,
            None,
            false
          ).body
        }
      }
      "unsubmitted Claim ID & file reference are defined - recoverWith" in {
        val sessionData = session(fileRef = Some(fileUploadReference))

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.failed(new Exception("CLAIM_REFERENCE_DOES_NOT_EXIST")))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
          .build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.YourConnectedCharitiesScheduleUploadController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadConnectedCharitiesScheduleController.onPageLoad.url
          )
        }
      }
    }

    "onRemove - delete schedule and redirect" in {
      val sessionData = completeRepaymentDetailsAnswersSession
        .and(RepaymentClaimDetailsAnswers.setClaimingConnectedCharities(true))
        .copy(connectedCharitiesScheduleFileUploadReference = Some(fileUploadReference))

      (mockService
        .deleteConnectedCharitiesSchedule(using _: DataRequest[?], _: HeaderCarrier))
        .expects(*, *)
        .returning(Future.successful(()))

      given application: Application = applicationBuilder(sessionData = sessionData)
        .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
        .overrides(inject.bind[ClaimsValidationService].toInstance(mockService))
        .build()

      running(application) {
        val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.YourConnectedCharitiesScheduleUploadController.onRemove.url)

        val result = route(application, request).value

        status(result) shouldEqual SEE_OTHER
        redirectLocation(result) shouldEqual Some(
          routes.UploadConnectedCharitiesScheduleController.onPageLoad.url
        )
      }
    }

    "onSubmit" - {
      "unsubmitted Claim ID is not defined" in {
        val sessionData =
          RepaymentClaimDetailsAnswers
            .setClaimingConnectedCharities(true)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.YourConnectedCharitiesScheduleUploadController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined" in {
        val sessionData =
          completeRepaymentDetailsAnswersSession
            .and(RepaymentClaimDetailsAnswers.setClaimingConnectedCharities(true))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.YourConnectedCharitiesScheduleUploadController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadConnectedCharitiesScheduleController.onPageLoad.url
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = Verification Failed - QUARANTINE" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingConnectedCharities(true))
          .copy(connectedCharitiesScheduleFileUploadReference = Some(fileUploadReference))

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testVerificationFailedQuarantineResponse))

        (mockService
          .deleteConnectedCharitiesSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
          .overrides(inject.bind[ClaimsValidationService].toInstance(mockService))
          .build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.YourConnectedCharitiesScheduleUploadController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadConnectedCharitiesScheduleController.onPageLoad.url
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = Verification Failed - REJECTED" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingConnectedCharities(true))
          .copy(connectedCharitiesScheduleFileUploadReference = Some(fileUploadReference))

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testVerificationFailedRejectedResponse))

        (mockService
          .deleteConnectedCharitiesSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
          .overrides(inject.bind[ClaimsValidationService].toInstance(mockService))
          .build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.YourConnectedCharitiesScheduleUploadController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ProblemUpdatingConnectedCharitiesScheduleRejectedController.onPageLoad.url
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = Verification Failed - UNKNOWN" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingConnectedCharities(true))
          .copy(connectedCharitiesScheduleFileUploadReference = Some(fileUploadReference))

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testVerificationFailedUnknownResponse))

        (mockService
          .deleteConnectedCharitiesSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
          .overrides(inject.bind[ClaimsValidationService].toInstance(mockService))
          .build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.YourConnectedCharitiesScheduleUploadController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadConnectedCharitiesScheduleController.onPageLoad.url
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = passed Validation" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingConnectedCharities(true))
          .copy(connectedCharitiesScheduleFileUploadReference = Some(fileUploadReference))

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testValidatedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
          .build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.YourConnectedCharitiesScheduleUploadController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.CheckYourConnectedCharitiesScheduleController.onPageLoad.url
          )
        }

      }

      "unsubmitted Claim ID & file reference are defined - result = failed Validation" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingConnectedCharities(true))
          .copy(connectedCharitiesScheduleFileUploadReference = Some(fileUploadReference))

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testValidationFailedResponse))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
          .build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.YourConnectedCharitiesScheduleUploadController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ProblemWithConnectedCharitiesScheduleController.onPageLoad.url
          )
        }
      }

      "unsubmitted Claim ID & file reference are defined - result = other" in {
        val sessionData = completeRepaymentDetailsAnswersSession
          .and(RepaymentClaimDetailsAnswers.setClaimingConnectedCharities(true))
          .copy(connectedCharitiesScheduleFileUploadReference = Some(fileUploadReference))

        (mockConnector
          .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
          .expects(claimId, fileUploadReference, *)
          .returning(Future.successful(testAwaitingResponse))

        given application: Application = applicationBuilder(sessionData = sessionData)
          .overrides(inject.bind[ClaimsValidationConnector].toInstance(mockConnector))
          .build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.YourConnectedCharitiesScheduleUploadController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.YourConnectedCharitiesScheduleUploadController.onPageLoad.url
          )
        }
      }
    }
  }
}

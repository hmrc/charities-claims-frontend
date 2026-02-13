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

import com.typesafe.config.ConfigFactory
import connectors.UpscanInitiateConnector
import controllers.otherIncomeSchedule.routes
import controllers.ControllerSpec
import models.requests.DataRequest
import models.{
  CreateUploadTrackingRequest,
  FileUploadReference,
  RepaymentClaimDetailsAnswers,
  SessionData,
  UpscanInitiateRequest,
  UpscanInitiateResponse,
  ValidationType
}
import play.api.{inject, Application, Configuration}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.libs.json.Json
import util.HttpV2Support
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import org.scalamock.handlers.CallHandler
import play.api.inject.guice.GuiceableModule
import services.{ClaimsValidationService, SaveService}

import scala.concurrent.Future

class UploadOtherIncomeScheduleControllerSpec extends ControllerSpec with HttpV2Support {
  val config: Configuration = Configuration(
    ConfigFactory.parseString(
      """
        |  microservice {
        |    services {
        |      upscan-initiate {
        |        protocol = http
        |        host     = foo.bar.com
        |        port     = 1234
        |        retryIntervals = [10ms,50ms]
        |        context-path = "/foo-upscan"
        |        service-name = "foo-bar"
        |      }
        |      charities-claims-validation {
        |        protocol = http
        |        host     = example.com
        |        port     = 1235
        |        context-path = "/charities-claims-validation"
        |      }
        |   }
        |}
        |""".stripMargin
    )
  )

  def givenPostInitiateEndpointReturns(
    request: UpscanInitiateRequest,
    response: HttpResponse
  ): CallHandler[Future[HttpResponse]] =
    mockHttpPostSuccess(
      url = "http://foo.bar.com:1234/foo-upscan/v2/initiate",
      requestBody = Json.toJson(request),
      hasHeaders = false
    )(response)

  given HeaderCarrier = HeaderCarrier()

  val uploadUrl   = "http://foo.bar.com/upscan-upload-proxy/bucketName"
  val callbackUrl = "http://example.com:1235/charities-claims-validation/claim-1234567890/upscan-callback"

  val upscanInitiateRequest =
    UpscanInitiateRequest(
      successRedirect = "http://foo.bar.com/success",
      errorRedirect = "http://foo.bar.com/error"
    )

  val expectedUpscanInitiateRequest =
    upscanInitiateRequest.copy(
      consumingService = Some("foo-bar"),
      callbackUrl = Some(callbackUrl)
    )

  val responseJson =
    s"""{
          "reference": "11370e18-6e24-453e-b45a-76d3e32ea33d",
          "uploadRequest": {
              "href": "$uploadUrl",
              "fields": {
                  "Content-Type": "application/xml",
                  "acl": "private",
                  "key": "xxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
                  "policy": "xxxxxxxx==",
                  "x-amz-algorithm": "AWS4-HMAC-SHA256",
                  "x-amz-credential": "ASIAxxxxxxxxx/20180202/eu-west-2/s3/aws4_request",
                  "x-amz-date": "yyyyMMddThhmmssZ",
                  "x-amz-meta-callback-url": "$callbackUrl",
                  "x-amz-signature": "xxxx",
                  "success_action_redirect": "http://foo.bar.com/success",
                  "error_action_redirect": "http://foo.bar.com/error"
              }
          }
        }""".stripMargin

  val response = Json.parse(responseJson).as[UpscanInitiateResponse]

  val mockClaimsValidationService: ClaimsValidationService = mock[ClaimsValidationService]
  val mockUpscanInitiateConnector: UpscanInitiateConnector = mock[UpscanInitiateConnector]
  val mockSaveService: SaveService                         = mock[SaveService]

  "UploadOtherIncomeScheduleController" - {

    "onPageLoad" - {

      "should render Page Not Found if setClaimingTaxDeducted is false" in {
        val sessionData  = RepaymentClaimDetailsAnswers.setClaimingTaxDeducted(false)
        val customConfig = Map(
          "urls.giftAidScheduleSpreadsheetsToClaimBackTaxOnDonationsUrl" -> "https://test.example.com/charity-repayment-claim"
        )

        given application: Application = applicationBuilder(sessionData = sessionData).configure(customConfig).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.UploadOtherIncomeScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.repaymentClaimDetails.routes.RepaymentClaimDetailsController.onPageLoad.url
          )
        }
      }

      "should render Page Not Found if setClaimingTaxDeducted is true && unsubmittedClaimId is None" in {
        val sessionData  = RepaymentClaimDetailsAnswers.setClaimingTaxDeducted(true)
        val customConfig = Map(
          "urls.giftAidScheduleSpreadsheetsToClaimBackTaxOnDonationsUrl" -> "https://test.example.com/charity-repayment-claim"
        )

        given application: Application = applicationBuilder(sessionData = sessionData).configure(customConfig).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.UploadOtherIncomeScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.repaymentClaimDetails.routes.RepaymentClaimDetailsController.onPageLoad.url
          )
        }
      }

      "should render page when upscan initiation exists in session" in {

        val upscan = response

        val sessionData =
          RepaymentClaimDetailsAnswers
            .setClaimingTaxDeducted(true)
            .copy(
              unsubmittedClaimId = Some("claim-123"),
              otherIncomeScheduleUpscanInitialization = Some(upscan)
            )

        given application: Application =
          applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request = FakeRequest(GET, routes.UploadOtherIncomeScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include("claim-123")
        }
      }

      "should redirect when file upload reference already exists" in {

        val sessionData =
          RepaymentClaimDetailsAnswers
            .setClaimingTaxDeducted(true)
            .copy(unsubmittedClaimId = Some("claim-123"))

        (mockClaimsValidationService
          .getFileUploadReference(_: ValidationType, _: Boolean)(using _: DataRequest[?], _: HeaderCarrier))
          .expects(ValidationType.OtherIncome, false, *, *)
          .returning(Future.successful(Some(FileUploadReference("ref-123"))))

        given application: Application =
          applicationBuilder(sessionData = sessionData)
            .overrides(
              inject.bind[ClaimsValidationService].toInstance(mockClaimsValidationService)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.UploadOtherIncomeScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual
            Some(routes.YourOtherIncomeScheduleUploadController.onPageLoad.url)
        }
      }

      "should initiate upscan and store session when no reference exists" in {

        val sessionData =
          RepaymentClaimDetailsAnswers
            .setClaimingTaxDeducted(true)
            .copy(unsubmittedClaimId = Some("claim-123"))

        (mockClaimsValidationService
          .getFileUploadReference(_: ValidationType, _: Boolean)(using _: DataRequest[?], _: HeaderCarrier))
          .expects(ValidationType.OtherIncome, false, *, *)
          .returning(Future.successful(None))

        (mockUpscanInitiateConnector
          .initiate(_: String, _: UpscanInitiateRequest)(using _: HeaderCarrier))
          .expects(*, *, *)
          .returning(Future.successful(response))

        (mockClaimsValidationService
          .createUploadTracking(_: String, _: CreateUploadTrackingRequest)(using _: HeaderCarrier))
          .expects(*, *, *)
          .returning(Future.successful(true))

        (mockSaveService
          .save(_: SessionData)(using _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        given application: Application =
          applicationBuilder(sessionData = sessionData)
            .overrides(
              inject.bind[ClaimsValidationService].toInstance(mockClaimsValidationService),
              inject.bind[UpscanInitiateConnector].toInstance(mockUpscanInitiateConnector),
              inject.bind[SaveService].toInstance(mockSaveService)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.UploadOtherIncomeScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual OK
        }
      }
    }

    "onUploadSuccess" - {

      "should update status and redirect when reference exists" in {

        val sessionData =
          RepaymentClaimDetailsAnswers
            .setClaimingTaxDeducted(true)
            .copy(unsubmittedClaimId = Some("claim-123"))

        (mockClaimsValidationService
          .getFileUploadReference(_: ValidationType, _: Boolean)(using _: DataRequest[?], _: HeaderCarrier))
          .expects(ValidationType.OtherIncome, true, *, *)
          .returning(Future.successful(Some(FileUploadReference("ref-123"))))

        (mockClaimsValidationService
          .updateUploadStatus(_: String, _: FileUploadReference)(using _: DataRequest[?], _: HeaderCarrier))
          .expects("claim-123", FileUploadReference("ref-123"), *, *)
          .returning(Future.successful(true))

        given application: Application =
          applicationBuilder(sessionData = sessionData)
            .overrides(
              inject.bind[ClaimsValidationService].toInstance(mockClaimsValidationService)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.UploadOtherIncomeScheduleController.onUploadSuccess.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual
            Some(routes.YourOtherIncomeScheduleUploadController.onPageLoad.url)
        }
      }

      "should redirect back when no reference found" in {

        val sessionData =
          RepaymentClaimDetailsAnswers
            .setClaimingTaxDeducted(true)
            .copy(unsubmittedClaimId = Some("claim-123"))

        (mockClaimsValidationService
          .getFileUploadReference(_: ValidationType, _: Boolean)(using _: DataRequest[?], _: HeaderCarrier))
          .expects(ValidationType.OtherIncome, true, *, *)
          .returning(Future.successful(None))

        given application: Application =
          applicationBuilder(sessionData = sessionData)
            .overrides(
              inject.bind[ClaimsValidationService].toInstance(mockClaimsValidationService)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.UploadOtherIncomeScheduleController.onUploadSuccess.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual
            Some(routes.UploadOtherIncomeScheduleController.onPageLoad.url)
        }
      }

    }

    "onUploadError" - {

      "should redirect when reference exists" in {

        val sessionData =
          RepaymentClaimDetailsAnswers
            .setClaimingTaxDeducted(true)
            .copy(unsubmittedClaimId = Some("claim-123"))

        (mockClaimsValidationService
          .getFileUploadReference(_: ValidationType, _: Boolean)(using _: DataRequest[?], _: HeaderCarrier))
          .expects(ValidationType.OtherIncome, true, *, *)
          .returning(Future.successful(Some(FileUploadReference("ref-123"))))

        given application: Application =
          applicationBuilder(sessionData = sessionData)
            .overrides(
              inject.bind[ClaimsValidationService].toInstance(mockClaimsValidationService)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.UploadOtherIncomeScheduleController.onUploadError.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual
            Some(routes.YourOtherIncomeScheduleUploadController.onPageLoad.url)
        }
      }

      "should render error page when upscan exists in session" in {

        val sessionData =
          RepaymentClaimDetailsAnswers
            .setClaimingTaxDeducted(true)
            .copy(
              unsubmittedClaimId = Some("claim-123"),
              otherIncomeScheduleUpscanInitialization = Some(response)
            )

        (mockClaimsValidationService
          .getFileUploadReference(_: ValidationType, _: Boolean)(using _: DataRequest[?], _: HeaderCarrier))
          .expects(ValidationType.OtherIncome, true, *, *)
          .returning(Future.successful(None))

        given application: Application =
          applicationBuilder(sessionData = sessionData)
            .overrides(
              inject.bind[ClaimsValidationService].toInstance(mockClaimsValidationService)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.UploadOtherIncomeScheduleController.onUploadError.url)

          val result = route(application, request).value

          status(result) shouldEqual OK
        }
      }

      "should redirect when no upscan and no reference" in {

        val sessionData =
          RepaymentClaimDetailsAnswers
            .setClaimingTaxDeducted(true)
            .copy(unsubmittedClaimId = Some("claim-123"))

        (mockClaimsValidationService
          .getFileUploadReference(_: ValidationType, _: Boolean)(using _: DataRequest[?], _: HeaderCarrier))
          .expects(ValidationType.OtherIncome, true, *, *)
          .returning(Future.successful(None))

        given application: Application =
          applicationBuilder(sessionData = sessionData)
            .overrides(
              inject.bind[ClaimsValidationService].toInstance(mockClaimsValidationService)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.UploadOtherIncomeScheduleController.onUploadError.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual
            Some(routes.UploadOtherIncomeScheduleController.onPageLoad.url)
        }
      }

    }

  }

}

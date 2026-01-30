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

package connectors

import util.{BaseSpec, HttpV2Support}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import models.*
import org.scalamock.handlers.CallHandler
import play.api.test.Helpers.*
import com.typesafe.config.ConfigFactory
import play.api.Configuration
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

import java.net.URL
import util.TestResources

class ClaimsValidationConnectorSpec extends BaseSpec with HttpV2Support {

  val config: Configuration = Configuration(
    ConfigFactory.parseString(
      """
        |  microservice {
        |    services {
        |      charities-claims-validation {
        |        protocol = http
        |        host     = example.com
        |        port     = 1234
        |        retryIntervals = [10ms,50ms]
        |        context-path = "/charities-claims-validation"
        |      }
        |   }
        |}
        |""".stripMargin
    )
  )

  val connector =
    new ClaimsValidationConnectorImpl(
      http = mockHttp,
      servicesConfig = new ServicesConfig(config),
      configuration = config,
      actorSystem = actorSystem
    )

  val testUploadSummaryResponse = GetUploadSummaryResponse(
    uploads = Seq(
      UploadSummary(
        reference = FileUploadReference("f5da5578-8393-4cd1-be0e-d8ef1b78d8e7"),
        validationType = ValidationType.GiftAid,
        fileStatus = FileStatus.VALIDATED,
        uploadUrl = None
      ),
      UploadSummary(
        reference = FileUploadReference("501beba6-fb65-4952-93fc-f83be323fde6"),
        validationType = ValidationType.OtherIncome,
        fileStatus = FileStatus.VALIDATING,
        uploadUrl = None
      )
    )
  )

  val testUploadSummaryResponseJsonString: String = Json.stringify(Json.toJson(testUploadSummaryResponse))

  lazy val testGetUploadResultValidatedGiftAidJsonString: String =
    TestResources.readTestResource("/test-get-upload-result-validated-gift-aid.json")

  lazy val testGetUploadResultAwaitingUploadJsonString: String =
    TestResources.readTestResource("/test-get-upload-result-awaiting-upload.json")

  def givenCreateUploadTrackingEndpointReturns(
    request: CreateUploadTrackingRequest,
    response: HttpResponse
  ): CallHandler[Future[HttpResponse]] =
    mockHttpPostSuccess(
      url = "http://example.com:1234/charities-claims-validation/123/create-upload-tracking",
      requestBody = Json.toJson(request),
      hasHeaders = false
    )(response)

  def givenGetUploadSummaryEndpointReturns(response: HttpResponse): CallHandler[Future[HttpResponse]] =
    mockHttpGetSuccess(
      URL("http://example.com:1234/charities-claims-validation/123/upload-results")
    )(response)

  def givenGetUploadResultEndpointReturns(response: HttpResponse): CallHandler[Future[HttpResponse]] =
    mockHttpGetSuccess(
      URL("http://example.com:1234/charities-claims-validation/123/upload-results/file-upload-reference-123")
    )(response)

  def givenDeleteScheduleEndpointReturns(response: HttpResponse): CallHandler[Future[HttpResponse]] =
    mockHttpDeleteSuccess(
      "http://example.com:1234/charities-claims-validation/123/upload-results/ref-123"
    )(response)

  given HeaderCarrier = HeaderCarrier()

  "ClaimsValidationConnector" - {

    "createUpoloadTracking" - {

      val createUploadTrackingRequest = CreateUploadTrackingRequest(
        reference = UpscanReference("reference-123"),
        validationType = ValidationType.GiftAid,
        uploadUrl = "upload-url-123",
        initateTimestamp = "initate-timestamp-123"
      )

      "should return true when service returns 200 status" in {
        givenCreateUploadTrackingEndpointReturns(
          createUploadTrackingRequest,
          HttpResponse(200, Json.stringify(Json.toJson(SuccessResponse(success = true))))
        ).once()
        await(connector.createUpoloadTracking("123", createUploadTrackingRequest)) should be(true)
      }

      "should return false when service returns 200 status" in {
        givenCreateUploadTrackingEndpointReturns(
          createUploadTrackingRequest,
          HttpResponse(200, Json.stringify(Json.toJson(SuccessResponse(success = false))))
        ).once()
        await(connector.createUpoloadTracking("123", createUploadTrackingRequest)) should be(false)
      }
    }

    "getUploadSummary" - {
      "have retries defined" in {
        connector.retryIntervals shouldBe Seq(FiniteDuration(10, "ms"), FiniteDuration(50, "ms"))
      }

      "should return upload summary when service returns 200 status" in {
        givenGetUploadSummaryEndpointReturns(HttpResponse(200, testUploadSummaryResponseJsonString)).once()

        await(connector.getUploadSummary("123")) shouldEqual testUploadSummaryResponse
      }

      "throw an exception if the service returns malformed JSON" in {
        givenGetUploadSummaryEndpointReturns(HttpResponse(200, "{\"invalid\": \"json\"}")).once()

        a[Exception] should be thrownBy {
          await(connector.getUploadSummary("123"))
        }
      }

      "throw an exception if the service returns wrong entity format" in {
        givenGetUploadSummaryEndpointReturns(HttpResponse(200, "{\"uploads\": \"not-an-array\"}")).once()

        a[Exception] should be thrownBy {
          await(connector.getUploadSummary("123"))
        }
      }

      "throw an exception if the service returns 404 status" in {
        givenGetUploadSummaryEndpointReturns(HttpResponse(404, "")).once()

        a[Exception] should be thrownBy {
          await(connector.getUploadSummary("123"))
        }
      }

      "throw an exception if the service returns 500 status" in {
        givenGetUploadSummaryEndpointReturns(HttpResponse(500, "")).once()

        a[Exception] should be thrownBy {
          await(connector.getUploadSummary("123"))
        }
      }

      "throw exception when 5xx response status in the third attempt" in {
        givenGetUploadSummaryEndpointReturns(HttpResponse(500, "")).once()
        givenGetUploadSummaryEndpointReturns(HttpResponse(499, "")).once()
        givenGetUploadSummaryEndpointReturns(HttpResponse(469, "")).once()

        a[Exception] shouldBe thrownBy {
          await(connector.getUploadSummary("123"))
        }
      }

      "accept valid response in a second attempt" in {
        givenGetUploadSummaryEndpointReturns(HttpResponse(500, "")).once()
        givenGetUploadSummaryEndpointReturns(HttpResponse(200, testUploadSummaryResponseJsonString)).once()

        await(connector.getUploadSummary("123")) shouldEqual testUploadSummaryResponse
      }

      "accept valid response in a third attempt" in {
        givenGetUploadSummaryEndpointReturns(HttpResponse(499, "")).once()
        givenGetUploadSummaryEndpointReturns(HttpResponse(500, "")).once()
        givenGetUploadSummaryEndpointReturns(HttpResponse(200, testUploadSummaryResponseJsonString)).once()

        await(connector.getUploadSummary("123")) shouldEqual testUploadSummaryResponse
      }
    }

    "getUploadResult" - {

      "should return upload result when service returns 200 status with validated gift aid" in {
        givenGetUploadResultEndpointReturns(HttpResponse(200, testGetUploadResultValidatedGiftAidJsonString)).once()
        await(connector.getUploadResult("123", FileUploadReference("file-upload-reference-123"))) should be(
          a[GetUploadResultValidatedGiftAid]
        )
      }

      "should return upload result when service returns 200 status with awaiting upload" in {
        givenGetUploadResultEndpointReturns(HttpResponse(200, testGetUploadResultValidatedGiftAidJsonString)).once()
        await(connector.getUploadResult("123", FileUploadReference("file-upload-reference-123"))) should be(
          a[GetUploadResultValidatedGiftAid]
        )
      }
    }

    "deleteSchedule" - {

      "should return DeleteScheduleResponse with success=true when deletion is successful" in {
        val successResponse = DeleteScheduleResponse(success = true)
        givenDeleteScheduleEndpointReturns(
          HttpResponse(200, Json.stringify(Json.toJson(successResponse)))
        ).once()

        await(connector.deleteSchedule("123", FileUploadReference("ref-123"))) shouldBe successResponse
      }

      "should throw exception when deletion returns success=false" in {
        val failureResponse = DeleteScheduleResponse(success = false)
        givenDeleteScheduleEndpointReturns(
          HttpResponse(200, Json.stringify(Json.toJson(failureResponse)))
        ).once()

        a[Exception] should be thrownBy {
          await(connector.deleteSchedule("123", FileUploadReference("ref-123")))
        }
      }

      "throw an exception if the service returns malformed JSON" in {
        givenDeleteScheduleEndpointReturns(HttpResponse(200, "{\"invalid\"}")).once()

        a[Exception] should be thrownBy {
          await(connector.deleteSchedule("123", FileUploadReference("ref-123")))
        }
      }

      "throw an exception if the service returns 404 status" in {
        givenDeleteScheduleEndpointReturns(HttpResponse(404, "")).once()

        a[Exception] should be thrownBy {
          await(connector.deleteSchedule("123", FileUploadReference("ref-123")))
        }
      }

      "throw an exception if the service returns 500 status" in {
        givenDeleteScheduleEndpointReturns(HttpResponse(500, "")).once()

        a[Exception] should be thrownBy {
          await(connector.deleteSchedule("123", FileUploadReference("ref-123")))
        }
      }
    }

    // Note: The POST/PUT and JSON payload handling in ClaimsValidationConnector
    // are not covered by tests because current methods (getUploadSummary, deleteSchedule) only use
    // GET and DELETE without payloads. The POST/PUT and payload are part of the reusable callValidationBackend.
    // Tests will be added to cover those scenarios in the future.
  }
}

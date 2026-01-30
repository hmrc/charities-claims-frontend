/*
 * Copyright 2025 HM Revenue & Customs
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
import models.{UpscanInitiateRequest, UpscanInitiateResponse}
import org.scalamock.handlers.CallHandler
import play.api.test.Helpers.*
import com.typesafe.config.ConfigFactory
import play.api.Configuration
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

class UpscanInitiateConnectorSpec extends BaseSpec with HttpV2Support {

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

  val connector =
    new UpscanInitiateConnectorImpl(
      http = mockHttp,
      servicesConfig = new ServicesConfig(config),
      configuration = config,
      actorSystem = actorSystem
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

  "UpscanInitiateConnector" - {
    "initiate" - {
      "have retries defined" in {
        connector.retryIntervals shouldBe Seq(FiniteDuration(10, "ms"), FiniteDuration(50, "ms"))
      }

      "should return a upscan initiate response if the service returns 200 status" in {
        givenPostInitiateEndpointReturns(expectedUpscanInitiateRequest, HttpResponse(200, responseJson)).once()
        await(connector.initiate("claim-1234567890", upscanInitiateRequest)) shouldBe response
      }

      "should return a failed future if the service returns a non-200 status" in {
        givenPostInitiateEndpointReturns(expectedUpscanInitiateRequest, HttpResponse(400, ""))
        an[Exception] should be thrownBy await(connector.initiate("claim-1234567890", upscanInitiateRequest))
      }

      "throw exception when 5xx response status in the third attempt" in {
        givenPostInitiateEndpointReturns(expectedUpscanInitiateRequest, HttpResponse(500, ""))
        givenPostInitiateEndpointReturns(expectedUpscanInitiateRequest, HttpResponse(500, ""))
        givenPostInitiateEndpointReturns(expectedUpscanInitiateRequest, HttpResponse(400, ""))
        a[Exception] shouldBe thrownBy {
          await(connector.initiate("claim-1234567890", upscanInitiateRequest))
        }
      }

      "throw an exception when the service returns a malformed JSON" in {
        givenPostInitiateEndpointReturns(expectedUpscanInitiateRequest, HttpResponse(200, "{invalidJson}"))
        a[Exception] shouldBe thrownBy {
          await(connector.initiate("claim-1234567890", upscanInitiateRequest))
        }
      }

      "accept valid response in a second attempt" in {
        givenPostInitiateEndpointReturns(expectedUpscanInitiateRequest, HttpResponse(500, ""))
        givenPostInitiateEndpointReturns(expectedUpscanInitiateRequest, HttpResponse(200, responseJson))
        await(connector.initiate("claim-1234567890", upscanInitiateRequest)) shouldBe response
      }

      "accept valid response in a third attempt" in {
        givenPostInitiateEndpointReturns(expectedUpscanInitiateRequest, HttpResponse(500, ""))
        givenPostInitiateEndpointReturns(expectedUpscanInitiateRequest, HttpResponse(500, ""))
        givenPostInitiateEndpointReturns(expectedUpscanInitiateRequest, HttpResponse(200, responseJson))
        await(connector.initiate("claim-1234567890", upscanInitiateRequest)) shouldBe response
      }

    }
  }

}

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

import util.BaseSpec
import util.HttpV2Support
import play.api.Configuration
import com.typesafe.config.ConfigFactory
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import uk.gov.hmrc.http.HttpResponse
import org.scalamock.handlers.CallHandler
import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier
import play.api.libs.json.Json
import models.GetClaimsRequest
import models.GetClaimsResponse
import play.api.test.Helpers.*

class ClaimsConnectorSpec extends BaseSpec with HttpV2Support {

  val config: Configuration = Configuration(
    ConfigFactory.parseString(
      """
        |  microservice {
        |    services {
        |      charities-claims {
        |        protocol = http
        |        host     = foo.bar.com
        |        port     = 1234
        |        retryIntervals = [10ms,50ms]
        |        context-path = "/foo-claims"
        |      }
        |   }
        |}
        |""".stripMargin
    )
  )

  val connector =
    new ClaimsConnectorImpl(
      http = mockHttp,
      servicesConfig = new ServicesConfig(config),
      configuration = config,
      actorSystem = actorSystem
    )

  val expectedUrl = "http://foo.bar.com:1234/foo-claims/get-claims"

  def givenServiceReturns: HttpResponse => CallHandler[Future[HttpResponse]] =
    mockHttpPostSuccess(expectedUrl, Json.toJson(GetClaimsRequest(claimSubmitted = false)), hasHeaders = false)(_)

  given HeaderCarrier = HeaderCarrier()

  val testClaimsUnsubmittedJson: String                    = readTestResource("/test-claims-unsubmitted.json")
  val expectedClaimsUnsubmittedResponse: GetClaimsResponse = Json.parse(testClaimsUnsubmittedJson).as[GetClaimsResponse]

  "ClaimsConnector" - {
    "retrieveUnsubmittedClaims" - {
      "have retries defined" in {
        connector.retryIntervals shouldBe Seq(FiniteDuration(10, "ms"), FiniteDuration(50, "ms"))
      }

      "should return a list of unsubmitted claims" in {
        givenServiceReturns(HttpResponse(200, testClaimsUnsubmittedJson)).once()
        await(connector.retrieveUnsubmittedClaims) shouldEqual expectedClaimsUnsubmittedResponse
      }

      "throw an exception if the service returs malformed JSON" in {
        givenServiceReturns(HttpResponse(200, "{\"claimsCount\": 1, \"claimsList\": [{\"claimId\": 123}]")).once()
        a[Exception] should be thrownBy {
          await(connector.retrieveUnsubmittedClaims)
        }
      }

      "throw an exception if the service returs wrong entity format" in {
        givenServiceReturns(HttpResponse(200, "{\"claimsCount\": 1, \"claimsList\": [{\"claimId\": 123}]}")).once()
        a[Exception] should be thrownBy {
          await(connector.retrieveUnsubmittedClaims)
        }
      }

      "throw an exception if the service returns 404 status" in {
        givenServiceReturns(HttpResponse(404, "Bad Request")).once()
        a[Exception] should be thrownBy {
          await(connector.retrieveUnsubmittedClaims)
        }
      }

      "throw an exception if the service returns 500 status" in {
        givenServiceReturns(HttpResponse(500, "")).once()
        a[Exception] should be thrownBy {
          await(connector.retrieveUnsubmittedClaims)
        }
      }

      "throw exception when 5xx response status in the third attempt" in {
        givenServiceReturns(HttpResponse(500, "")).once()
        givenServiceReturns(HttpResponse(499, "")).once()
        givenServiceReturns(HttpResponse(469, "")).once()

        a[Exception] shouldBe thrownBy {
          await(connector.retrieveUnsubmittedClaims)
        }
      }

      "accept valid response in a second attempt" in {
        givenServiceReturns(HttpResponse(500, "")).once()
        givenServiceReturns(HttpResponse(200, testClaimsUnsubmittedJson)).once()
        await(connector.retrieveUnsubmittedClaims) shouldEqual expectedClaimsUnsubmittedResponse
      }

      "accept valid response in a third attempt" in {
        givenServiceReturns(HttpResponse(499, "")).once()
        givenServiceReturns(HttpResponse(500, "")).once()
        givenServiceReturns(HttpResponse(200, testClaimsUnsubmittedJson)).once()
        await(connector.retrieveUnsubmittedClaims) shouldEqual expectedClaimsUnsubmittedResponse
      }
    }
  }
}

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
import play.api.Configuration
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import org.scalamock.handlers.CallHandler
import play.api.test.Helpers.*
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

import java.net.URL

class UnregulatedDonationsConnectorSpec extends BaseSpec with HttpV2Support {

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
    new UnregulatedDonationsConnectorImpl(
      http = mockHttp,
      servicesConfig = new ServicesConfig(config),
      configuration = config,
      actorSystem = actorSystem
    )

  def givenGetTotalUnregulatedDonationsEndpointReturns(response: HttpResponse): CallHandler[Future[HttpResponse]] =
    mockHttpGetSuccess(
      URL("http://foo.bar.com:1234/foo-claims/charities/123/unregulated-donations")
    )(response)

  given HeaderCarrier = HeaderCarrier()

  "UnregulatedDonationsConnector" - {
    "getTotalUnregulatedDonations" - {
      "have retries defined" in {
        connector.retryIntervals shouldBe Seq(FiniteDuration(10, "ms"), FiniteDuration(50, "ms"))
      }

      "should return a total of unregulated donations if the service returns 200 status" in {
        givenGetTotalUnregulatedDonationsEndpointReturns(
          HttpResponse(200, "{\"unregulatedDonationsTotal\": 123.45}")
        ).once()

        await(connector.getTotalUnregulatedDonations("123")) shouldEqual Some(123.45)
      }

      "should return None if the service returns 404 status" in {
        givenGetTotalUnregulatedDonationsEndpointReturns(HttpResponse(404, "")).once()
        await(connector.getTotalUnregulatedDonations("123")) shouldEqual None
      }

      "throw an exception if the service returs malformed JSON" in {
        givenGetTotalUnregulatedDonationsEndpointReturns(
          HttpResponse(200, "{unregulatedDonationsTotal: 123.45}")
        ).once()
        a[Exception] should be thrownBy {
          await(connector.getTotalUnregulatedDonations("123"))
        }
      }

      "throw an exception if the service returs wrong entity format" in {
        givenGetTotalUnregulatedDonationsEndpointReturns(
          HttpResponse(200, "{\"claimsCount\": 1, \"claimsList\": [{\"claimId\": 123}]}")
        ).once()
        a[Exception] should be thrownBy {
          await(connector.getTotalUnregulatedDonations("123"))
        }
      }

      "throw an exception if the service returns 500 status" in {
        givenGetTotalUnregulatedDonationsEndpointReturns(HttpResponse(500, "")).once()
        a[Exception] should be thrownBy {
          await(connector.getTotalUnregulatedDonations("123"))
        }
      }

      "throw exception when 5xx response status in the third attempt" in {
        givenGetTotalUnregulatedDonationsEndpointReturns(HttpResponse(500, "")).once()
        givenGetTotalUnregulatedDonationsEndpointReturns(HttpResponse(499, "")).once()
        givenGetTotalUnregulatedDonationsEndpointReturns(HttpResponse(469, "")).once()

        a[Exception] shouldBe thrownBy {
          await(connector.getTotalUnregulatedDonations("123"))
        }
      }

      "accept valid response in a second attempt" in {
        givenGetTotalUnregulatedDonationsEndpointReturns(HttpResponse(500, "")).once()
        givenGetTotalUnregulatedDonationsEndpointReturns(
          HttpResponse(200, "{\"unregulatedDonationsTotal\": 123.45}")
        ).once()
        await(connector.getTotalUnregulatedDonations("123")) shouldEqual Some(123.45)
      }

      "accept valid response in a third attempt" in {
        givenGetTotalUnregulatedDonationsEndpointReturns(HttpResponse(499, "")).once()
        givenGetTotalUnregulatedDonationsEndpointReturns(HttpResponse(500, "")).once()
        givenGetTotalUnregulatedDonationsEndpointReturns(
          HttpResponse(200, "{\"unregulatedDonationsTotal\": 123.45}")
        ).once()
        await(connector.getTotalUnregulatedDonations("123")) shouldEqual Some(123.45)
      }
    }
  }

}

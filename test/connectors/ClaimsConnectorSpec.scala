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

import util.{BaseSpec, HttpV2Support, TestClaims}
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

  def givenGetClaimsEndpointReturns(response: HttpResponse): CallHandler[Future[HttpResponse]] =
    givenGetReturns(
      expectedUrl = "http://foo.bar.com:1234/foo-claims/claims?claimSubmitted=false",
      response = response
    )

  def givenSaveClaimEndpointReturns(
    payload: SaveClaimRequest,
    response: HttpResponse
  ): CallHandler[Future[HttpResponse]] =
    givenPostReturns(
      expectedUrl = "http://foo.bar.com:1234/foo-claims/claims",
      expectedPayload = Json.toJson(payload),
      response = response
    )

  def givenGetClaimEndpointReturns(
    response: HttpResponse
  ): CallHandler[Future[HttpResponse]] =
    givenGetReturns(
      expectedUrl = "http://foo.bar.com:1234/foo-claims/claims/123",
      response = response
    )

  def givenUpdateClaimEndpointReturns(
    payload: UpdateClaimRequest,
    response: HttpResponse
  ): CallHandler[Future[HttpResponse]] =
    givenPutReturns(
      expectedUrl = "http://foo.bar.com:1234/foo-claims/claims/123",
      expectedPayload = Json.toJson(payload),
      response = response
    )

  def givenDeleteClaimEndpointReturns(
    response: HttpResponse
  ): CallHandler[Future[HttpResponse]] =
    givenDeleteReturns(
      expectedUrl = "http://foo.bar.com:1234/foo-claims/claims/123",
      response = response
    )

  given HeaderCarrier = HeaderCarrier()

  "ClaimsConnector" - {
    "retrieveUnsubmittedClaims" - {
      "have retries defined" in {
        connector.retryIntervals shouldBe Seq(FiniteDuration(10, "ms"), FiniteDuration(50, "ms"))
      }

      "should return a list of unsubmitted claims" in {
        givenGetClaimsEndpointReturns(HttpResponse(200, TestClaims.testGetClaimsResponseUnsubmittedJsonString)).once()

        await(connector.retrieveUnsubmittedClaims) shouldEqual TestClaims.testGetClaimsResponseUnsubmitted
      }

      "throw an exception if the service returns malformed JSON" in {
        givenGetClaimsEndpointReturns(HttpResponse(200, "{\"claimsCount\": 1, \"claimsList\": [{\"claimId\": 123}]"))
          .once()
        a[Exception] should be thrownBy {
          await(connector.retrieveUnsubmittedClaims)
        }
      }

      "throw an exception if the service returns wrong entity format" in {
        givenGetClaimsEndpointReturns(HttpResponse(200, "{\"claimsCount\": 1, \"claimsList\": [{\"claimId\": 123}]}"))
          .once()
        a[Exception] should be thrownBy {
          await(connector.retrieveUnsubmittedClaims)
        }
      }

      "throw an exception if the service returns 404 status" in {
        givenGetClaimsEndpointReturns(HttpResponse(404, "Bad Request")).once()
        a[Exception] should be thrownBy {
          await(connector.retrieveUnsubmittedClaims)
        }
      }

      "throw an exception if the service returns 500 status" in {
        givenGetClaimsEndpointReturns(HttpResponse(500, "")).once()
        a[Exception] should be thrownBy {
          await(connector.retrieveUnsubmittedClaims)
        }
      }

      "throw exception when 5xx response status in the third attempt" in {
        givenGetClaimsEndpointReturns(HttpResponse(500, "")).once()
        givenGetClaimsEndpointReturns(HttpResponse(499, "")).once()
        givenGetClaimsEndpointReturns(HttpResponse(469, "")).once()

        a[Exception] shouldBe thrownBy {
          await(connector.retrieveUnsubmittedClaims)
        }
      }

      "accept valid response in a second attempt" in {
        givenGetClaimsEndpointReturns(HttpResponse(500, "")).once()
        givenGetClaimsEndpointReturns(HttpResponse(200, TestClaims.testGetClaimsResponseUnsubmittedJsonString)).once()
        await(connector.retrieveUnsubmittedClaims) shouldEqual TestClaims.testGetClaimsResponseUnsubmitted
      }

      "accept valid response in a third attempt" in {
        givenGetClaimsEndpointReturns(HttpResponse(499, "")).once()
        givenGetClaimsEndpointReturns(HttpResponse(500, "")).once()
        givenGetClaimsEndpointReturns(HttpResponse(200, TestClaims.testGetClaimsResponseUnsubmittedJsonString)).once()
        await(connector.retrieveUnsubmittedClaims) shouldEqual TestClaims.testGetClaimsResponseUnsubmitted
      }
    }
  }

  "saveClaim" - {
    "should save a claim when all required fields are present" in {
      givenSaveClaimEndpointReturns(
        SaveClaimRequest(
          claimingGiftAid = true,
          claimingTaxDeducted = true,
          claimingUnderGiftAidSmallDonationsScheme = false,
          claimReferenceNumber = Some("1234567890")
        ),
        HttpResponse(
          200,
          Json.stringify(Json.toJson(SaveClaimResponse(claimId = "1237", lastUpdatedReference = "0123456789")))
        )
      ).once()
      await(
        connector.saveClaim(
          RepaymentClaimDetails(
            claimingGiftAid = true,
            claimingTaxDeducted = true,
            claimingUnderGiftAidSmallDonationsScheme = false,
            claimReferenceNumber = Some("1234567890")
          )
        )
      ).claimId shouldEqual "1237"
    }
  }

  "getClaim" - {
    "should send a get request and return a claim on success" in {
      givenGetClaimEndpointReturns(
        HttpResponse(200, Json.stringify(Json.toJson(TestClaims.testClaimUnsubmitted)))
      )
      await(connector.getClaim("123")) shouldEqual Some(TestClaims.testClaimUnsubmitted)
    }

    "should send a get request and return None on not found" in {
      givenGetClaimEndpointReturns(HttpResponse(404, "Not Found"))
      await(connector.getClaim("123")) shouldEqual None
    }
  }

  "updateClaim" - {
    "should send an update request and return Unit on success" in {
      val repaymentDetails = RepaymentClaimDetails(
        claimingGiftAid = true,
        claimingTaxDeducted = true,
        claimingUnderGiftAidSmallDonationsScheme = false,
        claimReferenceNumber = Some("1234567890")
      )

      val updateRequest = UpdateClaimRequest(
        lastUpdatedReference = "1234567890",
        repaymentClaimDetails = repaymentDetails
      )

      givenUpdateClaimEndpointReturns(
        payload = updateRequest,
        response = HttpResponse(
          200,
          Json.stringify(Json.toJson(UpdateClaimResponse(success = true, lastUpdatedReference = "1234567891")))
        )
      )

      await(connector.updateClaim("123", updateRequest)) shouldBe (UpdateClaimResponse(
        success = true,
        lastUpdatedReference = "1234567891"
      ))
    }

    "should throw UpdatedByAnotherUserException when backend returns 400 with UPDATED_BY_ANOTHER_USER error" in {
      val repaymentDetails = RepaymentClaimDetails(
        claimingGiftAid = true,
        claimingTaxDeducted = true,
        claimingUnderGiftAidSmallDonationsScheme = false,
        claimReferenceNumber = Some("1234567890")
      )

      val updateRequest = UpdateClaimRequest(
        lastUpdatedReference = "1234567890",
        repaymentClaimDetails = repaymentDetails
      )

      givenUpdateClaimEndpointReturns(
        payload = updateRequest,
        response = HttpResponse(400, """{"errorCode": "UPDATED_BY_ANOTHER_USER"}""")
      )

      a[UpdatedByAnotherUserException] should be thrownBy {
        await(connector.updateClaim("123", updateRequest))
      }
    }

    "should throw MaxClaimsExceededException when backend returns 400 with MAX_CLAIMS_EXCEEDED error" in {
      val repaymentDetails = RepaymentClaimDetails(
        claimingGiftAid = true,
        claimingTaxDeducted = true,
        claimingUnderGiftAidSmallDonationsScheme = false,
        claimReferenceNumber = Some("1234567890")
      )

      val updateRequest = UpdateClaimRequest(
        lastUpdatedReference = "1234567890",
        repaymentClaimDetails = repaymentDetails
      )

      givenUpdateClaimEndpointReturns(
        payload = updateRequest,
        response = HttpResponse(400, """{"errorCode": "MAX_CLAIMS_EXCEEDED"}""")
      )

      a[MaxClaimsExceededException] should be thrownBy {
        await(connector.updateClaim("123", updateRequest))
      }
    }

    "should throw UnknownClaimError when backend returns 400 with unknown error code" in {
      val repaymentDetails = RepaymentClaimDetails(
        claimingGiftAid = true,
        claimingTaxDeducted = true,
        claimingUnderGiftAidSmallDonationsScheme = false,
        claimReferenceNumber = Some("1234567890")
      )

      val updateRequest = UpdateClaimRequest(
        lastUpdatedReference = "1234567890",
        repaymentClaimDetails = repaymentDetails
      )

      givenUpdateClaimEndpointReturns(
        payload = updateRequest,
        response = HttpResponse(400, """{"errorCode": "SOME_UNKNOWN_ERROR"}""")
      )

      a[UnknownClaimError] should be thrownBy {
        await(connector.updateClaim("123", updateRequest))
      }
    }
  }

  "deleteClaim" - {
    "should send a delete request and return true on success" in {
      givenDeleteClaimEndpointReturns(
        HttpResponse(200, Json.stringify(Json.toJson(DeleteClaimResponse(success = true))))
      )
      await(connector.deleteClaim("123")) shouldBe true
    }

    "should send a delete request and return false on failure" in {
      givenDeleteClaimEndpointReturns(
        HttpResponse(200, Json.stringify(Json.toJson(DeleteClaimResponse(success = false))))
      )
      await(connector.deleteClaim("123")) shouldBe false
    }

    "throw an exception if the service returns malformed JSON" in {
      givenDeleteClaimEndpointReturns(HttpResponse(200, "{\"claimsCount\": 1, \"claimsList\": [{\"claimId\": 123}]"))
        .once()
      a[Exception] should be thrownBy {
        await(connector.deleteClaim("123"))
      }
    }

    "throw an exception if the service returns wrong entity format" in {
      givenDeleteClaimEndpointReturns(HttpResponse(200, "{\"claimsCount\": 1, \"claimsList\": [{\"claimId\": 123}]}"))
        .once()
      a[Exception] should be thrownBy {
        await(connector.deleteClaim("123"))
      }
    }

    "throw an exception if the service returns 404 status" in {
      givenDeleteClaimEndpointReturns(HttpResponse(404, "Bad Request")).once()
      a[Exception] should be thrownBy {
        await(connector.deleteClaim("123"))
      }
    }

    "throw an exception if the service returns 500 status" in {
      givenDeleteClaimEndpointReturns(HttpResponse(500, ""))
      a[Exception] should be thrownBy {
        await(connector.deleteClaim("123"))
      }
    }
  }
}

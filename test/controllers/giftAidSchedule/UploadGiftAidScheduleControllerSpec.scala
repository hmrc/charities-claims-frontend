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

package controllers.giftAidSchedule

import com.typesafe.config.ConfigFactory
import connectors.ClaimsConnector
import controllers.giftAidSchedule.routes
import controllers.ControllerSpec
import models.{RepaymentClaimDetailsAnswers, SessionData, UploadRequest, UpscanInitiateRequest, UpscanInitiateResponse}
import connectors.{ClaimsValidationConnector, UpscanInitiateConnector}
import play.api.{Application, Configuration}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.libs.json.Json
import util.HttpV2Support
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import org.scalamock.handlers.CallHandler

import scala.concurrent.Future

class UploadGiftAidScheduleControllerSpec extends ControllerSpec with HttpV2Support {
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

  "UploadGiftAidScheduleController" - {

    "onPageLoad" - {

      "should render Page Not Found if setClaimingGiftAid is false" in {
        val sessionData  = RepaymentClaimDetailsAnswers.setClaimingGiftAid(false)
        val customConfig = Map(
          "urls.giftAidScheduleSpreadsheetsToClaimBackTaxOnDonationsUrl" -> "https://test.example.com/charity-repayment-claim"
        )

        given application: Application = applicationBuilder(sessionData = sessionData).configure(customConfig).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.UploadGiftAidScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.repaymentClaimDetails.routes.RepaymentClaimDetailsController.onPageLoad.url
          )
        }
      }

      "should render Page Not Found if setClaimingGiftAid is true && unsubmittedClaimId is None" in {
        val sessionData  = RepaymentClaimDetailsAnswers.setClaimingGiftAid(true)
        val customConfig = Map(
          "urls.giftAidScheduleSpreadsheetsToClaimBackTaxOnDonationsUrl" -> "https://test.example.com/charity-repayment-claim"
        )

        given application: Application = applicationBuilder(sessionData = sessionData).configure(customConfig).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.UploadGiftAidScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.repaymentClaimDetails.routes.RepaymentClaimDetailsController.onPageLoad.url
          )
        }
      }

      "should render next page if setClaimingGiftAid is true && unsubmittedClaimId is not None & giftAidScheduleFileUploadReference is defined" in {

        val sessionDataTestClaim =
          SessionData
            .empty(testCharitiesReference)
            .copy(unsubmittedClaimId = Some("test-claim-123"))
            .copy(giftAidScheduleUpscanInitialization = Some(response))

        val sessionData = RepaymentClaimDetailsAnswers.setClaimingGiftAid(true)(using sessionDataTestClaim)

        val customConfig = Map(
          "urls.giftAidScheduleSpreadsheetsToClaimBackTaxOnDonationsUrl" -> "https://test.example.com/charity-repayment-claim"
        )

        given application: Application = applicationBuilder(sessionData = sessionData)
          .configure(customConfig)
          .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.UploadGiftAidScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.YourGiftAidScheduleUploadController.onPageLoad.url
          )
        }
      }

      "should use the correct configured giftAidScheduleSpreadsheetsToClaimBackTaxOnDonationsUrl in the message" in {
        val sessionData  = RepaymentClaimDetailsAnswers.setClaimingGiftAid(true)
        val customConfig = Map(
          "urls.giftAidScheduleSpreadsheetsToClaimBackTaxOnDonationsUrl" -> "https://test.example.com/charity-repayment-claim"
        )

        given application: Application = applicationBuilder(sessionData = sessionData)
          .configure(customConfig)
          .build()

        running(application) {
          val request =
            FakeRequest(GET, routes.UploadGiftAidScheduleController.onPageLoad.url)
          val result  = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include("https://test.example.com/charity-repayment-claim")
        }
      }
    }

    "onUploadSuccess" - {
      "should redirect to the next page when claim id is not defined" in {
        // val sessionData = RepaymentClaimDetailsAnswers.setClaimingGiftAid(true)

        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.UploadGiftAidScheduleController.onUploadError.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.repaymentClaimDetails.routes.RepaymentClaimDetailsController.onPageLoad.url
          )
        }
      }

      "should redirect to the next page when claim id is defined & upload reference is found" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingGiftAid(true)
          .copy(unsubmittedClaimId = Some("test-claim-123"))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.UploadGiftAidScheduleController.onUploadError.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.YourGiftAidScheduleUploadController.onPageLoad.url
          )
        }
      }

      "should redirect to the next page when claim id is defined & upload reference is not found" in {
        val sessionData = RepaymentClaimDetailsAnswers.setClaimingGiftAid(true)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.UploadGiftAidScheduleController.onUploadError.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.YourGiftAidScheduleUploadController.onPageLoad.url
          )
        }
      }
    }

//    "onUploadError" - {
//      "should redirect to the next page" in {
//        val sessionData                =
//          SessionData
//            .empty(testCharitiesReference)
//            .copy(giftAidScheduleFileUploadReference = Some("test-claim-123"))
//        given application: Application = applicationBuilder(sessionData = sessionData).build()
//
//        running(application) {
//          given request: FakeRequest[AnyContentAsEmpty.type] =
//            FakeRequest(POST, routes.UploadGiftAidScheduleController.onUploadError.url)
//
//          val result = route(application, request).value
//
//          status(result) shouldEqual SEE_OTHER
//          redirectLocation(result) shouldEqual Some(routes.YourGiftAidScheduleUploadController.onPageLoad.url)
//        }
//      }
//    }
  }

}

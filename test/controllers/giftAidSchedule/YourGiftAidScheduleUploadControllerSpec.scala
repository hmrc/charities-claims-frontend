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

import com.typesafe.config.ConfigFactory
import controllers.giftAidSchedule.routes
import controllers.ControllerSpec
import models.{RepaymentClaimDetailsAnswers, UpscanInitiateRequest, UpscanInitiateResponse}
import play.api.{Application, Configuration}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.libs.json.Json
import util.HttpV2Support
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import org.scalamock.handlers.CallHandler
import views.html.YourGiftAidScheduleUploadView
import services.{ClaimsService, ClaimsValidationService}
import models.*

import scala.concurrent.Future

class YourGiftAidScheduleUploadControllerSpec extends ControllerSpec with HttpV2Support {
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
        val sessionData                = defaultSessionData.copy(unsubmittedClaimId = Some("test-claim-123"))
        given application: Application = applicationBuilder(sessionData = sessionData).build()

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
        val sessionData                = defaultSessionData
          .copy(unsubmittedClaimId = Some("test-claim-123"))
          .copy(giftAidScheduleFileUploadReference = "11370e18-6e24-453e-b45a-76d3e32ea33d")
        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.YourGiftAidScheduleUploadController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[YourGiftAidScheduleUploadView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(
            claimId = claimId,
            uploadResult = FileStatus.AWAITING_UPLOAD,
            failureDetails = None,
            screenLocked = true
          ).body

        }
      }
      //      "unsubmitted Claim ID & file reference are defined - result = Verifying" in {}
      //      "unsubmitted Claim ID & file reference are defined - result = Validating" in {}
      //      "unsubmitted Claim ID & file reference are defined - result = Verification Failed" in {}
      //      "unsubmitted Claim ID & file reference are defined - result = Validated" in {}
      //      "unsubmitted Claim ID & file reference are defined - result = other" in {}
    }

    //    "onRemove" in {}
    //
    //    "onSubmit" - {
    //      "unsubmitted Claim ID is not defined" in {}
    //      "unsubmitted Claim ID & file reference are defined - result = Validated" in {}
    //      "unsubmitted Claim ID & file reference are defined - result = Validation Failed" in {}
    //      "unsubmitted Claim ID & file reference are defined - result = other" in {}
    //    }
  }
}

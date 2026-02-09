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

import connectors.ClaimsConnector
import controllers.giftAidSchedule.routes
import controllers.ControllerSpec
import models.{RepaymentClaimDetailsAnswers, SessionData, UploadRequest, UpscanInitiateResponse}
import connectors.{ClaimsValidationConnector, UpscanInitiateConnector}
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class UploadGiftAidScheduleControllerSpec extends ControllerSpec {
  "UploadGiftAidScheduleController" - {
    val mockClaimsConnector: ClaimsConnector = mock[ClaimsConnector]

//    "must initiate a request to upscan to bring back an upload form" in {
//      val fakeUpscanConnector: FakeUpscanConnector = inject[FakeUpscanConnector]
//      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
//
//      val application: Application = applicationBuilder(userAnswers = Some(userAnswers))
//        .overrides(
//          bind[UpscanConnector].toInstance(fakeUpscanConnector)
//        )
//        .build()
//
//      val request = FakeRequest(GET, routes.UploadGiftAidScheduleController.onPageLoad().url)
//      val result  = route(application, request).value
//
//      val view = application.injector.instanceOf[UploadGiftAidScheduleView]
//
//      status(result) mustEqual OK
//      contentAsString(result) mustEqual view(form(), UpscanInitiateResponse(Reference(""), "target", Map.empty))(
//        request,
//        messages(application)
//      ).toString
//    }

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
        // val sessionDataUnC = SessionData.empty(testCharitiesReference).copy(unsubmittedClaimId = Some("test-claim-123"))

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
        val validUploadRequest          = UploadRequest(
          href = "https://xxxx/upscan-upload-proxy/bucketName",
          fields =
            "fields = { \"Content-Type\": \"application/xml\", \"acl\": \"private\",\n  *   \"key\": \"xxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx\", \"policy\": \"xxxxxxxx==\", \"x-amz-algorithm\": \"AWS4-HMAC-SHA256\",\n  *   \"x-amz-credential\": \"ASIAxxxxxxxxx/20180202/eu-west-2/s3/aws4_request\", \"x-amz-date\": \"yyyyMMddThhmmssZ\",\n  *   \"x-amz-meta-callback-url\": \"https://myservice.com/callback\", \"x-amz-signature\": \"xxxx\", \"success_action_redirect\":\n  *   \"https://myservice.com/nextPage\", \"error_action_redirect\": \"https://myservice.com/errorPage\"}"
        )
        val validUpscanInitiateResponse = UpscanInitiateResponse(
          reference = "11370e18-6e24-453e-b45a-76d3e32ea33d",
          uploadRequest = validUploadRequest
        )
        val sessionDataTestClaim        =
          SessionData
            .empty(testCharitiesReference)
            .copy(unsubmittedClaimId = Some("test-claim-123"))
            .copy(giftAidScheduleUpscanInitialization = Some(validUpscanInitiateResponse))

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

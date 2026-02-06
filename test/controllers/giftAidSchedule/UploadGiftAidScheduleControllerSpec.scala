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
import models.{RepaymentClaimDetailsAnswers, SessionData}
import play.api.Application
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.inject.bind
import services.ClaimsValidationService
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

//      "should render Page Not Found if setClaimingGiftAid is false && unsubmittedClaimId is not None" in {
//        val sessionDataTestClaim =
//          SessionData
//            .empty(testCharitiesReference)
//            .copy(unsubmittedClaimId = Some("test-claim-123"))
//
//        val sessionData          = RepaymentClaimDetailsAnswers.setClaimingGiftAid(false)(using sessionDataTestClaim)
//        (mockClaimsConnector
//          .deleteClaim(_: String)(using _: HeaderCarrier))
//          .expects("test-claim-123", *)
//          .returning(Future.successful(false))
//
//        val customConfig = Map(
//          "urls.giftAidScheduleSpreadsheetsToClaimBackTaxOnDonationsUrl" -> "https://test.example.com/charity-repayment-claim"
//        )
//
//        given application: Application = applicationBuilder(sessionData = sessionData)
//          .overrides(bind[ClaimsConnector].toInstance(mockClaimsConnector))
//          .configure(customConfig)
//          .build()
//
//        running(application) {
//          given request: FakeRequest[AnyContentAsEmpty.type] =
//            FakeRequest(GET, routes.UploadGiftAidScheduleController.onPageLoad.url)
//
//          val result = route(application, request).value
//
//          status(result) shouldEqual SEE_OTHER
//          redirectLocation(result) shouldEqual Some(
//            controllers.repaymentClaimDetails.routes.RepaymentClaimDetailsController.onPageLoad.url
//          )
//        }
//      }

//      "should use the correct configured giftAidScheduleSpreadsheetsToClaimBackTaxOnDonationsUrl in the message" in {
//        val sessionData  = RepaymentClaimDetailsAnswers.setClaimingGiftAid(true)
//        val customConfig = Map(
//          "urls.giftAidScheduleSpreadsheetsToClaimBackTaxOnDonationsUrl" -> "https://test.example.com/charity-repayment-claim"
//        )
//
//        given application: Application = applicationBuilder(sessionData = sessionData)
//          .configure(customConfig)
//          .build()
//
//        running(application) {
//          val request =
//            FakeRequest(GET, routes.UploadGiftAidScheduleController.onPageLoad.url)
//          val result  = route(application, request).value
//
//          status(result) shouldEqual OK
//          contentAsString(result) should include("https://test.example.com/charity-repayment-claim")
//        }
//      }
    }

//    "onUploadError" - {
//      "should redirect to the next page" in {
//        given application: Application = applicationBuilder().build()
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

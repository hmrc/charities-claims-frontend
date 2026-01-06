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

package controllers.organisationDetails

import play.api.test.FakeRequest
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import controllers.ControllerSpec
import views.html.ClaimingReferenceNumberCheckView
import play.api.Application
import forms.YesNoFormProvider
import models.RepaymentClaimDetailsAnswers
import play.api.data.Form
import models.Mode.*

// TODO:
//  - make sure to test that the schedule is actually deleted when "Yes" is selected
//  - make sure user gets redirected to R2 when selected YES
//  - make sure user gets redirected to G2 when selected NO, and make sure schedule is retained
//  - check regarding screen readers and accessibility
class DeleteGiftAidScheduleControllerSpec extends ControllerSpec {
//
//  private val form: Form[Boolean] = new YesNoFormProvider()()
//
//  "ClaimingReferenceNumberCheckController" - {
//    "onPageLoad" - {
//      "should render the page correctly" in {
//
//        given application: Application = applicationBuilder().build()
//
//        running(application) {
//          given request: FakeRequest[AnyContentAsEmpty.type] =
//            FakeRequest(GET, routes.DeleteGiftAidScheduleController.onPageLoad.url)
//
//          val result = route(application, request).value
//          val view   = application.injector.instanceOf[ClaimingReferenceNumberCheckView]
//
//          status(result)          shouldBe OK
//          contentAsString(result) shouldBe view(form, NormalMode).body
//        }
//      }
//
//      "should render the page and pre-populate correctly with true value" in {
//
//        val sessionData = RepaymentClaimDetailsAnswers.setClaimingReferenceNumber(true)
//
//        given application: Application = applicationBuilder(sessionData = sessionData).build()
//
//        running(application) {
//          given request: FakeRequest[AnyContentAsEmpty.type] =
//            // FakeRequest(GET, routes.ClaimingReferenceNumberCheckController.onPageLoad(NormalMode).url)
//
//          val result = route(application, request).value
//          val view   = application.injector.instanceOf[ClaimingReferenceNumberCheckView]
//
//          status(result)          shouldBe OK
//          contentAsString(result) shouldBe view(form.fill(true), NormalMode).body
//        }
//      }
//      "should render the page and pre-populate correctly with false value" in {
//
//        val sessionData = RepaymentClaimDetailsAnswers.setClaimingReferenceNumber(false)
//
//        given application: Application = applicationBuilder(sessionData = sessionData).build()
//
//        running(application) {
//          given request: FakeRequest[AnyContentAsEmpty.type] =
//            // FakeRequest(GET, routes.ClaimingReferenceNumberCheckController.onPageLoad(NormalMode).url)
//
//          val result = route(application, request).value
//          val view   = application.injector.instanceOf[ClaimingReferenceNumberCheckView]
//
//          status(result)          shouldBe OK
//          contentAsString(result) shouldBe view(form.fill(false), NormalMode).body
//        }
//      }
//    }
//
//    "onSubmit" - {
//      "should redirect to the next page when the value is true and NormalMode" in {
//        given application: Application = applicationBuilder().mockSaveSession.build()
//
//        running(application) {
//          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
//            // FakeRequest(POST, routes.ClaimingReferenceNumberCheckController.onSubmit(NormalMode).url)
//              // .withFormUrlEncodedBody("value" -> "true")
//
//          val result = route(application, request).value
//
//          status(result)           shouldBe SEE_OTHER
//          redirectLocation(result) shouldBe Some(
//            controllers.repaymentclaimdetails.routes.ClaimReferenceNumberInputController.onPageLoad(NormalMode).url
//          )
//        }
//      }
//
//      "should redirect to the next page when the value is true and CheckMode" in {
//        given application: Application = applicationBuilder().mockSaveSession.build()
//
//        running(application) {
//          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
//            // FakeRequest(POST, routes.ClaimingReferenceNumberCheckController.onSubmit(NormalMode).url)
//              // .withFormUrlEncodedBody("value" -> "true")
//
//          val result = route(application, request).value
//
//          status(result)           shouldBe SEE_OTHER
//          redirectLocation(result) shouldBe Some(
//            controllers.repaymentclaimdetails.routes.ClaimReferenceNumberInputController.onPageLoad(NormalMode).url
//          )
//        }
//      }
//
//      "should redirect back to cya page when the value is false and CheckMode" in {
//        given application: Application = applicationBuilder().mockSaveSession.build()
//
//        running(application) {
//          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
//            // FakeRequest(POST, routes.ClaimingReferenceNumberCheckController.onSubmit(CheckMode).url)
//              // .withFormUrlEncodedBody("value" -> "false")
//
//          val result = route(application, request).value
//
//          status(result)           shouldBe SEE_OTHER
//          redirectLocation(result) shouldBe Some(
//            controllers.repaymentclaimdetails.routes.CheckYourAnswersController.onPageLoad.url
//          )
//        }
//      }
//
//      "should redirect back to cya page when the value is false and NormalMode" in {
//        given application: Application = applicationBuilder().mockSaveSession.build()
//
//        running(application) {
//          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
//            // FakeRequest(POST, routes.ClaimingReferenceNumberCheckController.onSubmit(NormalMode).url)
//              // .withFormUrlEncodedBody("value" -> "false")
//
//          val result = route(application, request).value
//
//          status(result)           shouldBe SEE_OTHER
//          redirectLocation(result) shouldBe Some(
//            controllers.repaymentclaimdetails.routes.ClaimDeclarationController.onPageLoad.url
//          )
//        }
//      }
//
//      "should reload the page with errors when a required field is missing" in {
//        given application: Application = applicationBuilder().build()
//
//        running(application) {
//          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
//            // FakeRequest(POST, routes.ClaimingReferenceNumberCheckController.onSubmit(NormalMode).url)
//              // .withFormUrlEncodedBody("other" -> "field")
//
//          val result = route(application, request).value
//
//          status(result) shouldBe BAD_REQUEST
//        }
//      }
//    }
//  }
}

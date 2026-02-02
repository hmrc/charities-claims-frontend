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

package controllers.repaymentClaimDetails

import controllers.ControllerSpec
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import models.{RepaymentClaimDetailsAnswers, RepaymentClaimType}
import forms.CheckBoxListFormProvider
import play.api.data.Form
import views.html.RepaymentClaimTypeView
import models.Mode.*

class RepaymentClaimTypeControllerSpec extends ControllerSpec {
  val formProvider                   = new CheckBoxListFormProvider()
  val form: Form[RepaymentClaimType] = formProvider()
  val dataWithAllChecked             = RepaymentClaimType(
    claimingGiftAid = true,
    claimingTaxDeducted = true,
    claimingUnderGiftAidSmallDonationsScheme = true
  )

  val dataWithNoneChecked = RepaymentClaimType(
    claimingGiftAid = false,
    claimingTaxDeducted = false,
    claimingUnderGiftAidSmallDonationsScheme = false
  )

  val dataWithClaimingGiftAidChecked = RepaymentClaimType(
    claimingGiftAid = true,
    claimingTaxDeducted = false,
    claimingUnderGiftAidSmallDonationsScheme = false
  )

  "RepaymentClaimTypeController" - {
    "onPageLoad" - {
      "should render the page correctly" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RepaymentClaimTypeController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[RepaymentClaimTypeView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with all checked" in {

        val sessionData = RepaymentClaimDetailsAnswers.setRepaymentClaimType(dataWithAllChecked)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RepaymentClaimTypeController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[RepaymentClaimTypeView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(dataWithAllChecked), NormalMode).body
        }
      }

      "should render the page and pre-populate correctly with none checked" in {

        val sessionData = RepaymentClaimDetailsAnswers.setRepaymentClaimType(dataWithNoneChecked)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RepaymentClaimTypeController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[RepaymentClaimTypeView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(dataWithNoneChecked), NormalMode).body
        }
      }
    }

    "onSubmit" - {
      "should redirect to the next page when the value is all are checked" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RepaymentClaimTypeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody(
                "value[0]" -> "claimingGiftAid",
                "value[1]" -> "claimingUnderGiftAidSmallDonationsScheme",
                "value[2]" -> "claimingTaxDeducted"
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimGASDSController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to the next page when the value when only claimingUnderGiftAidSmallDonationsScheme & claimingTaxDeducted are checked" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RepaymentClaimTypeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody(
                "value[1]" -> "claimingUnderGiftAidSmallDonationsScheme",
                "value[2]" -> "claimingTaxDeducted"
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimGASDSController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to the next page when the value when only claimingUnderGiftAidSmallDonationsScheme & claimingGiftAid are checked" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RepaymentClaimTypeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody(
                "value[0]" -> "claimingGiftAid",
                "value[1]" -> "claimingUnderGiftAidSmallDonationsScheme"
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimGASDSController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to the next page when the value when only claimingUnderGiftAidSmallDonationsScheme is checked" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RepaymentClaimTypeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody(
                "value[1]" -> "claimingUnderGiftAidSmallDonationsScheme"
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimGASDSController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to the next page when the value when only claimingGiftAid is checked" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RepaymentClaimTypeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody(
                "value[0]" -> "claimingGiftAid"
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimingReferenceNumberController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to the next page when the value when only claimingTaxDeducted is checked" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RepaymentClaimTypeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody(
                "value[2]" -> "claimingTaxDeducted"
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimingReferenceNumberController.onPageLoad(NormalMode).url
          )
        }
      }

      "should redirect to the next page when the value when only claimingGiftAid & claimingTaxDeducted are checked" in {
        given application: Application = applicationBuilder().mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RepaymentClaimTypeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody(
                "value[0]" -> "claimingGiftAid",
                "value[2]" -> "claimingTaxDeducted"
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimingReferenceNumberController.onPageLoad(NormalMode).url
          )
        }
      }

      "should reload the page with errors when a required field is missing" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RepaymentClaimTypeController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("claimingGiftAid" -> "None")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }
    }
  }

}

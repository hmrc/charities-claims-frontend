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
        val sessionData =
          RepaymentClaimDetailsAnswers.setRepaymentClaimType(dataWithAllChecked, Some(dataWithAllChecked))

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
        val sessionData =
          RepaymentClaimDetailsAnswers.setRepaymentClaimType(dataWithNoneChecked, Some(dataWithAllChecked))

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
            routes.ClaimGiftAidSmallDonationsSchemeController.onPageLoad(NormalMode).url
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
            routes.ClaimGiftAidSmallDonationsSchemeController.onPageLoad(NormalMode).url
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
            routes.ClaimGiftAidSmallDonationsSchemeController.onPageLoad(NormalMode).url
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
            routes.ClaimGiftAidSmallDonationsSchemeController.onPageLoad(NormalMode).url
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

      "in CheckMode" - {
        "old selection with GASDS & new selection with GASDS not defined should show WRN3" in {
          val sessionData                =
            RepaymentClaimDetailsAnswers.setRepaymentClaimType(dataWithAllChecked, Some(dataWithClaimingGiftAidChecked))
          given application: Application = applicationBuilder(sessionData = sessionData).build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.RepaymentClaimTypeController.onSubmit(CheckMode).url)
                .withFormUrlEncodedBody(
                  "value[0]" -> "claimingGiftAid",
                  "value[2]" -> "claimingTaxDeducted"
                )

            val result = route(application, request).value

            status(result) shouldEqual OK
            contentAsString(result) should include("Do you want to update this repayment claim?")
          }
        }

        "old GASDS not defined & new GASDS is true" in {
          given application: Application = applicationBuilder().mockSaveSession.build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.RepaymentClaimTypeController.onSubmit(CheckMode).url)
                .withFormUrlEncodedBody(
                  "value[0]" -> "claimingGiftAid",
                  "value[1]" -> "claimingUnderGiftAidSmallDonationsScheme",
                  "value[2]" -> "claimingTaxDeducted"
                )

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              routes.ClaimGiftAidSmallDonationsSchemeController.onPageLoad(CheckMode).url
            )
          }
        }

        "old GASDS not defined & new GASDS is false" in {
          given application: Application = applicationBuilder().mockSaveSession.build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.RepaymentClaimTypeController.onSubmit(CheckMode).url)
                .withFormUrlEncodedBody(
                  "value[0]" -> "claimingGiftAid",
                  "value[2]" -> "claimingTaxDeducted"
                )

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
            )
          }
        }

        "old GASDS is false and new GASDS" in {
          val sessionData =
            RepaymentClaimDetailsAnswers.setRepaymentClaimType(dataWithNoneChecked, Some(dataWithAllChecked))

          given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.RepaymentClaimTypeController.onSubmit(CheckMode).url)
                .withFormUrlEncodedBody(
                  "value[0]" -> "claimingGiftAid",
                  "value[1]" -> "claimingUnderGiftAidSmallDonationsScheme",
                  "value[2]" -> "claimingTaxDeducted"
                )

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              routes.ClaimGiftAidSmallDonationsSchemeController.onPageLoad(CheckMode).url
            )
          }
        }

        "no change in old and new GASDS" in {
          val sessionData =
            RepaymentClaimDetailsAnswers.setRepaymentClaimType(dataWithAllChecked, Some(dataWithAllChecked))

          given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

          running(application) {
            given request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, routes.RepaymentClaimTypeController.onSubmit(CheckMode).url)
                .withFormUrlEncodedBody(
                  "value[0]" -> "claimingGiftAid",
                  "value[1]" -> "claimingUnderGiftAidSmallDonationsScheme",
                  "value[2]" -> "claimingTaxDeducted"
                )

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
            )
          }
        }
      }

      // WRN3 Confirmation Screen Tests:

      "WRN3: should show confirmation when changing claimingGiftAid from Yes to No in CheckMode" in {
        val sessionData =
          RepaymentClaimDetailsAnswers.setRepaymentClaimType(dataWithAllChecked, Some(dataWithAllChecked))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RepaymentClaimTypeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody(
                "value[1]" -> "claimingUnderGiftAidSmallDonationsScheme",
                "value[2]" -> "claimingTaxDeducted"
              )

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include("Do you want to update this repayment claim?")
          contentAsString(result) should include("confirmingUpdate")
        }
      }

      "WRN3: should show confirmation when changing claimingTaxDeducted from Yes to No in CheckMode" in {
        val sessionData =
          RepaymentClaimDetailsAnswers.setRepaymentClaimType(dataWithAllChecked, Some(dataWithAllChecked))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RepaymentClaimTypeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody(
                "value[0]" -> "claimingGiftAid",
                "value[1]" -> "claimingUnderGiftAidSmallDonationsScheme"
              )

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include("Do you want to update this repayment claim?")
          contentAsString(result) should include("confirmingUpdate")
        }
      }

      "WRN3: should show confirmation when changing claimingUnderGiftAidSmallDonationsScheme from Yes to No in CheckMode" in {
        val sessionData =
          RepaymentClaimDetailsAnswers.setRepaymentClaimType(dataWithAllChecked, Some(dataWithAllChecked))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RepaymentClaimTypeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody(
                "value[0]" -> "claimingGiftAid",
                "value[2]" -> "claimingTaxDeducted"
              )

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include("Do you want to update this repayment claim?")
          contentAsString(result) should include("confirmingUpdate")
        }
      }

      "WRN3: should NOT show confirmation when changing No to Yes in CheckMode" in {
        val sessionData =
          RepaymentClaimDetailsAnswers.setRepaymentClaimType(
            dataWithClaimingGiftAidChecked,
            Some(dataWithClaimingGiftAidChecked)
          )

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RepaymentClaimTypeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody(
                "value[0]" -> "claimingGiftAid",
                "value[1]" -> "claimingUnderGiftAidSmallDonationsScheme"
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ClaimGiftAidSmallDonationsSchemeController.onPageLoad(CheckMode).url
          )
        }
      }

      "WRN3: should NOT show confirmation when answer unchanged in CheckMode" in {
        val sessionData =
          RepaymentClaimDetailsAnswers.setRepaymentClaimType(dataWithAllChecked, Some(dataWithAllChecked))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RepaymentClaimTypeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody(
                "value[0]" -> "claimingGiftAid",
                "value[1]" -> "claimingUnderGiftAidSmallDonationsScheme",
                "value[2]" -> "claimingTaxDeducted"
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "WRN3: should NOT show confirmation in NormalMode when unchecking selection" in {
        val sessionData =
          RepaymentClaimDetailsAnswers.setRepaymentClaimType(dataWithAllChecked, Some(dataWithAllChecked))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

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

      "WRN3: should save updated values and redirect to CYA when user confirms Yes on WRN3" in {
        val sessionData =
          RepaymentClaimDetailsAnswers.setRepaymentClaimType(dataWithAllChecked, Some(dataWithAllChecked))

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RepaymentClaimTypeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody(
                "confirmingUpdate" -> "true",
                "value"            -> "true",
                "value[]"          -> "claimingGiftAid",
                "value[]"          -> "claimingTaxDeducted"
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "WRN3: should redirect to CYA without saving when user selects No on WRN3" in {
        val sessionData =
          RepaymentClaimDetailsAnswers.setRepaymentClaimType(dataWithAllChecked, Some(dataWithAllChecked))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RepaymentClaimTypeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody(
                "confirmingUpdate" -> "true",
                "value"            -> "false",
                "value[]"          -> "claimingGiftAid",
                "value[]"          -> "claimingTaxDeducted"
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )
        }
      }

      "WRN3: should show errors when no radio selected on confirmation screen" in {
        val sessionData =
          RepaymentClaimDetailsAnswers.setRepaymentClaimType(dataWithAllChecked, Some(dataWithAllChecked))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.RepaymentClaimTypeController.onSubmit(CheckMode).url)
              .withFormUrlEncodedBody(
                "confirmingUpdate" -> "true",
                "value[]"          -> "claimingGiftAid",
                "value[]"          -> "claimingTaxDeducted"
              )

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
          contentAsString(result) should include("Do you want to update this repayment claim?")
          contentAsString(result) should include("Select ‘Yes’ if you want to update this repayment claim")
        }
      }
    }
  }
}

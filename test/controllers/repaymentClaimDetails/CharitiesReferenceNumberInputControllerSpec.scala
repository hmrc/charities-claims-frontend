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

import config.FrontendAppConfig
import controllers.ControllerSpec
import forms.TextInputFormProvider
import models.Mode.*
import models.{RepaymentClaimDetailsAnswers, SessionData}
import play.api.Application
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import views.html.CharitiesReferenceNumberInputView

class CharitiesReferenceNumberInputControllerSpec extends ControllerSpec {

  private val form: Form[String] = new TextInputFormProvider()(
    "charitiesReferenceNumber.error.required",
    (7, "charitiesReferenceNumber.error.length"),
    "charitiesReferenceNumber.error.regex"
  )

  "CharitiesReferenceNumberInputController" - {

    "onPageLoad" - {
      // TODO: UPDATE URL
      "should render the page correctly when the user provides a HMRC Charities Reference number" in {
        val answers = RepaymentClaimDetailsAnswers(
          claimingReferenceNumber = Some(true),
          hmrcCharitiesReference = Some("AA12356")
        )

        val sessionData = SessionData
          .empty("AA12356")
          .copy(
            repaymentClaimDetailsAnswers = Some(answers)
          )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CharitiesReferenceNumberInputController.onPageLoad().url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[CharitiesReferenceNumberInputView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form.fill(""), NormalMode).body
        }
      }
    }

    "onSubmit" - {

      // TODO: UPDATE REDIRECT URL
      "should redirect to R17 Agent About Repayment page when in NormalMode" in {
        val answers = RepaymentClaimDetailsAnswers(
          claimingReferenceNumber = Some(true),
          hmrcCharitiesReference = Some("AA12356")
        )

        val sessionData = SessionData
          .empty("AA12356")
          .copy(
            repaymentClaimDetailsAnswers = Some(answers)
          )

        given application: Application = applicationBuilder(sessionData = sessionData).mockSaveSession.build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CharitiesReferenceNumberInputController.onSubmit().url)
              .withFormUrlEncodedBody("value" -> "AA12356")

          val result = route(application, request).value

          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          status(result) shouldEqual SEE_OTHER
//          redirectLocation(result) shouldEqual Some(
//            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
//          )
          redirectLocation(result) shouldEqual Some(appConfig.charityRepaymentDashboardUrl)

        }
      }

    }
  }
}

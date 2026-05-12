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

import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import controllers.ControllerSpec
import views.html.CharitiesReferenceNumberInputView
import play.api.Application
import forms.TextInputFormProvider
import models.{RepaymentClaimDetailsAnswers, SessionData}
import play.api.data.Form
import play.api.test.FakeRequest
import models.Mode.*
import uk.gov.hmrc.auth.core.AffinityGroup
import connectors.ClaimsConnector
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CharitiesReferenceNumberInputControllerSpec extends ControllerSpec {

  private val form: Form[String] = new TextInputFormProvider()(
    "charitiesReferenceNumber.error.required",
    (7, "charitiesReferenceNumber.error.length"),
    "charitiesReferenceNumber.error.regex"
  )

  val mockClaimsConnector: ClaimsConnector = mock[ClaimsConnector]

  override protected val additionalBindings: List[GuiceableModule] = List(
    bind[ClaimsConnector].toInstance(mockClaimsConnector)
  )

  "CharitiesReferenceNumberInputController" - {

    "onPageLoad" - {

      "should Not render the page correctly for an organisation User" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CharitiesReferenceNumberInputController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should Not render the page correctly for an organisation User for checkmode" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CharitiesReferenceNumberInputController.onPageLoad(CheckMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should render the page correctly when the user provides a HMRC Charities Reference number" in {
        val sessionData = SessionData
          .empty("AA12356")

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CharitiesReferenceNumberInputController.onPageLoad(NormalMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[CharitiesReferenceNumberInputView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, NormalMode).body
        }
      }

      "should render the page correctly when the user provides a HMRC Charities Reference number - checkmode" in {
        val sessionData = SessionData
          .empty("AA12356")

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CharitiesReferenceNumberInputController.onPageLoad(CheckMode).url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[CharitiesReferenceNumberInputView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form, CheckMode).body
        }
      }
    }

    "onSubmit" - {
      "should return bad request when in NormalMode when hrmcref is duplicate ref - " in {
        val hmrcRef = "AA12356"
        val answers = RepaymentClaimDetailsAnswers(
          claimingReferenceNumber = Some(true),
          hmrcCharitiesReference = None
        )

        val sessionData = SessionData
          .empty("AA12356")
          .copy(
            repaymentClaimDetailsAnswers = Some(answers)
          )

        (mockClaimsConnector
          .hasUnsubmittedClaim(_: String)(using _: HeaderCarrier))
          .expects(hmrcRef, *)
          .returning(Future(true))

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CharitiesReferenceNumberInputController.onPageLoad(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "AA12356")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      "should redirect to R1.9 when in NormalMode" in {
        val hmrcRef = "AA12356"
        val answers = RepaymentClaimDetailsAnswers(
          claimingReferenceNumber = Some(true),
          hmrcCharitiesReference = None
        )

        val sessionData = SessionData
          .empty("AA12356")
          .copy(
            repaymentClaimDetailsAnswers = Some(answers)
          )

        (mockClaimsConnector
          .hasUnsubmittedClaim(_: String)(using _: HeaderCarrier))
          .expects(hmrcRef, *)
          .returning(Future(false))

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CharitiesReferenceNumberInputController.onPageLoad(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "AA12356")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.EnterCharityNameController.onPageLoad(NormalMode).url
          )

        }
      }

      "should redirect to R1.9 when in CheckMode" in {
        val hmrcRef = "AA12356"
        val answers = RepaymentClaimDetailsAnswers(
          claimingReferenceNumber = Some(true),
          hmrcCharitiesReference = Some("AA12355")
        )

        val sessionData = SessionData
          .empty("AA12356")
          .copy(
            repaymentClaimDetailsAnswers = Some(answers)
          )

        (mockClaimsConnector
          .hasUnsubmittedClaim(_: String)(using _: HeaderCarrier))
          .expects(hmrcRef, *)
          .returning(Future(false))

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CharitiesReferenceNumberInputController.onPageLoad(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "AA12356")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )

        }
      }

      "should redirect to R1.9 when in CheckMode and answer does not change" in {
        val answers = RepaymentClaimDetailsAnswers(
          claimingReferenceNumber = Some(true),
          hmrcCharitiesReference = Some("AA12356")
        )

        val sessionData = SessionData
          .empty("AA12356")
          .copy(
            repaymentClaimDetailsAnswers = Some(answers)
          )

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CharitiesReferenceNumberInputController.onPageLoad(CheckMode).url)
              .withFormUrlEncodedBody("value" -> "AA12356")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url
          )

        }
      }

      "should render ClaimCompleteController if submissionReference is defined" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          lastUpdatedReference = Some(testCharitiesReference),
          submissionReference = Some(testCharitiesReference)
        )

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.CharitiesReferenceNumberInputController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }

      "should redirect to the R2 claims list for an organisation user - normalmode" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.CharitiesReferenceNumberInputController.onSubmit(NormalMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should return BadRequest when invalid data is submitted" in {
        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          val request =
            FakeRequest(POST, routes.CharitiesReferenceNumberInputController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody("value" -> "invalid")

          val result = route(application, request).value

          status(result) shouldEqual BAD_REQUEST
        }
      }

      "should redirect to the R2 claims list for an organisation user - checkmode" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.CharitiesReferenceNumberInputController.onSubmit(CheckMode).url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }
    }
  }
}

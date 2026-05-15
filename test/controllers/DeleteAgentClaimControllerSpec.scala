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

package controllers

import play.api.test.FakeRequest
import play.api.mvc.AnyContentAsEmpty
import controllers.ControllerSpec
import config.FrontendAppConfig
import views.html.DeleteAgentClaimView
import play.api.Application
import uk.gov.hmrc.auth.core.AffinityGroup
import models.SessionData
import models.RepaymentClaimDetailsAnswers
import forms.YesNoFormProvider
import play.api.mvc.AnyContentAsFormUrlEncoded
import connectors.ClaimsConnector
import services.SaveService
import play.api.inject.bind
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.Future

class DeleteAgentClaimControllerSpec extends ControllerSpec {

  "DeleteAgentClaimController" - {
    "onPageLoad" - {
      "should render the page correctly for an agent" in {
        given application: Application = applicationBuilder(
          sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            unsubmittedClaimId = Some("test-claim-123"),
            repaymentClaimDetailsAnswers = Some(
              RepaymentClaimDetailsAnswers(
                nameOfCharity = Some("Test Charity ABC")
              )
            )
          ),
          affinityGroup = AffinityGroup.Agent
        ).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.DeleteAgentClaimController.onPageLoad.url + "?claimId=test-claim-123")

          val result = route(application, request).value
          val view   = application.injector.instanceOf[DeleteAgentClaimView]
          val form   = application.injector.instanceOf[YesNoFormProvider]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(form(), "test-claim-123", "Test Charity ABC").body
        }
      }

      "should reject requests for an organisation" in {
        given application: Application = applicationBuilder(affinityGroup = AffinityGroup.Organisation).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.DeleteAgentClaimController.onPageLoad.url + "?claimId=test-claim-123")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should redirect to the claim task list when unsubmittedClaimId is not present and no claimId is provided" in {
        given application: Application = applicationBuilder(affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.DeleteAgentClaimController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should redirect to the charity repayment dashboard when unsubmittedClaimId is not present and claimId is provided" in {
        given application: Application = applicationBuilder(affinityGroup = AffinityGroup.Agent).build()
        val appConfig                  = application.injector.instanceOf[FrontendAppConfig]

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.DeleteAgentClaimController.onPageLoad.url + "?claimId=test-claim-123")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(appConfig.charityRepaymentDashboardUrl)
        }
      }

      "should redirect to the claim complete page when the claim is submitted" in {
        given application: Application = applicationBuilder(
          sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            unsubmittedClaimId = Some("test-claim-123"),
            submissionReference = Some("test-submission-123")
          ),
          affinityGroup = AffinityGroup.Agent
        ).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.DeleteAgentClaimController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
    }

    "onSubmit" - {
      "should delete the claim and redirect to the charity repayment dashboard when the user confirms" in {
        val mockClaimsConnector: ClaimsConnector = mock[ClaimsConnector]
        val mockSaveService: SaveService         = mock[SaveService]

        given application: Application = applicationBuilder(
          sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            unsubmittedClaimId = Some("test-claim-123"),
            repaymentClaimDetailsAnswers = Some(
              RepaymentClaimDetailsAnswers(
                nameOfCharity = Some("Test Charity ABC")
              )
            )
          ),
          affinityGroup = AffinityGroup.Agent
        ).overrides(
          bind[ClaimsConnector].toInstance(mockClaimsConnector),
          bind[SaveService].toInstance(mockSaveService)
        ).build()

        (mockClaimsConnector
          .deleteClaim(_: String)(using _: HeaderCarrier))
          .expects("test-claim-123", *)
          .returning(Future.successful(true))

        (mockSaveService
          .save(_: SessionData)(using _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteAgentClaimController.onSubmit.url + "?claimId=test-claim-123")
              .withFormUrlEncodedBody("value" -> "true", "claimId" -> "test-claim-123")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(appConfig.charityRepaymentDashboardUrl)
        }
      }

      "should delete the claim and redirect to the dashboard when the user confirms" in {
        val mockClaimsConnector: ClaimsConnector = mock[ClaimsConnector]
        val mockSaveService: SaveService         = mock[SaveService]

        given application: Application = applicationBuilder(
          sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            unsubmittedClaimId = Some("test-claim-123"),
            repaymentClaimDetailsAnswers = Some(
              RepaymentClaimDetailsAnswers(
                nameOfCharity = Some("Test Charity ABC")
              )
            )
          ),
          affinityGroup = AffinityGroup.Agent
        ).overrides(
          bind[ClaimsConnector].toInstance(mockClaimsConnector),
          bind[SaveService].toInstance(mockSaveService)
        ).build()

        (mockClaimsConnector
          .deleteClaim(_: String)(using _: HeaderCarrier))
          .expects("test-claim-123", *)
          .returning(Future.successful(true))

        (mockSaveService
          .save(_: SessionData)(using _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteAgentClaimController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true", "claimId" -> "test-claim-123")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(appConfig.charityRepaymentDashboardUrl)
        }
      }

      "should redirect to the charity repayment dashboard when claim deletion is not confirmed and claimId is provided" in {
        given application: Application = applicationBuilder(
          sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            unsubmittedClaimId = Some("test-claim-123"),
            repaymentClaimDetailsAnswers = Some(
              RepaymentClaimDetailsAnswers(
                nameOfCharity = Some("Test Charity ABC")
              )
            )
          ),
          affinityGroup = AffinityGroup.Agent
        ).build()

        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteAgentClaimController.onSubmit.url + "?claimId=test-claim-123")
              .withFormUrlEncodedBody("value" -> "false", "claimId" -> "test-claim-123")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(appConfig.charityRepaymentDashboardUrl)
        }
      }

      "should redirect to the claim task list when claim deletion is not confirmed and no claimId is provided" in {
        given application: Application = applicationBuilder(
          sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            unsubmittedClaimId = Some("test-claim-123"),
            repaymentClaimDetailsAnswers = Some(
              RepaymentClaimDetailsAnswers(
                nameOfCharity = Some("Test Charity ABC")
              )
            )
          ),
          affinityGroup = AffinityGroup.Agent
        ).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteAgentClaimController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "false", "claimId" -> "test-claim-123")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.ClaimsTaskListController.onPageLoad.url)
        }
      }

      "should reload the page with errors when a required field `value` is missing" in {
        given application: Application = applicationBuilder(
          sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            unsubmittedClaimId = Some("test-claim-123"),
            repaymentClaimDetailsAnswers = Some(
              RepaymentClaimDetailsAnswers(
                nameOfCharity = Some("Test Charity ABC")
              )
            )
          ),
          affinityGroup = AffinityGroup.Agent
        ).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteAgentClaimController.onSubmit.url)
              .withFormUrlEncodedBody("claimId" -> "test-claim-123")

          val result = route(application, request).value
          status(result) shouldEqual BAD_REQUEST
        }
      }

      "should throw an exception when a required field `claimId` is missing" in {
        given application: Application = applicationBuilder(
          sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            unsubmittedClaimId = Some("test-claim-123"),
            repaymentClaimDetailsAnswers = Some(
              RepaymentClaimDetailsAnswers(
                nameOfCharity = Some("Test Charity ABC")
              )
            )
          ),
          affinityGroup = AffinityGroup.Agent
        ).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.DeleteAgentClaimController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value
          a[RuntimeException] should be thrownBy result.futureValue
        }
      }
    }
  }

}

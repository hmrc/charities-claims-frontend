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

package controllers.claimDeclaration

import connectors.ClaimsConnector
import controllers.ControllerSpec
import models.*
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import views.html.RepaymentClaimSummaryView

import scala.concurrent.Future

class RepaymentClaimSummaryControllerSpec extends ControllerSpec {
  val mockClaimsConnector: ClaimsConnector = mock[ClaimsConnector]

  override protected val additionalBindings: List[GuiceableModule] = List(
    bind[ClaimsConnector].toInstance(mockClaimsConnector)
  )

  private val testClaimId   = "claim-123"
  private val submissionRef = "sub-ref"

  "ClaimDeclarationController" - {
    "on pageLoad" - {
      "should render the page correctly when isClaimSubmitted condition is met" in {
        val sessionData = SessionData
          .empty(testCharitiesReference)
          .copy(submissionReference = Some(submissionRef), unsubmittedClaimId = Some(testClaimId))

        val submissionSummaryResponse = SubmissionSummaryResponse(
          claimDetails = ClaimDetails("test charity", "test ref", "2026-04-07T11:34:21.147Z", "Mr John"),
          giftAidDetails = None,
          otherIncomeDetails = None,
          gasdsDetails = None,
          adjustmentDetails = None,
          submissionReferenceNumber = submissionRef
        )

        (mockClaimsConnector
          .getSubmissionClaimSummary(_: String)(using _: HeaderCarrier))
          .expects(submissionRef, *)
          .returning(Future.successful(submissionSummaryResponse))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RepaymentClaimSummaryController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[RepaymentClaimSummaryView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(submissionSummaryResponse).body
        }
      }

      "should redirect to ClaimsTaskListController if isClaimSubmitted is false" in {
        val sessionData = SessionData.empty(testCharitiesReference).copy(submissionReference = None)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RepaymentClaimSummaryController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }
    }
  }
}

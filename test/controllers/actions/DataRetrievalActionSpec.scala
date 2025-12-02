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

package controllers.actions

import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers.*
import util.{BaseSpec, TestClaims}
import connectors.ClaimsConnector
import uk.gov.hmrc.auth.core.AffinityGroup
import models.{GetClaimsResponse, RepaymentClaimDetailsAnswers, SessionData}
import models.requests.{AuthorisedRequest, DataRequest}
import play.api.mvc.Results.*
import repositories.SessionCache
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class DataRetrievalActionSpec extends BaseSpec {

  val request                      = FakeRequest("GET", "/test")
  val authorisedRequestOrgnisation = AuthorisedRequest(request, AffinityGroup.Organisation)
  val authorisedRequestAgent       = AuthorisedRequest(request, AffinityGroup.Agent)

  given SessionData = SessionData(None)

  "DataRetrievalAction" - {
    "refines AuthorisedRequest into a DataRequest when session data exists" in {
      val mockSessionCache    = mock[SessionCache]
      val mockClaimsConnector = mock[ClaimsConnector]
      val action              = new DefaultDataRetrievalAction(mockSessionCache, mockClaimsConnector, testFrontendAppConfig)

      val sessionData = RepaymentClaimDetailsAnswers.setClaimingTaxDeducted(true)

      (mockSessionCache
        .get()(using _: HeaderCarrier))
        .expects(*)
        .returning(Future.successful(Some(sessionData)))

      val result = action.invokeBlock(
        authorisedRequestOrgnisation,
        (req: DataRequest[?]) =>
          req.sessionData shouldBe sessionData
          Future.successful(Ok)
      )
      status(result) shouldBe OK
    }

    "refines AuthorisedRequest into a DataRequest when session data object doesn't exist and no claims are retrieved from backend" in {
      val mockSessionCache    = mock[SessionCache]
      val mockClaimsConnector = mock[ClaimsConnector]
      val action              = new DefaultDataRetrievalAction(mockSessionCache, mockClaimsConnector, testFrontendAppConfig)

      (mockSessionCache
        .get()(using _: HeaderCarrier))
        .expects(*)
        .returning(Future.successful(None))

      (mockClaimsConnector
        .retrieveUnsubmittedClaims(using _: HeaderCarrier))
        .expects(*)
        .returning(Future.successful(GetClaimsResponse(claimsCount = 0, claimsList = Seq())))

      val result = action.invokeBlock(
        authorisedRequestOrgnisation,
        (req: DataRequest[?]) =>
          req.sessionData shouldBe SessionData()
          Future.successful(Ok)
      )
      status(result) shouldBe OK
    }

    "refines AuthorisedRequest into a DataRequest when session data object doesn't exist and some claims are retrieved from backend" in {
      val mockSessionCache    = mock[SessionCache]
      val mockClaimsConnector = mock[ClaimsConnector]
      val action              = new DefaultDataRetrievalAction(mockSessionCache, mockClaimsConnector, testFrontendAppConfig)

      (mockSessionCache
        .get()(using _: HeaderCarrier))
        .expects(*)
        .returning(Future.successful(None))

      (mockClaimsConnector
        .retrieveUnsubmittedClaims(using _: HeaderCarrier))
        .expects(*)
        .returning(
          Future.successful(
            GetClaimsResponse(
              claimsCount = 1,
              claimsList = Seq(TestClaims.testClaimWithRepaymentClaimDetailsOnly())
            )
          )
        )

      val result = action.invokeBlock(
        authorisedRequestOrgnisation,
        (req: DataRequest[?]) =>
          req.sessionData shouldBe
            SessionData.from(TestClaims.testClaimWithRepaymentClaimDetailsOnly())

          req.sessionData.repaymentClaimDetailsAnswers   shouldBe Some(
            RepaymentClaimDetailsAnswers.from(
              TestClaims.testClaimWithRepaymentClaimDetailsOnly().claimData.repaymentClaimDetails
            )
          )
          req.sessionData.organisationDetailsAnswers     shouldBe None
          req.sessionData.giftAidScheduleDataAnswers     shouldBe None
          req.sessionData.declarationDetailsAnswers      shouldBe None
          req.sessionData.otherIncomeScheduleDataAnswers shouldBe None
          req.sessionData.gasdsScheduleDataAnswers       shouldBe None
          Future.successful(Ok)
      )
      status(result) shouldBe OK
    }

    "refines AuthorisedRequest into a DataRequest when session data exists when user is agent" in {
      val mockSessionCache    = mock[SessionCache]
      val mockClaimsConnector = mock[ClaimsConnector]
      val action              = new DefaultDataRetrievalAction(mockSessionCache, mockClaimsConnector, testFrontendAppConfig)

      val sessionData = RepaymentClaimDetailsAnswers.setClaimingTaxDeducted(true)

      (mockSessionCache
        .get()(using _: HeaderCarrier))
        .expects(*)
        .returning(Future.successful(Some(sessionData)))

      val result = action.invokeBlock(
        authorisedRequestAgent,
        (req: DataRequest[?]) =>
          req.sessionData shouldBe sessionData
          Future.successful(Ok)
      )
      status(result) shouldBe OK
    }

    "refines AuthorisedRequest into a DataRequest when session data object doesn't exist and no claims are retrieved from backend for agent" in {
      val mockSessionCache    = mock[SessionCache]
      val mockClaimsConnector = mock[ClaimsConnector]
      val action              = new DefaultDataRetrievalAction(mockSessionCache, mockClaimsConnector, testFrontendAppConfig)

      (mockSessionCache
        .get()(using _: HeaderCarrier))
        .expects(*)
        .returning(Future.successful(None))

      (mockClaimsConnector
        .retrieveUnsubmittedClaims(using _: HeaderCarrier))
        .expects(*)
        .returning(Future.successful(GetClaimsResponse(claimsCount = 0, claimsList = Seq())))

      val result = action.invokeBlock(
        authorisedRequestAgent,
        (req: DataRequest[?]) =>
          req.sessionData shouldBe SessionData()
          Future.successful(Ok)
      )
      status(result) shouldBe OK
    }

    "refines AuthorisedRequest into a DataRequest when session data object doesn't exist and no claims are retrieved from backend less than limit for agent" in {
      val mockSessionCache    = mock[SessionCache]
      val mockClaimsConnector = mock[ClaimsConnector]
      val action              = new DefaultDataRetrievalAction(mockSessionCache, mockClaimsConnector, testFrontendAppConfig)

      (mockSessionCache
        .get()(using _: HeaderCarrier))
        .expects(*)
        .returning(Future.successful(None))

      (mockClaimsConnector
        .retrieveUnsubmittedClaims(using _: HeaderCarrier))
        .expects(*)
        .returning(Future.successful(GetClaimsResponse(claimsCount = 2, claimsList = Seq())))

      val result =
        action.invokeBlock(authorisedRequestAgent, (_: DataRequest[?]) => ???) // never going to be executed
      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("page-for-agent-to-select-claim")
    }

    "refines AuthorisedRequest into a DataRequest when session data object doesn't exist and no claims are retrieved from backend and equal to limit for agent" in {
      val mockSessionCache    = mock[SessionCache]
      val mockClaimsConnector = mock[ClaimsConnector]
      val action              = new DefaultDataRetrievalAction(mockSessionCache, mockClaimsConnector, testFrontendAppConfig)

      (mockSessionCache
        .get()(using _: HeaderCarrier))
        .expects(*)
        .returning(Future.successful(None))

      (mockClaimsConnector
        .retrieveUnsubmittedClaims(using _: HeaderCarrier))
        .expects(*)
        .returning(Future.successful(GetClaimsResponse(claimsCount = 3, claimsList = Seq())))

      val result =
        action.invokeBlock(authorisedRequestAgent, (_: DataRequest[?]) => ???) // never going to be executed
      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("error-agent-unsubmitted-claim-limit-exceeded")
    }
  }
}

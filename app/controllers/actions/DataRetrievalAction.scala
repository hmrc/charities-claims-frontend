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

import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import models.SessionData
import models.requests.{AuthorisedRequest, DataRequest}
import play.api.mvc.{ActionRefiner, Result, Results}
import repositories.SessionCache
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import connectors.ClaimsConnector
import uk.gov.hmrc.auth.core.AffinityGroup

@ImplementedBy(classOf[DefaultDataRetrievalAction])
trait DataRetrievalAction extends ActionRefiner[AuthorisedRequest, DataRequest]

class DefaultDataRetrievalAction @Inject() (
  cache: SessionCache,
  claimsConnector: ClaimsConnector,
  config: FrontendAppConfig
)(using val executionContext: ExecutionContext)
    extends DataRetrievalAction {

  override protected def refine[A](
    request: AuthorisedRequest[A]
  ): Future[Either[Result, DataRequest[A]]] = {
    given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request.underlying, request.underlying.session)
    cache
      .get()
      .flatMap {
        case None              =>
          claimsConnector.retrieveUnsubmittedClaims
            .map { getClaimsResponse =>
              request.affinityGroup match {
                case AffinityGroup.Organisation =>
                  if getClaimsResponse.claimsCount > 0
                  then
                    Right(
                      DataRequest(
                        request,
                        // safe to assume there is at least one claim
                        SessionData.from(getClaimsResponse.claimsList.head)
                      )
                    )
                  else Right(DataRequest(request, SessionData()))

                case AffinityGroup.Agent =>
                  getClaimsResponse.claimsCount match {
                    case 0 =>
                      Right(DataRequest(request, SessionData()))

                    case x if x > 0 && x < config.agentUnsubmittedClaimLimit =>
                      Left(
                        Results.Redirect(
                          // TODO: replace with correct url when ready
                          "page-for-agent-to-select-claim"
                        )
                      )
                    case _                                                   =>
                      Left(
                        Results.Redirect(
                          // TODO: replace with correct url when ready
                          "error-agent-unsubmitted-claim-limit-exceeded"
                        )
                      )
                  }
              }
            }
        case Some(sessionData) =>
          Future.successful(Right(DataRequest(request, sessionData)))

      }
  }
}

case class AgentOutOfLimitException() extends Exception()

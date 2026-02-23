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

import play.api.mvc.{ActionRefiner, Result, Results}
import com.google.inject.ImplementedBy
import connectors.{ClaimsConnector, ClaimsValidationConnector}
import config.FrontendAppConfig
import uk.gov.hmrc.auth.core.AffinityGroup
import models.SessionData
import repositories.SessionCache
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import models.requests.{AuthorisedRequest, DataRequest}

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

@ImplementedBy(classOf[DefaultDataRetrievalAction])
trait DataRetrievalAction extends ActionRefiner[AuthorisedRequest, DataRequest]

class DefaultDataRetrievalAction @Inject() (
  cache: SessionCache,
  claimsConnector: ClaimsConnector,
  claimsValidationConnector: ClaimsValidationConnector,
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
            .flatMap { getClaimsResponse =>
              request.affinityGroup match {
                case AffinityGroup.Organisation =>
                  getClaimsResponse.claimsList match
                    case claimInfo :: _ =>
                      claimsConnector.getClaim(claimInfo.claimId).flatMap {
                        case Some(claim) =>
                          claimsValidationConnector
                            .getUploadSummary(claim.claimId)
                            .flatMap { uploadsSummary =>
                              val sessionData =
                                SessionData.from(claim, request.charitiesReference, Some(uploadsSummary))
                              cache
                                .store(sessionData)
                                .map(_ => Right(DataRequest(request, sessionData)))
                            }
                        case None        =>
                          Future
                            .failed(new RuntimeException(s"claimId $claimInfo.claimId could not be found in backend"))
                      }
                    case _              =>
                      val sessionData = SessionData.empty(request.charitiesReference)
                      cache
                        .store(sessionData)
                        .map(_ => Right(DataRequest(request, sessionData)))

                case AffinityGroup.Agent =>
                  getClaimsResponse.claimsCount match {
                    case 0 =>
                      val sessionData = SessionData.empty(request.charitiesReference)
                      cache
                        .store(sessionData)
                        .map(_ => Right(DataRequest(request, sessionData)))

                    case x if x > 0 && x < config.agentUnsubmittedClaimLimit =>
                      Future.successful(
                        Left(
                          Results.Redirect(
                            // TODO: replace with correct url when ready
                            "page-for-agent-to-select-claim"
                          )
                        )
                      )

                    case _ =>
                      Future.successful(
                        Left(
                          Results.Redirect(
                            // TODO: replace with correct url when ready
                            "error-agent-unsubmitted-claim-limit-exceeded"
                          )
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

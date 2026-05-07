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
import play.api.Logging

import javax.inject.Inject

@ImplementedBy(classOf[DefaultDataRetrievalAction])
trait DataRetrievalAction extends ActionRefiner[AuthorisedRequest, DataRequest]

class DefaultDataRetrievalAction @Inject() (
  cache: SessionCache,
  claimsConnector: ClaimsConnector,
  claimsValidationConnector: ClaimsValidationConnector,
  config: FrontendAppConfig
)(using val executionContext: ExecutionContext)
    extends DataRetrievalAction
    with Logging {

  override def refine[A](
    request: AuthorisedRequest[A]
  ): Future[Either[Result, DataRequest[A]]] = {
    given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request.underlying, request.underlying.session)
    cache
      .get()
      .flatMap {
        case None =>
          logger.info(s"No session data in cache for ${request.charitiesReference}, retrieving from backend")
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
                          logger.error(s"Claim ${claimInfo.claimId} could not be found in backend")
                          Future
                            .failed(new RuntimeException(s"claim ${claimInfo.claimId} could not be found in backend"))
                      }
                    case _              =>
                      val sessionData = SessionData.empty(request.charitiesReference)
                      cache
                        .store(sessionData)
                        .map(_ => Right(DataRequest(request, sessionData)))

                case AffinityGroup.Agent =>
                  getClaimsResponse.claimsCount match {
                    case 0 =>
                      val sessionData = SessionData.empty(request.charitiesReference, true)
                      cache
                        .store(sessionData)
                        .map(_ => Right(DataRequest(request, sessionData)))

                    case unsubmittedClaimsCount =>
                      request.underlying.getQueryString("claimId") match {
                        case Some(claimId) if getClaimsResponse.claimsList.exists(_.claimId == claimId) =>
                          // in case when the claimId is provided and exists in the backend
                          // we should open that claim
                          claimsConnector.getClaim(claimId).flatMap {
                            case Some(claim) =>
                              claimsValidationConnector
                                .getUploadSummary(claim.claimId)
                                .flatMap { uploadsSummary =>
                                  val sessionData =
                                    SessionData.from(claim, request.charitiesReference, Some(uploadsSummary), true)
                                  cache.store(sessionData).map(_ => Right(DataRequest(request, sessionData)))
                                }

                            case None =>
                              Future.successful(Left(Results.Redirect(config.charityRepaymentDashboardUrl)))
                          }

                        case Some(claimId)
                            if claimId == "blank" && unsubmittedClaimsCount < config.agentUnsubmittedClaimLimit =>
                          // in case when the claimId is blank and unsubmitted claims count is less than a limit
                          // we should start a new claim
                          val sessionData = SessionData.empty(request.charitiesReference, true)
                          cache.store(sessionData).map(_ => Right(DataRequest(request, sessionData)))

                        case Some(claimId)
                            if claimId == "blank" && unsubmittedClaimsCount >= config.agentUnsubmittedClaimLimit =>
                          // in case when the claimId is blank and the unsubmitted claims count is greater than or equal to the limit
                          // we should redirect to the warning page
                          Future.successful(
                            Left(
                              Results.Redirect(controllers.routes.Warning11MaxClaimsReachedController.onPageLoad.url)
                            )
                          )

                        case Some(_) =>
                          // in case when the claimId is provided but does not exist in the backend
                          // we should redirect to the dashboard
                          Future.successful(Left(Results.Redirect(config.charityRepaymentDashboardUrl)))

                        case None if unsubmittedClaimsCount >= config.agentUnsubmittedClaimLimit =>
                          // in case when no claimId is provided and the unsubmitted claims count is greater than or equal to the limit
                          // we should redirect to the warning page
                          Future.successful(
                            Left(
                              Results.Redirect(controllers.routes.Warning11MaxClaimsReachedController.onPageLoad.url)
                            )
                          )

                        case None =>
                          // in case when no claimId is provided and unsubmitted claims count is less than a limit
                          // we should start a new claim
                          val sessionData = SessionData.empty(request.charitiesReference)
                          cache
                            .store(sessionData)
                            .map(_ => Right(DataRequest(request, sessionData)))
                      }
                  }
              }
            }

        case Some(sessionData) =>
          request.affinityGroup match {
            case AffinityGroup.Organisation =>
              Future.successful(Right(DataRequest(request, sessionData)))

            case AffinityGroup.Agent =>
              request.underlying.getQueryString("claimId") match {
                case Some(claimId) =>
                  tryOpenAgentClaimById(request, claimId)

                case None =>
                  Future.successful(Right(DataRequest(request, sessionData)))
              }
          }

      }
  }

  private def tryOpenAgentClaimById(request: AuthorisedRequest[?], claimId: String)(using HeaderCarrier) =
    claimsConnector.retrieveUnsubmittedClaims
      .flatMap { getClaimsResponse =>
        if getClaimsResponse.claimsList.exists(_.claimId == claimId)
        then
          claimsConnector
            .getClaim(claimId)
            .flatMap {
              case Some(claim) =>
                claimsValidationConnector
                  .getUploadSummary(claim.claimId)
                  .flatMap { uploadsSummary =>
                    val sessionData =
                      SessionData.from(
                        claim,
                        request.charitiesReference,
                        Some(uploadsSummary),
                        request.affinityGroup == AffinityGroup.Agent
                      )
                    cache.store(sessionData).map(_ => Right(DataRequest(request, sessionData)))
                  }

              case None =>
                Future.successful(Left(Results.Redirect(config.charityRepaymentDashboardUrl)))
            }
        else if claimId == "blank" then {
          if getClaimsResponse.claimsCount < config.agentUnsubmittedClaimLimit then {
            val sessionData =
              SessionData.empty(request.charitiesReference, request.affinityGroup == AffinityGroup.Agent)
            cache.store(sessionData).map(_ => Right(DataRequest(request, sessionData)))
          } else {
            Future.successful(
              Left(Results.Redirect(controllers.routes.Warning11MaxClaimsReachedController.onPageLoad.url))
            )
          }
        } else {
          Future.successful(Left(Results.Redirect(config.charityRepaymentDashboardUrl)))
        }
      }
}

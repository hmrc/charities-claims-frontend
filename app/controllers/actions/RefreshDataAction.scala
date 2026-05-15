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

import play.api.mvc.*
import com.google.inject.ImplementedBy
import javax.inject.Singleton
import connectors.{ClaimsConnector, ClaimsValidationConnector}
import models.SessionData
import repositories.SessionCache
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import models.requests.{AuthorisedRequest, DataRequest}

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject
import uk.gov.hmrc.auth.core.AffinityGroup
import config.FrontendAppConfig

@ImplementedBy(classOf[DefaultRefreshDataAction])
trait RefreshDataAction extends ActionRefiner[AuthorisedRequest, DataRequest]

@Singleton
class DefaultRefreshDataAction @Inject() (
  cache: SessionCache,
  claimsConnector: ClaimsConnector,
  claimsValidationConnector: ClaimsValidationConnector,
  dataRetrievalAction: DefaultDataRetrievalAction,
  config: FrontendAppConfig
)(using val executionContext: ExecutionContext)
    extends RefreshDataAction {

  override protected def refine[A](
    request: AuthorisedRequest[A]
  ): Future[Either[Result, DataRequest[A]]] = {
    given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request.underlying, request.underlying.session)
    request.affinityGroup match {
      case AffinityGroup.Agent        =>
        request.underlying.getQueryString("claimId") match {
          case Some(claimId) => tryOpenAgentClaimById(request, claimId)
          case None          => proceedWithCurrentClaim(request, acceptDraftOrEmptyClaim = false)
        }
      case AffinityGroup.Organisation =>
        request.underlying.getQueryString("claimId") match {
          case Some(claimId) if claimId == "blank" => tryCreateNewClaim(request)
          case _                                   => proceedWithCurrentClaim(request, acceptDraftOrEmptyClaim = true)
        }
    }
  }

  private def proceedWithCurrentClaim[A](request: AuthorisedRequest[A], acceptDraftOrEmptyClaim: Boolean)(using
    HeaderCarrier
  ) =
    cache
      .get()
      .flatMap {
        case Some(sessionData) if sessionData.submissionReference.isDefined =>
          Future.successful(
            Left(Results.Redirect(controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad))
          )

        case Some(sessionData) if sessionData.unsubmittedClaimId.isDefined =>
          claimsConnector
            .getClaim(sessionData.unsubmittedClaimId.get)
            .flatMap {
              case Some(claim) =>
                claimsValidationConnector
                  .getUploadSummary(claim.claimId)
                  .flatMap { uploadsSummary =>
                    val refreshedSessionData =
                      SessionData
                        .from(
                          claim,
                          request.charitiesReference,
                          Some(uploadsSummary),
                          request.affinityGroup == AffinityGroup.Agent
                        )
                        .copy(
                          unregulatedLimitExceeded = sessionData.unregulatedLimitExceeded,
                          unregulatedWarningBypassed = sessionData.unregulatedWarningBypassed
                        )
                    cache
                      .store(refreshedSessionData)
                      .map(_ => Right(DataRequest(request, refreshedSessionData)))
                  }
              case None        =>
                Future
                  .failed(
                    new RuntimeException(s"claimId ${sessionData.unsubmittedClaimId} could not be found in backend")
                  )
            }

        case _ =>
          if acceptDraftOrEmptyClaim
          then dataRetrievalAction.refine(request)
          else Future.successful(Left(Results.Redirect(config.charityRepaymentDashboardUrl)))
      }

  private def tryOpenAgentClaimById(request: AuthorisedRequest[?], claimId: String)(using HeaderCarrier) =
    claimsConnector.retrieveUnsubmittedClaims
      .flatMap { getClaimsResponse =>
        if getClaimsResponse.claimsList.exists(_.claimId == claimId)
        then {
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
        } else {
          Future.successful(Left(Results.Redirect(config.charityRepaymentDashboardUrl)))
        }
      }

  private def tryCreateNewClaim[A](request: AuthorisedRequest[A])(using HeaderCarrier) =
    cache
      .get()
      .flatMap {
        case Some(sessionData) if sessionData.submissionReference.isDefined =>
          // in case when session data exists and claim has been submitted
          // we should start a new claim
          val newSessionData = SessionData.empty(request.charitiesReference)
          cache
            .store(newSessionData)
            .map(_ => Right(DataRequest(request, newSessionData)))

        case Some(sessionData) if sessionData.unsubmittedClaimId.isDefined =>
          // in case when session data exists and an unsubmitted claim is associated with it
          // we should open that claim
          claimsConnector
            .getClaim(sessionData.unsubmittedClaimId.get)
            .flatMap {
              case Some(claim) =>
                claimsValidationConnector
                  .getUploadSummary(claim.claimId)
                  .flatMap { uploadsSummary =>
                    val refreshedSessionData =
                      SessionData
                        .from(claim, request.charitiesReference, Some(uploadsSummary))
                        .copy(
                          unregulatedLimitExceeded = sessionData.unregulatedLimitExceeded,
                          unregulatedWarningBypassed = sessionData.unregulatedWarningBypassed
                        )
                    cache
                      .store(refreshedSessionData)
                      .map(_ => Right(DataRequest(request, refreshedSessionData)))
                  }
              case None        =>
                Future
                  .failed(
                    new RuntimeException(s"claimId ${sessionData.unsubmittedClaimId} could not be found in backend")
                  )
            }

        case Some(_) =>
          // in case when session data exists but no claim is associated with it
          // we should proceed with the current claim
          dataRetrievalAction.refine(request)

        case None =>
          // in case when session data does not exist
          // we should start a new claim
          val newSessionData = SessionData.empty(request.charitiesReference)
          cache
            .store(newSessionData)
            .map(_ => Right(DataRequest(request, newSessionData)))
      }
}

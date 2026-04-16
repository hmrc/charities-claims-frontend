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
import connectors.{ClaimsConnector, ClaimsValidationConnector}
import models.SessionData
import repositories.SessionCache
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import models.requests.{AuthorisedRequest, DataRequest}

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

@ImplementedBy(classOf[DefaultRefreshDataAction])
trait RefreshDataAction extends ActionRefiner[AuthorisedRequest, DataRequest]

class DefaultRefreshDataAction @Inject() (
  cache: SessionCache,
  claimsConnector: ClaimsConnector,
  claimsValidationConnector: ClaimsValidationConnector,
  dataRetrievalAction: DefaultDataRetrievalAction
)(using val executionContext: ExecutionContext)
    extends RefreshDataAction {

  override protected def refine[A](
    request: AuthorisedRequest[A]
  ): Future[Either[Result, DataRequest[A]]] = {
    given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request.underlying, request.underlying.session)
    cache
      .get()
      .flatMap {
        case Some(sessionData) if sessionData.submissionReference.isDefined =>
          Future.successful(
            Left(Results.Redirect(controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad))
          )
        case Some(sessionData) if sessionData.unsubmittedClaimId.isDefined  =>
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
                        .copy(unregulatedLimitExceeded = sessionData.unregulatedLimitExceeded)
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
          dataRetrievalAction.refine(request)

      }
  }
}

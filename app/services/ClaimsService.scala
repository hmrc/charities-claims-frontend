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

package services

import com.google.inject.{ImplementedBy, Inject}
import connectors.ClaimsConnector
import uk.gov.hmrc.http.HeaderCarrier
import models.SessionData

import scala.concurrent.{ExecutionContext, Future}
import models.RepaymentClaimDetailsAnswers
import repositories.SessionCache

@ImplementedBy(classOf[ClaimsServiceImpl])
trait ClaimsService {

  /** Create new or update an existing claim and update the session data */
  def save(using hc: HeaderCarrier): Future[Unit]
}

class ClaimsServiceImpl @Inject() (
  sessionCache: SessionCache,
  connector: ClaimsConnector
)(using ec: ExecutionContext)
    extends ClaimsService {

  final def save(using hc: HeaderCarrier): Future[Unit] =
    sessionCache.get().flatMap {
      case None =>
        Future.failed(new RuntimeException("No session data found"))

      case Some(sessionData) =>
        sessionData.unsubmittedClaimId match {
          case None =>
            for
              repaymentClaimDetails <-
                Future.fromTry(
                  sessionData.repaymentClaimDetailsAnswers
                    .map(RepaymentClaimDetailsAnswers.toRepaymentClaimDetails(_))
                    .get
                )
              response              <- connector.saveClaim(repaymentClaimDetails)
              _                     <-
                sessionCache.store(
                  sessionData
                    .copy(
                      unsubmittedClaimId = Some(response.claimId),
                      lastUpdatedReference = Some(response.lastUpdatedReference)
                    )
                )
            yield ()

          case Some(claimId) =>
            for
              updateClaimRequest <- Future.fromTry(SessionData.toUpdateClaimRequest(sessionData))
              response           <- connector.updateClaim(claimId, updateClaimRequest)
              _                  <- sessionCache.store(
                                      sessionData.copy(
                                        lastUpdatedReference = Some(response.lastUpdatedReference)
                                      )
                                    )
            yield ()
        }
    }
}

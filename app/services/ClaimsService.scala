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
import connectors.{ClaimsConnector, MissingRequiredFieldsException}
import uk.gov.hmrc.http.HeaderCarrier
import models.{RepaymentClaimDetails, RepaymentClaimDetailsAnswers}
import utils.Required.required
import models.requests.DataRequest

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

@ImplementedBy(classOf[ClaimsServiceImpl])
trait ClaimsService {
  def save(using
    dataRequest: DataRequest[?],
    hc: HeaderCarrier
  ): Future[Unit]
}

class ClaimsServiceImpl @Inject() (saveService: SaveService, connector: ClaimsConnector)(using ec: ExecutionContext)
    extends ClaimsService {

  def save(using
    dataRequest: DataRequest[?],
    hc: HeaderCarrier
  ): Future[Unit] = {
    val session = dataRequest.sessionData
    session.unsubmittedClaimId match {
      case None =>
        for
          repaymentClaimDetails <- Future.fromTry(transform(session.repaymentClaimDetailsAnswers))
          claimId               <- connector.saveClaim(repaymentClaimDetails)
          _                     <- saveService.save(dataRequest.sessionData.copy(unsubmittedClaimId = Some(claimId)))
        yield ()

      case Some(claimId) =>
        updateClaim(claimId, session.repaymentClaimDetailsAnswers)
    }
  }

  private def updateClaim(
    claimId: String,
    repaymentClaimDetailsAnswers: RepaymentClaimDetailsAnswers
  )(using
    hc: HeaderCarrier
  ): Future[Unit] =
    connector
      .getClaim(claimId)
      .flatMap {
        case Some(existingClaim) =>
          for {
            repaymentClaimDetails <- Future.fromTry(transform(repaymentClaimDetailsAnswers))
            _                     <- connector.updateClaim(existingClaim.claimId, repaymentClaimDetails)
          } yield ()

        case None =>
          Future.failed(new RuntimeException("claimId exists in session but failed to fetch existing claim"))

      }

  private def transform(answers: RepaymentClaimDetailsAnswers): Try[RepaymentClaimDetails] =
    (
      for
        claimingGiftAid                          <- required(answers)(_.claimingGiftAid)
        claimingTaxDeducted                      <- required(answers)(_.claimingTaxDeducted)
        claimingUnderGiftAidSmallDonationsScheme <- required(answers)(_.claimingUnderGiftAidSmallDonationsScheme)
      yield RepaymentClaimDetails(
        claimingGiftAid,
        claimingTaxDeducted,
        claimingUnderGiftAidSmallDonationsScheme,
        answers.claimReferenceNumber
      )
    ).recoverWith(err => Failure(MissingRequiredFieldsException(err.getMessage)))
}

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

package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import connectors.ClaimsValidationConnector
import models.requests.DataRequest
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[ClaimsValidationServiceImpl])
trait ClaimsValidationService {
  def deleteGiftAidSchedule(using DataRequest[?], HeaderCarrier): Future[Unit]
}

@Singleton
class ClaimsValidationServiceImpl @Inject() (
  claimsValidationConnector: ClaimsValidationConnector
)(using ec: ExecutionContext)
    extends ClaimsValidationService {

  def deleteGiftAidSchedule(using request: DataRequest[?], hc: HeaderCarrier): Future[Unit] =
    deleteSchedule("GiftAid", request.sessionData.unsubmittedClaimId)

  private def deleteSchedule(validationType: String, claimIdOpt: Option[String])(using HeaderCarrier): Future[Unit] =
    claimIdOpt match {
      case Some(claimId) =>
        claimsValidationConnector
          .getUploadSummary(claimId)
          .flatMap { summaryResponse =>
            summaryResponse.uploads.find(_.validationType == validationType) match {
              case Some(upload) =>
                claimsValidationConnector
                  .deleteSchedule(claimId, upload.reference)
                  .map(_ => ())

              case None =>
                Future.failed(
                  new RuntimeException(s"No $validationType schedule upload found for claimId: $claimId")
                )
            }
          }

      case None =>
        Future.failed(
          new RuntimeException(s"No claimId found when attempting to delete $validationType schedule")
        )
    }
}

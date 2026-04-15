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

package util

import connectors.ClaimsValidationConnector
import uk.gov.hmrc.http.HeaderCarrier
import models.*

import scala.concurrent.Future

class FakeClaimsValidationConnector(
  uploads: Seq[UploadSummary]
) extends ClaimsValidationConnector {

  override def createUploadTracking(claimId: String, request: CreateUploadTrackingRequest)(using
    hc: HeaderCarrier
  ): Future[Boolean] = ???

  override def getUploadSummary(claimId: String)(using hc: HeaderCarrier): Future[GetUploadSummaryResponse] =
    Future.successful(GetUploadSummaryResponse(uploads = uploads))

  override def getUploadResult(claimId: String, reference: FileUploadReference)(using
    hc: HeaderCarrier
  ): Future[GetUploadResultResponse] = ???

  override def deleteSchedule(claimId: String, reference: FileUploadReference)(using
    hc: HeaderCarrier
  ): Future[DeleteScheduleResponse] = ???

  override def updateUploadStatus(claimId: String, reference: FileUploadReference, status: FileStatus)(using
    hc: HeaderCarrier
  ): Future[Boolean] = ???

  override def touchTtl(claimId: String)(using hc: HeaderCarrier): Future[Unit] = ???

}

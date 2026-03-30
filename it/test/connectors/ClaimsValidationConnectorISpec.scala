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

package connectors

import models.*
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.OK
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import utils.{ComponentSpecHelper, TestDataUtils, WiremockMethods}

class ClaimsValidationConnectorISpec
  extends ComponentSpecHelper
    with WiremockMethods with TestDataUtils {

  private val connector = app.injector.instanceOf[ClaimsValidationConnector]

  given HeaderCarrier = HeaderCarrier()

  private val reference = FileUploadReference("file-ref")

  "createUploadTracking" should {

    "return true when backend returns success" in {
      val request =
        CreateUploadTrackingRequest(
          reference = UpscanReference(reference),
          validationType = ValidationType.GiftAid,
          uploadUrl = "https://upload",
          initiateTimestamp = "2025-01-01T00:00:00Z",
          fields = Map("key" -> "value")
        )

      val response = SuccessResponse(success = true)

      when(POST, s"/charities-claims-validation/$claimId/create-upload-tracking", request)
        .thenReturn(OK, response)

      val result = connector.createUploadTracking(claimId, request).futureValue

      result shouldBe true
    }
  }

  "getUploadSummary" should {

    "return uploads when backend returns 200" in {
      val response =
        GetUploadSummaryResponse(
          uploads = Seq(
            UploadSummary(
              reference = reference,
              validationType = ValidationType.GiftAid,
              fileStatus = FileStatus.VALIDATED
            )
          )
        )

      when(GET, s"/charities-claims-validation/$claimId/upload-results")
        .thenReturn(OK, response)

      val result = connector.getUploadSummary(claimId).futureValue

      result.uploads.size shouldBe 1
    }
  }

  "getUploadResult" should {

    "return upload result when backend returns 200" in {

      val response =
        Json.parse(
          s"""
          {
            "reference": "$reference",
            "validationType": "GiftAid",
            "fileStatus": "VERIFYING"
          }
          """
        ).as[GetUploadResultResponse]

      when(GET, s"/charities-claims-validation/$claimId/upload-results/$reference")
        .thenReturn(OK, response)

      val result = connector.getUploadResult(claimId, reference).futureValue

      result.reference shouldBe reference
    }
  }

  "deleteSchedule" should {

    "return response when deletion succeeds" in {

      val response = DeleteScheduleResponse(success = true)

      when(DELETE, s"/charities-claims-validation/$claimId/upload-results/$reference")
        .thenReturn(OK, response)

      val result = connector.deleteSchedule(claimId, reference).futureValue

      result.success shouldBe true
    }
  }

  "updateUploadStatus" should {

    "return true when update succeeds" in {

      val request = UpdateUploadStatusRequest(FileStatus.VALIDATING)
      val response = SuccessResponse(success = true)

      when(PUT, s"/charities-claims-validation/$claimId/upload-results/$reference", request)
        .thenReturn(OK, response)

      val result = connector.updateUploadStatus(claimId, reference, FileStatus.VALIDATING).futureValue

      result shouldBe true
    }
  }
}

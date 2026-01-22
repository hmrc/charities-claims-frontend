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

import connectors.ClaimsValidationConnector
import models.{DeleteScheduleResponse, GetUploadSummaryResponse, SessionData, UploadSummary}
import models.requests.DataRequest
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import util.BaseSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ClaimsValidationServiceSpec extends BaseSpec {

  given HeaderCarrier = HeaderCarrier()

  val mockConnector: ClaimsValidationConnector = mock[ClaimsValidationConnector]

  val testUploadSummaryWithAll: GetUploadSummaryResponse = GetUploadSummaryResponse(
    uploads = Seq(
      UploadSummary(
        reference = "gift-aid-ref-123",
        validationType = "GiftAid",
        fileStatus = "VALIDATED",
        uploadUrl = None
      ),
      UploadSummary(
        reference = "other-income-ref-456",
        validationType = "OtherIncome",
        fileStatus = "VALIDATED",
        uploadUrl = None
      ),
      UploadSummary(
        reference = "community-buildings-ref-222",
        validationType = "CommunityBuildings",
        fileStatus = "VALIDATED",
        uploadUrl = None
      ),
      UploadSummary(
        reference = "connected-charities-ref-333",
        validationType = "ConnectedCharities",
        fileStatus = "VALIDATED",
        uploadUrl = None
      )
    )
  )

  val testUploadSummaryWithoutGiftAid: GetUploadSummaryResponse = GetUploadSummaryResponse(
    uploads = Seq(
      UploadSummary(
        reference = "other-income-ref-456",
        validationType = "OtherIncome",
        fileStatus = "VALIDATED",
        uploadUrl = None
      )
    )
  )

  val testUploadSummaryWithoutOtherIncome: GetUploadSummaryResponse = GetUploadSummaryResponse(
    uploads = Seq(
      UploadSummary(
        reference = "gift-aid-ref-123",
        validationType = "GiftAid",
        fileStatus = "VALIDATED",
        uploadUrl = None
      )
    )
  )

  val testUploadSummaryWithoutCommunityBuildings: GetUploadSummaryResponse = GetUploadSummaryResponse(
    uploads = Seq(
      UploadSummary(
        reference = "gift-aid-ref-123",
        validationType = "GiftAid",
        fileStatus = "VALIDATED",
        uploadUrl = None
      )
    )
  )

  val testUploadSummaryWithoutConnectedCharities: GetUploadSummaryResponse = GetUploadSummaryResponse(
    uploads = Seq(
      UploadSummary(
        reference = "gift-aid-ref-123",
        validationType = "GiftAid",
        fileStatus = "VALIDATED",
        uploadUrl = None
      )
    )
  )

  "ClaimsValidationService" - {

    "deleteGiftAidSchedule" - {

      "should delete the GiftAid schedule when claimId is present" in {
        val service          = new ClaimsValidationServiceImpl(mockConnector)
        val sessionData      = SessionData.empty.copy(unsubmittedClaimId = Some("test-claim-123"))
        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        (mockConnector
          .getUploadSummary(_: String)(using _: HeaderCarrier))
          .expects("test-claim-123", *)
          .returning(Future.successful(testUploadSummaryWithAll))

        (mockConnector
          .deleteSchedule(_: String, _: String)(using _: HeaderCarrier))
          .expects("test-claim-123", "gift-aid-ref-123", *)
          .returning(Future.successful(DeleteScheduleResponse(success = true)))

        await(service.deleteGiftAidSchedule)
      }

      "should fail when claimId is None" in {
        val service          = new ClaimsValidationServiceImpl(mockConnector)
        val sessionData      = SessionData.empty.copy(unsubmittedClaimId = None)
        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        val result = service.deleteGiftAidSchedule

        whenReady(result.failed) { (exception: Throwable) =>
          exception.getMessage should include("No claimId")
        }
      }

      "should fail when no GiftAid upload found" in {
        val service          = new ClaimsValidationServiceImpl(mockConnector)
        val sessionData      = SessionData.empty.copy(unsubmittedClaimId = Some("test-claim-789"))
        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        (mockConnector
          .getUploadSummary(_: String)(using _: HeaderCarrier))
          .expects("test-claim-789", *)
          .returning(Future.successful(testUploadSummaryWithoutGiftAid))

        val result = service.deleteGiftAidSchedule

        whenReady(result.failed) { (exception: Throwable) =>
          exception.getMessage should include("GiftAid")
          exception.getMessage should include("schedule upload")
        }
      }
    }

    "deleteOtherIncomeSchedule" - {

      "should delete the OtherIncome schedule when claimId is present" in {
        val service          = new ClaimsValidationServiceImpl(mockConnector)
        val sessionData      = SessionData.empty.copy(unsubmittedClaimId = Some("test-claim-456"))
        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        (mockConnector
          .getUploadSummary(_: String)(using _: HeaderCarrier))
          .expects("test-claim-456", *)
          .returning(Future.successful(testUploadSummaryWithAll))

        (mockConnector
          .deleteSchedule(_: String, _: String)(using _: HeaderCarrier))
          .expects("test-claim-456", "other-income-ref-456", *)
          .returning(Future.successful(DeleteScheduleResponse(success = true)))

        await(service.deleteOtherIncomeSchedule)
      }

      "should fail when claimId is None" in {
        val service          = new ClaimsValidationServiceImpl(mockConnector)
        val sessionData      = SessionData.empty.copy(unsubmittedClaimId = None)
        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        val result = service.deleteOtherIncomeSchedule

        whenReady(result.failed) { (exception: Throwable) =>
          exception.getMessage should include("No claimId")
        }
      }

      "should fail when no OtherIncome upload found" in {
        val service          = new ClaimsValidationServiceImpl(mockConnector)
        val sessionData      = SessionData.empty.copy(unsubmittedClaimId = Some("test-claim-999"))
        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        (mockConnector
          .getUploadSummary(_: String)(using _: HeaderCarrier))
          .expects("test-claim-999", *)
          .returning(Future.successful(testUploadSummaryWithoutOtherIncome))

        val result = service.deleteOtherIncomeSchedule

        whenReady(result.failed) { (exception: Throwable) =>
          exception.getMessage should include("OtherIncome")
          exception.getMessage should include("schedule upload")
        }
      }
    }

    "deleteCommunityBuildingsSchedule" - {

      "should delete the CommunityBuildings schedule when claimId is present" in {
        val service     = new ClaimsValidationServiceImpl(mockConnector)
        val sessionData = SessionData.empty.copy(unsubmittedClaimId = Some("test-claim-456"))

        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        (mockConnector
          .getUploadSummary(_: String)(using _: HeaderCarrier))
          .expects("test-claim-456", *)
          .returning(Future.successful(testUploadSummaryWithAll))

        (mockConnector
          .deleteSchedule(_: String, _: String)(using _: HeaderCarrier))
          .expects("test-claim-456", "community-buildings-ref-222", *)
          .returning(Future.successful(DeleteScheduleResponse(success = true)))

        await(service.deleteCommunityBuildingsSchedule)
      }

      "should fail when claimId is None" in {
        val service     = new ClaimsValidationServiceImpl(mockConnector)
        val sessionData = SessionData.empty.copy(unsubmittedClaimId = None)

        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        val result = service.deleteCommunityBuildingsSchedule

        whenReady(result.failed) { (exception: Throwable) =>
          exception.getMessage should include("No claimId")
        }
      }

      "should fail when no CommunityBuildings upload found" in {
        val service     = new ClaimsValidationServiceImpl(mockConnector)
        val sessionData = SessionData.empty.copy(unsubmittedClaimId = Some("test-claim-999"))

        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        (mockConnector
          .getUploadSummary(_: String)(using _: HeaderCarrier))
          .expects("test-claim-999", *)
          .returning(Future.successful(testUploadSummaryWithoutCommunityBuildings))

        val result = service.deleteCommunityBuildingsSchedule

        whenReady(result.failed) { (exception: Throwable) =>
          exception.getMessage should include("CommunityBuildings")
          exception.getMessage should include("schedule upload")
        }
      }
    }

    "deleteConnectedCharitiesSchedule" - {

      "should delete the ConnectedCharities schedule when claimId is present" in {
        val service     = new ClaimsValidationServiceImpl(mockConnector)
        val sessionData = SessionData.empty.copy(unsubmittedClaimId = Some("test-claim-456"))

        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        (mockConnector
          .getUploadSummary(_: String)(using _: HeaderCarrier))
          .expects("test-claim-456", *)
          .returning(Future.successful(testUploadSummaryWithAll))

        (mockConnector
          .deleteSchedule(_: String, _: String)(using _: HeaderCarrier))
          .expects("test-claim-456", "connected-charities-ref-333", *)
          .returning(Future.successful(DeleteScheduleResponse(success = true)))

        await(service.deleteConnectedCharitiesSchedule)
      }

      "should fail when claimId is None" in {
        val service     = new ClaimsValidationServiceImpl(mockConnector)
        val sessionData = SessionData.empty.copy(unsubmittedClaimId = None)

        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        val result = service.deleteConnectedCharitiesSchedule

        whenReady(result.failed) { (exception: Throwable) =>
          exception.getMessage should include("No claimId")
        }
      }

      "should fail when no ConnectedCharities upload found" in {
        val service     = new ClaimsValidationServiceImpl(mockConnector)
        val sessionData = SessionData.empty.copy(unsubmittedClaimId = Some("test-claim-999"))

        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        (mockConnector
          .getUploadSummary(_: String)(using _: HeaderCarrier))
          .expects("test-claim-999", *)
          .returning(Future.successful(testUploadSummaryWithoutConnectedCharities))

        val result = service.deleteConnectedCharitiesSchedule

        whenReady(result.failed) { (exception: Throwable) =>
          exception.getMessage should include("ConnectedCharities")
          exception.getMessage should include("schedule upload")
        }
      }
    }
  }
}

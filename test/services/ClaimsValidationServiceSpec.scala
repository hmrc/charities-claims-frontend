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
import models.{
  DeleteScheduleResponse,
  FileStatus,
  FileUploadReference,
  GetUploadSummaryResponse,
  SessionData,
  UploadSummary,
  ValidationType
}
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import util.BaseSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ClaimsValidationServiceSpec extends BaseSpec {

  given HeaderCarrier = HeaderCarrier()

  val mockSaveService: SaveService             = mock[SaveService]
  val mockClaimsService: ClaimsService         = mock[ClaimsService]
  val mockConnector: ClaimsValidationConnector = mock[ClaimsValidationConnector]

  val testUploadSummaryWithAll: GetUploadSummaryResponse = GetUploadSummaryResponse(
    uploads = Seq(
      UploadSummary(
        reference = FileUploadReference("gift-aid-ref-123"),
        validationType = ValidationType.GiftAid,
        fileStatus = FileStatus.VALIDATED,
        uploadUrl = None
      ),
      UploadSummary(
        reference = FileUploadReference("other-income-ref-456"),
        validationType = ValidationType.OtherIncome,
        fileStatus = FileStatus.VALIDATED,
        uploadUrl = None
      ),
      UploadSummary(
        reference = FileUploadReference("community-buildings-ref-222"),
        validationType = ValidationType.CommunityBuildings,
        fileStatus = FileStatus.VALIDATED,
        uploadUrl = None
      ),
      UploadSummary(
        reference = FileUploadReference("connected-charities-ref-333"),
        validationType = ValidationType.ConnectedCharities,
        fileStatus = FileStatus.VALIDATED,
        uploadUrl = None
      )
    )
  )

  val testUploadSummaryWithoutGiftAid: GetUploadSummaryResponse = GetUploadSummaryResponse(
    uploads = Seq(
      UploadSummary(
        reference = FileUploadReference("other-income-ref-456"),
        validationType = ValidationType.OtherIncome,
        fileStatus = FileStatus.VALIDATED,
        uploadUrl = None
      )
    )
  )

  val testUploadSummaryWithoutOtherIncome: GetUploadSummaryResponse = GetUploadSummaryResponse(
    uploads = Seq(
      UploadSummary(
        reference = FileUploadReference("gift-aid-ref-123"),
        validationType = ValidationType.GiftAid,
        fileStatus = FileStatus.VALIDATED,
        uploadUrl = None
      )
    )
  )

  val testUploadSummaryWithoutCommunityBuildings: GetUploadSummaryResponse = GetUploadSummaryResponse(
    uploads = Seq(
      UploadSummary(
        reference = FileUploadReference("gift-aid-ref-123"),
        validationType = ValidationType.GiftAid,
        fileStatus = FileStatus.VALIDATED,
        uploadUrl = None
      )
    )
  )

  val testUploadSummaryWithoutConnectedCharities: GetUploadSummaryResponse = GetUploadSummaryResponse(
    uploads = Seq(
      UploadSummary(
        reference = FileUploadReference("gift-aid-ref-123"),
        validationType = ValidationType.GiftAid,
        fileStatus = FileStatus.VALIDATED,
        uploadUrl = None
      )
    )
  )

  "ClaimsValidationService" - {

    "deleteGiftAidSchedule" - {

      "should delete the GiftAid schedule when claimId is present" in {
        val service          = new ClaimsValidationServiceImpl(mockSaveService, mockClaimsService, mockConnector)
        val sessionData      = SessionData.empty(testCharitiesReference).copy(unsubmittedClaimId = Some("test-claim-123"))
        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        when(mockConnector.getUploadSummary(eqTo("test-claim-123"))(using any()))
          .thenReturn(Future.successful(testUploadSummaryWithAll))

        when(
          mockConnector.deleteSchedule(eqTo("test-claim-123"), eqTo(FileUploadReference("gift-aid-ref-123")))(using
            any()
          )
        ).thenReturn(Future.successful(DeleteScheduleResponse(success = true)))

        when(
          mockSaveService.save(
            eqTo(
              sessionData.copy(
                giftAidScheduleFileUploadReference = None,
                giftAidScheduleData = None,
                prevOverclaimedGiftAid = None
              )
            )
          )(using any())
        ).thenReturn(Future.successful(()))

        when(mockClaimsService.save(using any())).thenReturn(Future.successful(()))

        await(service.deleteGiftAidSchedule)
      }

      "should fail when claimId is None" in {
        val service          = new ClaimsValidationServiceImpl(mockSaveService, mockClaimsService, mockConnector)
        val sessionData      = SessionData.empty(testCharitiesReference).copy(unsubmittedClaimId = None)
        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        val result = service.deleteGiftAidSchedule

        whenReady(result.failed) { (exception: Throwable) =>
          exception.getMessage should include("No claimId")
        }
      }

      "should proceed even when no GiftAid upload found" in {
        val service          = new ClaimsValidationServiceImpl(mockSaveService, mockClaimsService, mockConnector)
        val sessionData      = SessionData.empty(testCharitiesReference).copy(unsubmittedClaimId = Some("test-claim-789"))
        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        when(mockConnector.getUploadSummary(eqTo("test-claim-789"))(using any()))
          .thenReturn(Future.successful(testUploadSummaryWithoutGiftAid))

        when(
          mockSaveService.save(
            eqTo(
              sessionData.copy(
                giftAidScheduleFileUploadReference = None,
                giftAidScheduleData = None,
                prevOverclaimedGiftAid = None
              )
            )
          )(using any())
        ).thenReturn(Future.successful(()))

        when(mockClaimsService.save(using any())).thenReturn(Future.successful(()))

        await(service.deleteGiftAidSchedule)
      }
    }

    "deleteOtherIncomeSchedule" - {

      "should delete the OtherIncome schedule when claimId is present" in {
        val service          = new ClaimsValidationServiceImpl(mockSaveService, mockClaimsService, mockConnector)
        val sessionData      = SessionData.empty(testCharitiesReference).copy(unsubmittedClaimId = Some("test-claim-456"))
        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        when(mockConnector.getUploadSummary(eqTo("test-claim-456"))(using any()))
          .thenReturn(Future.successful(testUploadSummaryWithAll))

        when(
          mockConnector.deleteSchedule(eqTo("test-claim-456"), eqTo(FileUploadReference("other-income-ref-456")))(using
            any()
          )
        ).thenReturn(Future.successful(DeleteScheduleResponse(success = true)))

        when(
          mockSaveService.save(
            eqTo(
              sessionData.copy(
                otherIncomeScheduleFileUploadReference = None,
                otherIncomeScheduleData = None,
                adjustmentForOtherIncomePreviousOverClaimed = None
              )
            )
          )(using any())
        ).thenReturn(Future.successful(()))

        when(mockClaimsService.save(using any())).thenReturn(Future.successful(()))

        await(service.deleteOtherIncomeSchedule)
      }

      "should fail when claimId is None" in {
        val service          = new ClaimsValidationServiceImpl(mockSaveService, mockClaimsService, mockConnector)
        val sessionData      = SessionData.empty(testCharitiesReference).copy(unsubmittedClaimId = None)
        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        val result = service.deleteOtherIncomeSchedule

        whenReady(result.failed) { (exception: Throwable) =>
          exception.getMessage should include("No claimId")
        }
      }

      "should proceed even when no OtherIncome upload found" in {
        val service          = new ClaimsValidationServiceImpl(mockSaveService, mockClaimsService, mockConnector)
        val sessionData      = SessionData.empty(testCharitiesReference).copy(unsubmittedClaimId = Some("test-claim-999"))
        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        when(mockConnector.getUploadSummary(eqTo("test-claim-999"))(using any()))
          .thenReturn(Future.successful(testUploadSummaryWithoutOtherIncome))

        when(
          mockSaveService.save(
            eqTo(
              sessionData.copy(
                otherIncomeScheduleFileUploadReference = None,
                otherIncomeScheduleData = None,
                adjustmentForOtherIncomePreviousOverClaimed = None
              )
            )
          )(using any())
        ).thenReturn(Future.successful(()))

        when(mockClaimsService.save(using any())).thenReturn(Future.successful(()))

        await(service.deleteOtherIncomeSchedule)
      }
    }

    "deleteCommunityBuildingsSchedule" - {

      "should delete the CommunityBuildings schedule when claimId is present" in {
        val service     = new ClaimsValidationServiceImpl(mockSaveService, mockClaimsService, mockConnector)
        val sessionData = SessionData.empty(testCharitiesReference).copy(unsubmittedClaimId = Some("test-claim-456"))

        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        when(mockConnector.getUploadSummary(eqTo("test-claim-456"))(using any()))
          .thenReturn(Future.successful(testUploadSummaryWithAll))

        when(
          mockConnector.deleteSchedule(
            eqTo("test-claim-456"),
            eqTo(FileUploadReference("community-buildings-ref-222"))
          )(using any())
        ).thenReturn(Future.successful(DeleteScheduleResponse(success = true)))

        when(
          mockSaveService.save(
            eqTo(
              sessionData.copy(
                communityBuildingsScheduleFileUploadReference = None,
                communityBuildingsScheduleData = None
              )
            )
          )(using any())
        ).thenReturn(Future.successful(()))

        when(mockClaimsService.save(using any())).thenReturn(Future.successful(()))

        await(service.deleteCommunityBuildingsSchedule)
      }

      "should fail when claimId is None" in {
        val service     = new ClaimsValidationServiceImpl(mockSaveService, mockClaimsService, mockConnector)
        val sessionData = SessionData.empty(testCharitiesReference).copy(unsubmittedClaimId = None)

        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        val result = service.deleteCommunityBuildingsSchedule

        whenReady(result.failed) { (exception: Throwable) =>
          exception.getMessage should include("No claimId")
        }
      }

      "should proceed even when no CommunityBuildings upload found" in {
        val service     = new ClaimsValidationServiceImpl(mockSaveService, mockClaimsService, mockConnector)
        val sessionData = SessionData.empty(testCharitiesReference).copy(unsubmittedClaimId = Some("test-claim-999"))

        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        when(mockConnector.getUploadSummary(eqTo("test-claim-999"))(using any()))
          .thenReturn(Future.successful(testUploadSummaryWithoutCommunityBuildings))

        when(
          mockSaveService.save(
            eqTo(
              sessionData.copy(
                communityBuildingsScheduleFileUploadReference = None,
                communityBuildingsScheduleData = None
              )
            )
          )(using any())
        ).thenReturn(Future.successful(()))

        when(mockClaimsService.save(using any())).thenReturn(Future.successful(()))

        await(service.deleteCommunityBuildingsSchedule)
      }
    }

    "deleteConnectedCharitiesSchedule" - {

      "should delete the ConnectedCharities schedule when claimId is present" in {
        val service     = new ClaimsValidationServiceImpl(mockSaveService, mockClaimsService, mockConnector)
        val sessionData = SessionData.empty(testCharitiesReference).copy(unsubmittedClaimId = Some("test-claim-456"))

        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        when(mockConnector.getUploadSummary(eqTo("test-claim-456"))(using any()))
          .thenReturn(Future.successful(testUploadSummaryWithAll))

        when(
          mockConnector.deleteSchedule(
            eqTo("test-claim-456"),
            eqTo(FileUploadReference("connected-charities-ref-333"))
          )(using any())
        ).thenReturn(Future.successful(DeleteScheduleResponse(success = true)))

        when(
          mockSaveService.save(
            eqTo(
              sessionData.copy(
                connectedCharitiesScheduleFileUploadReference = None,
                connectedCharitiesScheduleData = None
              )
            )
          )(using any())
        ).thenReturn(Future.successful(()))

        when(mockClaimsService.save(using any())).thenReturn(Future.successful(()))

        await(service.deleteConnectedCharitiesSchedule)
      }

      "should fail when claimId is None" in {
        val service     = new ClaimsValidationServiceImpl(mockSaveService, mockClaimsService, mockConnector)
        val sessionData = SessionData.empty(testCharitiesReference).copy(unsubmittedClaimId = None)

        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        val result = service.deleteConnectedCharitiesSchedule

        whenReady(result.failed) { (exception: Throwable) =>
          exception.getMessage should include("No claimId")
        }
      }

      "should proceed even when no ConnectedCharities upload found" in {
        val service     = new ClaimsValidationServiceImpl(mockSaveService, mockClaimsService, mockConnector)
        val sessionData = SessionData.empty(testCharitiesReference).copy(unsubmittedClaimId = Some("test-claim-999"))

        given DataRequest[?] = DataRequest(FakeRequest(), sessionData)

        when(mockConnector.getUploadSummary(eqTo("test-claim-999"))(using any()))
          .thenReturn(Future.successful(testUploadSummaryWithoutConnectedCharities))

        when(
          mockSaveService.save(
            eqTo(
              sessionData.copy(
                connectedCharitiesScheduleFileUploadReference = None,
                connectedCharitiesScheduleData = None
              )
            )
          )(using any())
        ).thenReturn(Future.successful(()))

        when(mockClaimsService.save(using any())).thenReturn(Future.successful(()))

        await(service.deleteConnectedCharitiesSchedule)
      }
    }
  }
}

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

import util.BaseSpec
import util.TestScheduleData
import connectors.{ClaimsValidationConnector, UnregulatedDonationsConnector}
import models.{
  FileUploadReference,
  GetUploadResultValidatedGiftAid,
  GiftAidScheduleData,
  OrganisationDetailsAnswers,
  ReasonNotRegisteredWithRegulator,
  SessionData
}
import models.requests.DataRequest
import uk.gov.hmrc.http.HeaderCarrier
import play.api.mvc.AnyContent
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnregulatedDonationsServiceSpec extends BaseSpec {

  val mockConnector: UnregulatedDonationsConnector             = mock[UnregulatedDonationsConnector]
  val mockClaimsValidationConnector: ClaimsValidationConnector = mock[ClaimsValidationConnector]

  val service = new UnregulatedDonationsServiceImpl(
    unregulatedDonationsConnector = mockConnector,
    claimsValidationConnector = mockClaimsValidationConnector,
    appConfig = testFrontendAppConfig
  )

  given HeaderCarrier = HeaderCarrier()

  "UnregulatedDonationsService - Function Tests" - {

    "getReasonNotRegistered" - {

      "should return None when reasonNotRegisteredWithRegulator is not set" in {
        val sessionData = SessionData.empty(testCharitiesReference)
        val result      = UnregulatedDonationsService.getReasonNotRegistered(sessionData)
        result shouldEqual None
      }

      "should return Some(LowIncome) when reason is LowIncome" in {
        val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
          ReasonNotRegisteredWithRegulator.LowIncome
        )(using SessionData.empty(testCharitiesReference))

        val result = UnregulatedDonationsService.getReasonNotRegistered(sessionData)
        result shouldEqual Some(ReasonNotRegisteredWithRegulator.LowIncome)
      }

      "should return Some(Excepted) when reason is Excepted" in {
        val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
          ReasonNotRegisteredWithRegulator.Excepted
        )(using SessionData.empty(testCharitiesReference))

        val result = UnregulatedDonationsService.getReasonNotRegistered(sessionData)
        result shouldEqual Some(ReasonNotRegisteredWithRegulator.Excepted)
      }

      "should return Some(Exempt) when reason is Exempt" in {
        val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
          ReasonNotRegisteredWithRegulator.Exempt
        )(using SessionData.empty(testCharitiesReference))

        val result = UnregulatedDonationsService.getReasonNotRegistered(sessionData)
        result shouldEqual Some(ReasonNotRegisteredWithRegulator.Exempt)
      }

      "should return Some(Waiting) when reason is Waiting" in {
        val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
          ReasonNotRegisteredWithRegulator.Waiting
        )(using SessionData.empty(testCharitiesReference))

        val result = UnregulatedDonationsService.getReasonNotRegistered(sessionData)
        result shouldEqual Some(ReasonNotRegisteredWithRegulator.Waiting)
      }
    }

    "extractGiftAidTotal" - {

      "should return the totalDonations value when present" in {
        val data   = TestScheduleData.exampleGiftAidScheduleData
        // exampleGiftAidScheduleData.totalDonations = Some(1000.00)
        val result = UnregulatedDonationsService.getGiftAidTotalDonations(data)
        result shouldEqual BigDecimal(1000.00)
      }

      "should return 0 when totalDonations is None" in {
        val data   = GiftAidScheduleData(
          totalDonations = None,
          donations = Seq.empty
        )
        val result = UnregulatedDonationsService.getGiftAidTotalDonations(data)
        result shouldEqual BigDecimal(0)
      }
    }

    "getLimitForReason" - {

      "should return Some(lowIncomeLimit) for LowIncome reason" in {
        val result = UnregulatedDonationsService.getLimitForReason(
          ReasonNotRegisteredWithRegulator.LowIncome,
          lowIncomeLimit = 5000,
          exceptedLimit = 100000
        )
        result shouldEqual Some(5000)
      }

      "should return Some(exceptedLimit) for Excepted reason" in {
        val result = UnregulatedDonationsService.getLimitForReason(
          ReasonNotRegisteredWithRegulator.Excepted,
          lowIncomeLimit = 5000,
          exceptedLimit = 100000
        )
        result shouldEqual Some(100000)
      }

      "should return None for Exempt reason" in {
        val result = UnregulatedDonationsService.getLimitForReason(
          ReasonNotRegisteredWithRegulator.Exempt,
          lowIncomeLimit = 5000,
          exceptedLimit = 100000
        )
        result shouldEqual None
      }

      "should return None for Waiting reason" in {
        val result = UnregulatedDonationsService.getLimitForReason(
          ReasonNotRegisteredWithRegulator.Waiting,
          lowIncomeLimit = 5000,
          exceptedLimit = 100000
        )
        result shouldEqual None
      }
    }

    "formatLimit" - {

      "should format 5000 as '5,000'" in {
        val result = UnregulatedDonationsService.formatLimit(5000)
        result shouldEqual "5,000"
      }

      "should format 100000 as '100,000'" in {
        val result = UnregulatedDonationsService.formatLimit(100000)
        result shouldEqual "100,000"
      }

      "should format 0 as '0'" in {
        val result = UnregulatedDonationsService.formatLimit(0)
        result shouldEqual "0"
      }
    }

    "calculateTotalDonations" - {

      "should add current claim total and existing donations" in {
        val result = UnregulatedDonationsService.calculateTotalDonations(
          currentClaimTotal = BigDecimal(1000),
          existingUnregulatedDonations = BigDecimal(2000)
        )
        result shouldEqual BigDecimal(3000)
      }

      "should handle zero values" in {
        val result = UnregulatedDonationsService.calculateTotalDonations(
          currentClaimTotal = BigDecimal(0),
          existingUnregulatedDonations = BigDecimal(0)
        )
        result shouldEqual BigDecimal(0)
      }

      "should handle decimal values" in {
        val result = UnregulatedDonationsService.calculateTotalDonations(
          currentClaimTotal = BigDecimal(1000.50),
          existingUnregulatedDonations = BigDecimal(2000.75)
        )
        result shouldEqual BigDecimal(3001.25)
      }
    }

    "isOverOrEqualToLimit" - {

      "should return true when totalDonations exceeds limit 5000 (5001)" in {
        val result = UnregulatedDonationsService.isOverOrEqualToLimit(
          totalDonations = BigDecimal(5001),
          limit = 5000
        )
        result shouldEqual true
      }

      "should return false when totalDonations is under limit 5000 (4999)" in {
        val result = UnregulatedDonationsService.isOverOrEqualToLimit(
          totalDonations = BigDecimal(4999),
          limit = 5000
        )
        result shouldEqual false
      }

      "should return false when totalDonations is just under limit 5000 (4999.99)" in {
        val result = UnregulatedDonationsService.isOverOrEqualToLimit(
          totalDonations = BigDecimal(4999.99),
          limit = 5000
        )
        result shouldEqual false
      }

      "should return false when totalDonations is just under limit 5000 (4999.999)" in {
        val result = UnregulatedDonationsService.isOverOrEqualToLimit(
          totalDonations = BigDecimal(4999.999),
          limit = 5000
        )
        result shouldEqual false
      }

      "should return true when totalDonations equals limit 5000 (at boundary)" in {
        val result = UnregulatedDonationsService.isOverOrEqualToLimit(
          totalDonations = BigDecimal(5000),
          limit = 5000
        )
        result shouldEqual true
      }

      "should return true when totalDonations exceeds limit 100000 (100001)" in {
        val result = UnregulatedDonationsService.isOverOrEqualToLimit(
          totalDonations = BigDecimal(100001),
          limit = 100000
        )
        result shouldEqual true
      }

      "should return false when totalDonations is under limit 100000 (99999)" in {
        val result = UnregulatedDonationsService.isOverOrEqualToLimit(
          totalDonations = BigDecimal(99999),
          limit = 100000
        )
        result shouldEqual false
      }

      "should return false when totalDonations is just under limit 100000 (99999.99)" in {
        val result = UnregulatedDonationsService.isOverOrEqualToLimit(
          totalDonations = BigDecimal(99999.99),
          limit = 100000
        )
        result shouldEqual false
      }

      "should return false when totalDonations is just under limit 100000 (99999.999)" in {
        val result = UnregulatedDonationsService.isOverOrEqualToLimit(
          totalDonations = BigDecimal(99999.999),
          limit = 100000
        )
        result shouldEqual false
      }

      "should return true when totalDonations equals limit 100000 (at boundary)" in {
        val result = UnregulatedDonationsService.isOverOrEqualToLimit(
          totalDonations = BigDecimal(100000),
          limit = 100000
        )
        result shouldEqual true
      }

    }

    "buildLimitExceededResult" - {

      "should build result with formatted limit for 5000" in {
        val result = UnregulatedDonationsService.buildLimitExceededResult(5000)
        result shouldEqual UnregulatedLimitExceeded(5000, "5,000")
      }

      "should build result with formatted limit for 100000" in {
        val result = UnregulatedDonationsService.buildLimitExceededResult(100000)
        result shouldEqual UnregulatedLimitExceeded(100000, "100,000")
      }
    }

    "checkIfOverOrEqualToLimit" - {

      "should return Some(UnregulatedLimitExceeded) when total exceeds limit 5000 (3000 + 3000)" in {
        val result = UnregulatedDonationsService.checkIfOverOrEqualToLimit(
          currentClaimTotal = BigDecimal(3000),
          existingUnregulatedDonations = BigDecimal(3000),
          limit = 5000
        )
        result shouldEqual Some(UnregulatedLimitExceeded(5000, "5,000"))
      }

      "should return None when total is under limit 5000 (2000 + 2000)" in {
        val result = UnregulatedDonationsService.checkIfOverOrEqualToLimit(
          currentClaimTotal = BigDecimal(1000),
          existingUnregulatedDonations = BigDecimal(1000),
          limit = 5000
        )
        result shouldEqual None
      }

      "should return None when total is just under limit 5000 (1000 + 3999.99)" in {
        val result = UnregulatedDonationsService.checkIfOverOrEqualToLimit(
          currentClaimTotal = BigDecimal(1000),
          existingUnregulatedDonations = BigDecimal(3999.99),
          limit = 5000
        )
        result shouldEqual None
      }

      "should return None when total is just under limit 5000 (1000 + 3999.999)" in {
        val result = UnregulatedDonationsService.checkIfOverOrEqualToLimit(
          currentClaimTotal = BigDecimal(1000),
          existingUnregulatedDonations = BigDecimal(3999.999),
          limit = 5000
        )
        result shouldEqual None
      }

      "should return Some(UnregulatedLimitExceeded) when total equals limit 5000 (at boundary)" in {
        val result = UnregulatedDonationsService.checkIfOverOrEqualToLimit(
          currentClaimTotal = BigDecimal(2500),
          existingUnregulatedDonations = BigDecimal(2500),
          limit = 5000
        )
        result shouldEqual Some(UnregulatedLimitExceeded(5000, "5,000"))
      }

      "should return Some(UnregulatedLimitExceeded) when total exceeds limit 100000 (50000 + 60000)" in {
        val result = UnregulatedDonationsService.checkIfOverOrEqualToLimit(
          currentClaimTotal = BigDecimal(50000),
          existingUnregulatedDonations = BigDecimal(60000),
          limit = 100000
        )
        result shouldEqual Some(UnregulatedLimitExceeded(100000, "100,000"))
      }

      "should return None when total is under limit 100000 (30000 + 30000)" in {
        val result = UnregulatedDonationsService.checkIfOverOrEqualToLimit(
          currentClaimTotal = BigDecimal(30000),
          existingUnregulatedDonations = BigDecimal(30000),
          limit = 100000
        )
        result shouldEqual None
      }

      "should return None when total is just under limit 100000 (10000 + 89999.99)" in {
        val result = UnregulatedDonationsService.checkIfOverOrEqualToLimit(
          currentClaimTotal = BigDecimal(10000),
          existingUnregulatedDonations = BigDecimal(89999.99),
          limit = 100000
        )
        result shouldEqual None
      }

      "should return None when total is just under limit 100000 (10000 + 89999.999)" in {
        val result = UnregulatedDonationsService.checkIfOverOrEqualToLimit(
          currentClaimTotal = BigDecimal(10000),
          existingUnregulatedDonations = BigDecimal(89999.999),
          limit = 100000
        )
        result shouldEqual None
      }

      "should return Some(UnregulatedLimitExceeded) when total equals limit 100000 (at boundary)" in {
        val result = UnregulatedDonationsService.checkIfOverOrEqualToLimit(
          currentClaimTotal = BigDecimal(10000),
          existingUnregulatedDonations = BigDecimal(90000),
          limit = 100000
        )
        result shouldEqual Some(UnregulatedLimitExceeded(100000, "100,000"))
      }
    }

    "getFormattedLimitForReason" - {

      "should return Some('5,000') for LowIncome" in {
        val result = UnregulatedDonationsService.getFormattedLimitForReason(
          reason = Some(ReasonNotRegisteredWithRegulator.LowIncome),
          lowIncomeLimit = 5000,
          exceptedLimit = 100000
        )
        result shouldEqual Some("5,000")
      }

      "should return Some('100,000') for Excepted" in {
        val result = UnregulatedDonationsService.getFormattedLimitForReason(
          reason = Some(ReasonNotRegisteredWithRegulator.Excepted),
          lowIncomeLimit = 5000,
          exceptedLimit = 100000
        )
        result shouldEqual Some("100,000")
      }

      "should return None for Exempt" in {
        val result = UnregulatedDonationsService.getFormattedLimitForReason(
          reason = Some(ReasonNotRegisteredWithRegulator.Exempt),
          lowIncomeLimit = 5000,
          exceptedLimit = 100000
        )
        result shouldEqual None
      }

      "should return None for Waiting" in {
        val result = UnregulatedDonationsService.getFormattedLimitForReason(
          reason = Some(ReasonNotRegisteredWithRegulator.Waiting),
          lowIncomeLimit = 5000,
          exceptedLimit = 100000
        )
        result shouldEqual None
      }

      "should return None when reason is None" in {
        val result = UnregulatedDonationsService.getFormattedLimitForReason(
          reason = None,
          lowIncomeLimit = 5000,
          exceptedLimit = 100000
        )
        result shouldEqual None
      }
    }
  }

  "UnregulatedDonationsServiceImpl - Service Integration Tests" - {

    "checkUnregulatedLimit" - {

      "should return None when reasonNotRegisteredWithRegulator is None (user has regulator)" in {
        val sessionData                        = SessionData.empty(testCharitiesReference)
        given request: DataRequest[AnyContent] =
          DataRequest(FakeRequest(), sessionData)

        val result = await(service.checkUnregulatedLimit)

        result shouldEqual None
      }

      "should return None when reasonNotRegisteredWithRegulator is Exempt" in {
        val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
          ReasonNotRegisteredWithRegulator.Exempt
        )(using SessionData.empty(testCharitiesReference))

        given request: DataRequest[AnyContent] =
          DataRequest(FakeRequest(), sessionData)

        val result = await(service.checkUnregulatedLimit)

        result shouldEqual None
      }

      "should return None when reasonNotRegisteredWithRegulator is Waiting" in {
        val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
          ReasonNotRegisteredWithRegulator.Waiting
        )(using SessionData.empty(testCharitiesReference))

        given request: DataRequest[AnyContent] =
          DataRequest(FakeRequest(), sessionData)

        val result = await(service.checkUnregulatedLimit)

        result shouldEqual None
      }

      // LowIncome Limit tests:

      "when charity reason is LowIncome" - {

        "should return None when total donations are under the Low Income limit" in {
          (mockConnector
            .getTotalUnregulatedDonations(_: String)(using _: HeaderCarrier))
            .expects(testCharitiesReference, *)
            .returning(Future.successful(Some(BigDecimal(1000))))
            .once()

          val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
            ReasonNotRegisteredWithRegulator.LowIncome
          )(using SessionData.empty(testCharitiesReference))

          given request: DataRequest[AnyContent] =
            DataRequest(FakeRequest(), sessionData)

          val result = await(service.checkUnregulatedLimit)

          result shouldEqual None
        }

        "should return Some(UnregulatedLimitExceeded) when total donations exceed the Low Income limit" in {
          (mockConnector
            .getTotalUnregulatedDonations(_: String)(using _: HeaderCarrier))
            .expects(testCharitiesReference, *)
            .returning(Future.successful(Some(BigDecimal(5001))))
            .once()

          val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
            ReasonNotRegisteredWithRegulator.LowIncome
          )(using SessionData.empty(testCharitiesReference))

          given request: DataRequest[AnyContent] =
            DataRequest(FakeRequest(), sessionData)

          val result = await(service.checkUnregulatedLimit)

          result shouldEqual Some(UnregulatedLimitExceeded(5000, "5,000"))
        }

        "should handle when connector returns None (no existing unregulated donations)" in {
          (mockConnector
            .getTotalUnregulatedDonations(_: String)(using _: HeaderCarrier))
            .expects(testCharitiesReference, *)
            .returning(Future.successful(None))
            .once()

          val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
            ReasonNotRegisteredWithRegulator.LowIncome
          )(using SessionData.empty(testCharitiesReference))

          given request: DataRequest[AnyContent] =
            DataRequest(FakeRequest(), sessionData)

          val result = await(service.checkUnregulatedLimit)

          // 0 (no existing) + 0 (no schedule data) = 0, which is under 5000
          result shouldEqual None
        }

        "should include schedule data in total calculation" in {
          val testFileRef = FileUploadReference("test-file-ref")

          (mockConnector
            .getTotalUnregulatedDonations(_: String)(using _: HeaderCarrier))
            .expects(testCharitiesReference, *)
            .returning(Future.successful(Some(BigDecimal(4500))))
            .once()

          // mock the validation connector to return gift aid data with totalDonations = 1000
          (mockClaimsValidationConnector
            .getUploadResult(_: String, _: FileUploadReference)(using _: HeaderCarrier))
            .expects(*, testFileRef, *)
            .returning(
              Future.successful(
                GetUploadResultValidatedGiftAid(
                  reference = testFileRef,
                  giftAidScheduleData = TestScheduleData.exampleGiftAidScheduleData
                )
              )
            )
            .once()

          val baseSessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
            ReasonNotRegisteredWithRegulator.LowIncome
          )(using SessionData.empty(testCharitiesReference))

          val sessionData = baseSessionData.copy(
            unsubmittedClaimId = Some("test-claim-id"),
            giftAidScheduleFileUploadReference = Some(testFileRef)
          )

          given request: DataRequest[AnyContent] =
            DataRequest(FakeRequest(), sessionData)

          val result = await(service.checkUnregulatedLimit)

          // 4500 (existing) + 1000 (schedule) = 5500, which exceeds 5000
          result shouldEqual Some(UnregulatedLimitExceeded(5000, "5,000"))
        }
      }

      // Excepted Limit tests:

      "when charity reason is Excepted" - {

        "should return None when total donations are under the limit" in {
          (mockConnector
            .getTotalUnregulatedDonations(_: String)(using _: HeaderCarrier))
            .expects(testCharitiesReference, *)
            .returning(Future.successful(Some(BigDecimal(50000))))
            .once()

          val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
            ReasonNotRegisteredWithRegulator.Excepted
          )(using SessionData.empty(testCharitiesReference))

          given request: DataRequest[AnyContent] =
            DataRequest(FakeRequest(), sessionData)

          val result = await(service.checkUnregulatedLimit)

          // 0 + 50000 < 100000
          result shouldEqual None
        }

        "should return Some(UnregulatedLimitExceeded) when total donations exceed the limit" in {
          (mockConnector
            .getTotalUnregulatedDonations(_: String)(using _: HeaderCarrier))
            .expects(testCharitiesReference, *)
            .returning(Future.successful(Some(BigDecimal(100001))))
            .once()

          val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
            ReasonNotRegisteredWithRegulator.Excepted
          )(using SessionData.empty(testCharitiesReference))

          given request: DataRequest[AnyContent] =
            DataRequest(FakeRequest(), sessionData)

          val result = await(service.checkUnregulatedLimit)

          result shouldEqual Some(UnregulatedLimitExceeded(100000, "100,000"))
        }
      }
    }

    "getApplicableLimit" - {

      "should return formatted LowIncome limit (5,000) for LowIncome charity" in {
        val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
          ReasonNotRegisteredWithRegulator.LowIncome
        )(using SessionData.empty(testCharitiesReference))

        given request: DataRequest[AnyContent] =
          DataRequest(FakeRequest(), sessionData)

        val result = service.getApplicableLimit

        result shouldEqual Some("5,000")
      }

      "should return formatted Excepted limit (100,000) for Excepted charity" in {
        val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
          ReasonNotRegisteredWithRegulator.Excepted
        )(using SessionData.empty(testCharitiesReference))

        given request: DataRequest[AnyContent] =
          DataRequest(FakeRequest(), sessionData)

        val result = service.getApplicableLimit

        result shouldEqual Some("100,000")
      }

      "should return None for Exempt charity" in {
        val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
          ReasonNotRegisteredWithRegulator.Exempt
        )(using SessionData.empty(testCharitiesReference))

        given request: DataRequest[AnyContent] =
          DataRequest(FakeRequest(), sessionData)

        val result = service.getApplicableLimit

        result shouldEqual None
      }

      "should return None for Waiting charity" in {
        val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
          ReasonNotRegisteredWithRegulator.Waiting
        )(using SessionData.empty(testCharitiesReference))

        given request: DataRequest[AnyContent] =
          DataRequest(FakeRequest(), sessionData)

        val result = service.getApplicableLimit

        result shouldEqual None
      }

      "should return None when reasonNotRegisteredWithRegulator is not set" in {
        val sessionData = SessionData.empty(testCharitiesReference)

        given request: DataRequest[AnyContent] =
          DataRequest(FakeRequest(), sessionData)

        val result = service.getApplicableLimit

        result shouldEqual None
      }
    }
  }
}

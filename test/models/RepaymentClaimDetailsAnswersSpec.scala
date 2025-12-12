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

package models

import util.BaseSpec
import play.api.libs.json.Json

class RepaymentClaimDetailsAnswersSpec extends BaseSpec {

  "RepaymentClaimDetailsAnswers" - {
    "be serializable and deserializable" in {
      val repaymentClaimDetailsAnswers = RepaymentClaimDetailsAnswers(
        claimingGiftAid = Some(true),
        claimingTaxDeducted = Some(true),
        claimingUnderGiftAidSmallDonationsScheme = Some(true),
        claimingReferenceNumber = Some(true),
        claimReferenceNumber = Some("1234567890")
      )

      val json                                     = Json.toJson(repaymentClaimDetailsAnswers)
      val deserializedRepaymentClaimDetailsAnswers = json.as[RepaymentClaimDetailsAnswers]
      deserializedRepaymentClaimDetailsAnswers shouldBe repaymentClaimDetailsAnswers
    }

    "be created from RepaymentClaimDetails when claimReferenceNumber is defined" in {
      val repaymentClaimDetails        = RepaymentClaimDetails(
        claimingGiftAid = true,
        claimingTaxDeducted = false,
        claimingUnderGiftAidSmallDonationsScheme = true,
        claimReferenceNumber = Some("foobar")
      )
      val repaymentClaimDetailsAnswers = RepaymentClaimDetailsAnswers.from(repaymentClaimDetails)
      repaymentClaimDetailsAnswers shouldBe RepaymentClaimDetailsAnswers(
        claimingGiftAid = Some(true),
        claimingTaxDeducted = Some(false),
        claimingUnderGiftAidSmallDonationsScheme = Some(true),
        claimingReferenceNumber = Some(true),
        claimReferenceNumber = Some("foobar")
      )
    }

    "be created from RepaymentClaimDetails when claimReferenceNumber is not defined" in {
      val repaymentClaimDetails        = RepaymentClaimDetails(
        claimingGiftAid = false,
        claimingTaxDeducted = false,
        claimingUnderGiftAidSmallDonationsScheme = true,
        claimReferenceNumber = None
      )
      val repaymentClaimDetailsAnswers = RepaymentClaimDetailsAnswers.from(repaymentClaimDetails)

      repaymentClaimDetailsAnswers shouldBe RepaymentClaimDetailsAnswers(
        claimingGiftAid = Some(false),
        claimingTaxDeducted = Some(false),
        claimingUnderGiftAidSmallDonationsScheme = Some(true),
        claimingReferenceNumber = Some(false),
        claimReferenceNumber = None
      )
    }

    "setClaimingGiftAid" - {
      "should delete giftAidScheduleDataAnswers when changing to false" in {
        given session: SessionData = SessionData.empty.copy(
          giftAidScheduleDataAnswers = Some(GiftAidScheduleDataAnswers())
        )

        val result = RepaymentClaimDetailsAnswers.setClaimingGiftAid(false)

        result.giftAidScheduleDataAnswers                   shouldBe None
        result.repaymentClaimDetailsAnswers.claimingGiftAid shouldBe Some(false)
      }

      "should keep giftAidScheduleDataAnswers when changing to true" in {
        given session: SessionData = SessionData.empty.copy(
          giftAidScheduleDataAnswers = Some(GiftAidScheduleDataAnswers())
        )

        val result = RepaymentClaimDetailsAnswers.setClaimingGiftAid(true)

        result.giftAidScheduleDataAnswers                   shouldBe Some(GiftAidScheduleDataAnswers())
        result.repaymentClaimDetailsAnswers.claimingGiftAid shouldBe Some(true)
      }
    }

    "shouldWarnAboutChangingClaimingGiftAid" - {
      "should return true when value is false when there is Gift Aid data" in {
        given session: SessionData = SessionData.empty.copy(
          giftAidScheduleDataAnswers = Some(GiftAidScheduleDataAnswers())
        )

        RepaymentClaimDetailsAnswers.shouldWarnAboutChangingClaimingGiftAid(false) shouldBe true
      }

      "should return false when setting value to true" in {
        given session: SessionData = SessionData.empty.copy(
          giftAidScheduleDataAnswers = Some(GiftAidScheduleDataAnswers())
        )

        RepaymentClaimDetailsAnswers.shouldWarnAboutChangingClaimingGiftAid(true) shouldBe false
      }

      "should return false when no Gift Aid data exists" in {
        given session: SessionData = SessionData.empty

        RepaymentClaimDetailsAnswers.shouldWarnAboutChangingClaimingGiftAid(false) shouldBe false
      }
    }

    "setClaimingTaxDeducted" - {
      "should delete otherIncomeScheduleDataAnswers when changing to false" in {
        given session: SessionData = SessionData.empty.copy(
          otherIncomeScheduleDataAnswers = Some(OtherIncomeScheduleDataAnswers())
        )

        val result = RepaymentClaimDetailsAnswers.setClaimingTaxDeducted(false)

        result.otherIncomeScheduleDataAnswers                   shouldBe None
        result.repaymentClaimDetailsAnswers.claimingTaxDeducted shouldBe Some(false)
      }

      "should keep otherIncomeScheduleDataAnswers when changing to true" in {
        given session: SessionData = SessionData.empty.copy(
          otherIncomeScheduleDataAnswers = Some(OtherIncomeScheduleDataAnswers())
        )

        val result = RepaymentClaimDetailsAnswers.setClaimingTaxDeducted(true)

        result.otherIncomeScheduleDataAnswers                   shouldBe Some(OtherIncomeScheduleDataAnswers())
        result.repaymentClaimDetailsAnswers.claimingTaxDeducted shouldBe Some(true)
      }
    }

    "shouldWarnAboutChangingClaimingTaxDeducted" - {
      "should return true when value is false when there is Other Income data" in {
        given session: SessionData = SessionData.empty.copy(
          otherIncomeScheduleDataAnswers = Some(OtherIncomeScheduleDataAnswers())
        )

        RepaymentClaimDetailsAnswers.shouldWarnAboutChangingClaimingTaxDeducted(false) shouldBe true
      }

      "should return false when setting value to true" in {
        given session: SessionData = SessionData.empty.copy(
          otherIncomeScheduleDataAnswers = Some(OtherIncomeScheduleDataAnswers())
        )

        RepaymentClaimDetailsAnswers.shouldWarnAboutChangingClaimingTaxDeducted(true) shouldBe false
      }

      "should return false when no Other Income data exists" in {
        given session: SessionData = SessionData.empty

        RepaymentClaimDetailsAnswers.shouldWarnAboutChangingClaimingTaxDeducted(false) shouldBe false
      }
    }

    "setClaimingUnderGasds" - {
      "should delete gasdsScheduleDataAnswers when changing to false" in {
        given session: SessionData = SessionData.empty.copy(
          gasdsScheduleDataAnswers = Some(GasdsScheduleDataAnswers())
        )

        val result = RepaymentClaimDetailsAnswers.setClaimingUnderGasds(false)

        result.gasdsScheduleDataAnswers                        shouldBe None
        result.repaymentClaimDetailsAnswers.claimingUnderGasds shouldBe Some(false)
      }

      "should keep gasdsScheduleDataAnswers when changing to true" in {
        given session: SessionData = SessionData.empty.copy(
          gasdsScheduleDataAnswers = Some(GasdsScheduleDataAnswers())
        )

        val result = RepaymentClaimDetailsAnswers.setClaimingUnderGasds(true)

        result.gasdsScheduleDataAnswers                        shouldBe Some(GasdsScheduleDataAnswers())
        result.repaymentClaimDetailsAnswers.claimingUnderGasds shouldBe Some(true)
      }
    }

    "shouldWarnAboutChangingClaimingUnderGasds" - {
      "should return true when value is false when there is GASDS data" in {
        given session: SessionData = SessionData.empty.copy(
          gasdsScheduleDataAnswers = Some(GasdsScheduleDataAnswers())
        )

        RepaymentClaimDetailsAnswers.shouldWarnAboutChangingClaimingUnderGasds(false) shouldBe true
      }

      "should return false when setting value to true" in {
        given session: SessionData = SessionData.empty.copy(
          gasdsScheduleDataAnswers = Some(GasdsScheduleDataAnswers())
        )

        RepaymentClaimDetailsAnswers.shouldWarnAboutChangingClaimingUnderGasds(true) shouldBe false
      }

      "should return false when no GASDS schedule data exists" in {
        given session: SessionData = SessionData.empty

        RepaymentClaimDetailsAnswers.shouldWarnAboutChangingClaimingUnderGasds(false) shouldBe false
      }
    }
  }
}

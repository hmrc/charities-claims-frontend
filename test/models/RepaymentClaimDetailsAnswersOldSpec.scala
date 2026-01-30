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

import util.{BaseSpec, TestScheduleData}
import play.api.libs.json.Json

class RepaymentClaimDetailsAnswersOldSpec extends BaseSpec {

  "RepaymentClaimDetailsAnswers" - {
    "be serializable and deserializable" in {
      val repaymentClaimDetailsAnswersOld = RepaymentClaimDetailsAnswersOld(
        claimingGiftAid = Some(true),
        claimingTaxDeducted = Some(true),
        claimingUnderGiftAidSmallDonationsScheme = Some(true),
        claimingReferenceNumber = Some(true),
        claimReferenceNumber = Some("1234567890")
      )

      val json                                        = Json.toJson(repaymentClaimDetailsAnswersOld)
      val deserializedRepaymentClaimDetailsAnswersOld = json.as[RepaymentClaimDetailsAnswersOld]
      deserializedRepaymentClaimDetailsAnswersOld shouldBe repaymentClaimDetailsAnswersOld
    }

    "be created from RepaymentClaimDetails when claimReferenceNumber is defined" in {
      val repaymentClaimDetails           = RepaymentClaimDetails(
        claimingGiftAid = true,
        claimingTaxDeducted = false,
        claimingUnderGiftAidSmallDonationsScheme = true,
        claimReferenceNumber = Some("foobar")
      )
      val repaymentClaimDetailsAnswersOld = RepaymentClaimDetailsAnswersOld.from(repaymentClaimDetails)
      repaymentClaimDetailsAnswersOld shouldBe RepaymentClaimDetailsAnswersOld(
        claimingGiftAid = Some(true),
        claimingTaxDeducted = Some(false),
        claimingUnderGiftAidSmallDonationsScheme = Some(true),
        claimingReferenceNumber = Some(true),
        claimReferenceNumber = Some("foobar")
      )
    }

    "be created from RepaymentClaimDetails when claimReferenceNumber is not defined" in {
      val repaymentClaimDetails           = RepaymentClaimDetails(
        claimingGiftAid = false,
        claimingTaxDeducted = false,
        claimingUnderGiftAidSmallDonationsScheme = true,
        claimReferenceNumber = None
      )
      val repaymentClaimDetailsAnswersOld = RepaymentClaimDetailsAnswersOld.from(repaymentClaimDetails)

      repaymentClaimDetailsAnswersOld shouldBe RepaymentClaimDetailsAnswersOld(
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
          giftAidScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
          giftAidScheduleData = Some(TestScheduleData.exampleGiftAidScheduleData)
        )

        val result = RepaymentClaimDetailsAnswersOld.setClaimingGiftAid(false)

        result.giftAidScheduleFileUploadReference              shouldBe None
        result.giftAidScheduleData                             shouldBe None
        result.repaymentClaimDetailsAnswersOld.claimingGiftAid shouldBe Some(false)
      }

      "should keep giftAidScheduleDataAnswers when changing to true" in {
        given session: SessionData = SessionData.empty.copy(
          giftAidScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
          giftAidScheduleData = Some(TestScheduleData.exampleGiftAidScheduleData)
        )

        val result = RepaymentClaimDetailsAnswersOld.setClaimingGiftAid(true)

        result.giftAidScheduleFileUploadReference              shouldBe Some(FileUploadReference("test-file-upload-reference"))
        result.giftAidScheduleData                             shouldBe Some(TestScheduleData.exampleGiftAidScheduleData)
        result.repaymentClaimDetailsAnswersOld.claimingGiftAid shouldBe Some(true)
      }
    }

    "shouldWarnAboutChangingClaimingGiftAid" - {
      "should return true when value is false when there is Gift Aid data" in {
        given session: SessionData = SessionData.empty.copy(
          giftAidScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
          giftAidScheduleData = Some(TestScheduleData.exampleGiftAidScheduleData)
        )

        RepaymentClaimDetailsAnswersOld.shouldWarnAboutChangingClaimingGiftAid(false) shouldBe true
      }

      "should return false when setting value to true" in {
        given session: SessionData = SessionData.empty.copy(
          giftAidScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
          giftAidScheduleData = Some(TestScheduleData.exampleGiftAidScheduleData)
        )

        RepaymentClaimDetailsAnswersOld.shouldWarnAboutChangingClaimingGiftAid(true) shouldBe false
      }

      "should return false when no Gift Aid data exists" in {
        given session: SessionData = SessionData.empty

        RepaymentClaimDetailsAnswersOld.shouldWarnAboutChangingClaimingGiftAid(false) shouldBe false
      }
    }

    "setClaimingTaxDeducted" - {
      "should delete otherIncomeScheduleDataAnswers when changing to false" in {
        given session: SessionData = SessionData.empty.copy(
          otherIncomeScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
          otherIncomeScheduleData = Some(TestScheduleData.exampleOtherIncomeScheduleData)
        )

        val result = RepaymentClaimDetailsAnswersOld.setClaimingTaxDeducted(false)

        result.otherIncomeScheduleData                             shouldBe None
        result.otherIncomeScheduleFileUploadReference              shouldBe None
        result.repaymentClaimDetailsAnswersOld.claimingTaxDeducted shouldBe Some(false)
      }

      "should keep otherIncomeScheduleDataAnswers when changing to true" in {
        given session: SessionData = SessionData.empty.copy(
          otherIncomeScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
          otherIncomeScheduleData = Some(TestScheduleData.exampleOtherIncomeScheduleData)
        )

        val result = RepaymentClaimDetailsAnswersOld.setClaimingTaxDeducted(true)

        result.otherIncomeScheduleFileUploadReference              shouldBe Some(FileUploadReference("test-file-upload-reference"))
        result.otherIncomeScheduleData                             shouldBe Some(TestScheduleData.exampleOtherIncomeScheduleData)
        result.repaymentClaimDetailsAnswersOld.claimingTaxDeducted shouldBe Some(true)
      }
    }

    "shouldWarnAboutChangingClaimingTaxDeducted" - {
      "should return true when value is false when there is Other Income data" in {
        given session: SessionData = SessionData.empty.copy(
          otherIncomeScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
          otherIncomeScheduleData = Some(TestScheduleData.exampleOtherIncomeScheduleData)
        )

        RepaymentClaimDetailsAnswersOld.shouldWarnAboutChangingClaimingTaxDeducted(false) shouldBe true
      }

      "should return false when setting value to true" in {
        given session: SessionData = SessionData.empty.copy(
          otherIncomeScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
          otherIncomeScheduleData = Some(TestScheduleData.exampleOtherIncomeScheduleData)
        )

        RepaymentClaimDetailsAnswersOld.shouldWarnAboutChangingClaimingTaxDeducted(true) shouldBe false
      }

      "should return false when no Other Income data exists" in {
        given session: SessionData = SessionData.empty

        RepaymentClaimDetailsAnswersOld.shouldWarnAboutChangingClaimingTaxDeducted(false) shouldBe false
      }
    }

    "setClaimingUnderGasds" - {
      "should delete gasdsScheduleDataAnswers when changing to false" in {
        given session: SessionData = SessionData.empty.copy(
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(GiftAidSmallDonationsSchemeDonationDetailsAnswers())
        )

        val result = RepaymentClaimDetailsAnswersOld.setClaimingUnderGiftAidSmallDonationsScheme(false)

        result.giftAidSmallDonationsSchemeDonationDetailsAnswers                        shouldBe None
        result.repaymentClaimDetailsAnswersOld.claimingUnderGiftAidSmallDonationsScheme shouldBe Some(false)
      }

      "should keep gasdsScheduleDataAnswers when changing to true" in {
        given session: SessionData = SessionData.empty.copy(
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(GiftAidSmallDonationsSchemeDonationDetailsAnswers())
        )

        val result = RepaymentClaimDetailsAnswersOld.setClaimingUnderGiftAidSmallDonationsScheme(true)

        result.giftAidSmallDonationsSchemeDonationDetailsAnswers                        shouldBe Some(
          GiftAidSmallDonationsSchemeDonationDetailsAnswers()
        )
        result.repaymentClaimDetailsAnswersOld.claimingUnderGiftAidSmallDonationsScheme shouldBe Some(true)
      }
    }

    "shouldWarnAboutChangingClaimingUnderGasds" - {
      "should return true when value is false when there is GASDS data" in {
        given session: SessionData = SessionData.empty.copy(
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(GiftAidSmallDonationsSchemeDonationDetailsAnswers())
        )

        RepaymentClaimDetailsAnswersOld.shouldWarnAboutChangingClaimingUnderGiftAidSmallDonationsScheme(
          false
        ) shouldBe true
      }

      "should return false when setting value to true" in {
        given session: SessionData = SessionData.empty.copy(
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(GiftAidSmallDonationsSchemeDonationDetailsAnswers())
        )

        RepaymentClaimDetailsAnswersOld.shouldWarnAboutChangingClaimingUnderGiftAidSmallDonationsScheme(
          true
        ) shouldBe false
      }

      "should return false when no GASDS schedule data exists" in {
        given session: SessionData = SessionData.empty

        RepaymentClaimDetailsAnswersOld.shouldWarnAboutChangingClaimingUnderGiftAidSmallDonationsScheme(
          false
        ) shouldBe false
      }
    }
  }

}

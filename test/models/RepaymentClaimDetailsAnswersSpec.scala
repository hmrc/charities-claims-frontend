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
import util.TestScheduleData

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
        given session: SessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            giftAidScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
            giftAidScheduleData = Some(TestScheduleData.exampleGiftAidScheduleData)
          )

        val result = RepaymentClaimDetailsAnswers.setClaimingGiftAid(false)

        result.giftAidScheduleFileUploadReference                 shouldBe None
        result.giftAidScheduleData                                shouldBe None
        result.repaymentClaimDetailsAnswers.value.claimingGiftAid shouldBe Some(false)
      }

      "should keep giftAidScheduleDataAnswers when changing to true" in {
        given session: SessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            giftAidScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
            giftAidScheduleData = Some(TestScheduleData.exampleGiftAidScheduleData)
          )

        val result = RepaymentClaimDetailsAnswers.setClaimingGiftAid(true)

        result.giftAidScheduleFileUploadReference                 shouldBe Some(FileUploadReference("test-file-upload-reference"))
        result.giftAidScheduleData                                shouldBe Some(TestScheduleData.exampleGiftAidScheduleData)
        result.repaymentClaimDetailsAnswers.value.claimingGiftAid shouldBe Some(true)
      }
    }

    "setClaimingTaxDeducted" - {
      "should delete otherIncomeScheduleDataAnswers when changing to false" in {
        given session: SessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            otherIncomeScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
            otherIncomeScheduleData = Some(TestScheduleData.exampleOtherIncomeScheduleData)
          )

        val result = RepaymentClaimDetailsAnswers.setClaimingTaxDeducted(false)

        result.otherIncomeScheduleData                                shouldBe None
        result.otherIncomeScheduleFileUploadReference                 shouldBe None
        result.repaymentClaimDetailsAnswers.value.claimingTaxDeducted shouldBe Some(false)
      }

      "should keep otherIncomeScheduleDataAnswers when changing to true" in {
        given session: SessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            otherIncomeScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
            otherIncomeScheduleData = Some(TestScheduleData.exampleOtherIncomeScheduleData)
          )

        val result = RepaymentClaimDetailsAnswers.setClaimingTaxDeducted(true)

        result.otherIncomeScheduleFileUploadReference                 shouldBe Some(FileUploadReference("test-file-upload-reference"))
        result.otherIncomeScheduleData                                shouldBe Some(TestScheduleData.exampleOtherIncomeScheduleData)
        result.repaymentClaimDetailsAnswers.value.claimingTaxDeducted shouldBe Some(true)
      }
    }

    "setClaimingUnderGasds" - {
      "should delete gasdsScheduleDataAnswers when changing to false" in {
        given session: SessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            giftAidSmallDonationsSchemeDonationDetailsAnswers =
              Some(GiftAidSmallDonationsSchemeDonationDetailsAnswers())
          )

        val result = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(false)

        result.giftAidSmallDonationsSchemeDonationDetailsAnswers                           shouldBe None
        result.repaymentClaimDetailsAnswers.value.claimingUnderGiftAidSmallDonationsScheme shouldBe Some(false)
      }

      "should keep gasdsScheduleDataAnswers when changing to true" in {
        given session: SessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            giftAidSmallDonationsSchemeDonationDetailsAnswers =
              Some(GiftAidSmallDonationsSchemeDonationDetailsAnswers())
          )

        val result = RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(true)

        result.giftAidSmallDonationsSchemeDonationDetailsAnswers                           shouldBe Some(
          GiftAidSmallDonationsSchemeDonationDetailsAnswers()
        )
        result.repaymentClaimDetailsAnswers.value.claimingUnderGiftAidSmallDonationsScheme shouldBe Some(true)
      }
    }
  }
}

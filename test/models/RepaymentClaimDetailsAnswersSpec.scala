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

import play.api.libs.json.Json
import util.{BaseSpec, TestScheduleData}

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

    "missingFields" - {
      "should return empty when GASDS is true but all sub-fields are false" in {
        val answers = RepaymentClaimDetailsAnswers(
          claimingGiftAid = Some(false),
          claimingTaxDeducted = Some(false),
          claimingUnderGiftAidSmallDonationsScheme = Some(true),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(false),
          connectedToAnyOtherCharities = Some(false),
          claimingReferenceNumber = Some(false)
        )

        answers.missingFields shouldBe empty
      }

      "should return true for hasRepaymentClaimDetailsCompleteAnswers when GASDS is true but all sub-fields are false" in {
        val answers = RepaymentClaimDetailsAnswers(
          claimingGiftAid = Some(false),
          claimingTaxDeducted = Some(false),
          claimingUnderGiftAidSmallDonationsScheme = Some(true),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(false),
          connectedToAnyOtherCharities = Some(false),
          claimingReferenceNumber = Some(false)
        )

        answers.hasRepaymentClaimDetailsCompleteAnswers() shouldBe true
      }
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

        val result = RepaymentClaimDetailsAnswers
          .setClaimingGiftAid(true)
          .and(SessionData.setUnsubmittedClaimId("claim-123"))

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

        val result =
          RepaymentClaimDetailsAnswers.setClaimingTaxDeducted(true).and(SessionData.setUnsubmittedClaimId("claim-123"))

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

    "getGasdsClaimType" - {

      "should return None when no answers in session" in {
        given session: SessionData = SessionData.empty(testCharitiesReference)

        RepaymentClaimDetailsAnswers.getGasdsClaimType shouldBe None
      }

      "should map all true values correctly" in {
        given session: SessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            repaymentClaimDetailsAnswers = Some(
              RepaymentClaimDetailsAnswers(
                claimingDonationsNotFromCommunityBuilding = Some(true),
                claimingDonationsCollectedInCommunityBuildings = Some(true),
                connectedToAnyOtherCharities = Some(true)
              )
            )
          )

        RepaymentClaimDetailsAnswers.getGasdsClaimType shouldBe Some(
          GasdsClaimType(
            topUp = true,
            communityBuildings = true,
            connectedCharity = true
          )
        )
      }

      "should return None when all GASDS fields are None" in {
        given session: SessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            repaymentClaimDetailsAnswers = Some(
              RepaymentClaimDetailsAnswers(
                claimingDonationsNotFromCommunityBuilding = None,
                claimingDonationsCollectedInCommunityBuildings = None,
                connectedToAnyOtherCharities = None
              )
            )
          )

        RepaymentClaimDetailsAnswers.getGasdsClaimType shouldBe None
      }

      "should return Some(false, false, false) when at least one field is defined but false" in {
        given session: SessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            repaymentClaimDetailsAnswers = Some(
              RepaymentClaimDetailsAnswers(
                claimingDonationsNotFromCommunityBuilding = Some(false)
              )
            )
          )

        RepaymentClaimDetailsAnswers.getGasdsClaimType shouldBe Some(
          GasdsClaimType(false, false, false)
        )
      }

      "should correctly map mixed values" in {
        given session: SessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            repaymentClaimDetailsAnswers = Some(
              RepaymentClaimDetailsAnswers(
                claimingDonationsNotFromCommunityBuilding = Some(true),
                claimingDonationsCollectedInCommunityBuildings = Some(false),
                connectedToAnyOtherCharities = None
              )
            )
          )

        RepaymentClaimDetailsAnswers.getGasdsClaimType shouldBe Some(
          GasdsClaimType(
            topUp = true,
            communityBuildings = false,
            connectedCharity = false
          )
        )
      }
    }

    "setGasdsClaimType" - {

      "should set all fields to true" in {
        given session: SessionData = SessionData.empty(testCharitiesReference)

        val result = RepaymentClaimDetailsAnswers.setGasdsClaimType(
          GasdsClaimType(
            topUp = true,
            communityBuildings = true,
            connectedCharity = true
          ),
          None
        )

        val answers = result.repaymentClaimDetailsAnswers.value

        answers.claimingDonationsNotFromCommunityBuilding      shouldBe Some(true)
        answers.claimingDonationsCollectedInCommunityBuildings shouldBe Some(true)
        answers.connectedToAnyOtherCharities                   shouldBe Some(true)
      }

      "should set all fields to false" in {
        given session: SessionData = SessionData.empty(testCharitiesReference)

        val result = RepaymentClaimDetailsAnswers.setGasdsClaimType(
          GasdsClaimType(
            topUp = false,
            communityBuildings = false,
            connectedCharity = false
          ),
          None
        )

        val answers = result.repaymentClaimDetailsAnswers.value

        answers.claimingDonationsNotFromCommunityBuilding      shouldBe Some(false)
        answers.claimingDonationsCollectedInCommunityBuildings shouldBe Some(false)
        answers.connectedToAnyOtherCharities                   shouldBe Some(false)
      }

      "should overwrite existing values" in {
        given session: SessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            repaymentClaimDetailsAnswers = Some(
              RepaymentClaimDetailsAnswers(
                claimingDonationsNotFromCommunityBuilding = Some(false),
                claimingDonationsCollectedInCommunityBuildings = Some(false),
                connectedToAnyOtherCharities = Some(false)
              )
            )
          )

        val result = RepaymentClaimDetailsAnswers.setGasdsClaimType(
          GasdsClaimType(
            topUp = true,
            communityBuildings = false,
            connectedCharity = true
          ),
          None
        )

        val answers = result.repaymentClaimDetailsAnswers.value

        answers.claimingDonationsNotFromCommunityBuilding      shouldBe Some(true)
        answers.claimingDonationsCollectedInCommunityBuildings shouldBe Some(false)
        answers.connectedToAnyOtherCharities                   shouldBe Some(true)
      }

      "should clear adjustment and GASDS details when removing topUp and communityBuildings" in {
        val prevAnswer = Some(GasdsClaimType(topUp = true, communityBuildings = true, connectedCharity = false))

        given session: SessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            giftAidSmallDonationsSchemeDonationDetailsAnswers =
              Some(GiftAidSmallDonationsSchemeDonationDetailsAnswers())
          )

        val result = RepaymentClaimDetailsAnswers.setGasdsClaimType(
          GasdsClaimType(
            topUp = false,
            communityBuildings = false,
            connectedCharity = true
          ),
          prevAnswer
        )

        result.giftAidSmallDonationsSchemeDonationDetailsAnswers shouldBe None
      }

      "should clear GASDS claims when topUp is false" in {
        given session: SessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
              GiftAidSmallDonationsSchemeDonationDetailsAnswers(
                adjustmentForGiftAidOverClaimed = Some(BigDecimal(45)),
                claims = Some(
                  Seq(
                    Some(GiftAidSmallDonationsSchemeClaimAnswers(2026, Some(100)))
                  )
                )
              )
            )
          )

        val result = RepaymentClaimDetailsAnswers.setGasdsClaimType(
          GasdsClaimType(
            topUp = false,
            communityBuildings = true,
            connectedCharity = false
          ),
          None
        )

        result.giftAidSmallDonationsSchemeDonationDetailsAnswers.value.claims shouldBe Some(Seq.empty)
      }

      "should NOT clear GASDS details when topUp or communityBuildings still selected" in {
        val prevAnswer = Some(GasdsClaimType(topUp = true, communityBuildings = false, connectedCharity = false))

        given session: SessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            giftAidSmallDonationsSchemeDonationDetailsAnswers =
              Some(GiftAidSmallDonationsSchemeDonationDetailsAnswers())
          )

        val result = RepaymentClaimDetailsAnswers.setGasdsClaimType(
          GasdsClaimType(
            topUp = true, // still selected
            communityBuildings = false,
            connectedCharity = true
          ),
          prevAnswer
        )

        result.giftAidSmallDonationsSchemeDonationDetailsAnswers shouldBe defined
      }
    }

    "setMakingAdjustmentToPreviousClaim" - {

      "should reset adjustment to 0 when set to false" in {
        given session: SessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
              GiftAidSmallDonationsSchemeDonationDetailsAnswers(
                adjustmentForGiftAidOverClaimed = Some(BigDecimal(45)),
                claims = Some(Seq(Some(GiftAidSmallDonationsSchemeClaimAnswers(2026, Some(100)))))
              )
            )
          )

        val result = RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(false)

        result.giftAidSmallDonationsSchemeDonationDetailsAnswers.value.adjustmentForGiftAidOverClaimed shouldBe Some(
          BigDecimal(0)
        )
      }
    }

    "setClaimingDonationsNotFromCommunityBuilding" - {

      "should clear claims but keep object when set to false" in {
        given session: SessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
              GiftAidSmallDonationsSchemeDonationDetailsAnswers(
                adjustmentForGiftAidOverClaimed = Some(BigDecimal(45)),
                claims = Some(Seq(Some(GiftAidSmallDonationsSchemeClaimAnswers(2026, Some(100)))))
              )
            )
          )

        val result = RepaymentClaimDetailsAnswers
          .setClaimingDonationsNotFromCommunityBuilding(false)

        result.giftAidSmallDonationsSchemeDonationDetailsAnswers.value.claims shouldBe Some(Seq.empty)
      }
    }

    "setRepaymentClaimType" - {

      "should remove entire GASDS data when switching from true to false" in {
        val prev = Some(RepaymentClaimType(true, true, true))

        given session: SessionData = SessionData
          .empty(testCharitiesReference)
          .copy(
            giftAidSmallDonationsSchemeDonationDetailsAnswers =
              Some(GiftAidSmallDonationsSchemeDonationDetailsAnswers())
          )

        val result = RepaymentClaimDetailsAnswers.setRepaymentClaimType(
          RepaymentClaimType(true, true, false),
          prev
        )

        result.giftAidSmallDonationsSchemeDonationDetailsAnswers shouldBe None
      }
    }
  }
}

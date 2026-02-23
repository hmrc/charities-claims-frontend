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

class SessionDataSpec extends BaseSpec {

  private val completeRepaymentClaimDetailsAnswers = RepaymentClaimDetailsAnswers(
    claimingGiftAid = Some(true),
    claimingTaxDeducted = Some(true),
    claimingUnderGiftAidSmallDonationsScheme = Some(false),
    claimingReferenceNumber = Some(false)
  )

  private val incompleteRepaymentClaimDetailsAnswers = RepaymentClaimDetailsAnswers(
    claimingGiftAid = Some(true),
    claimingTaxDeducted = Some(true),
    claimingUnderGiftAidSmallDonationsScheme = Some(false)
  )

  "SessionData" - {
    "be serializable and deserializable" in {
      val sessionData             = SessionData(
        charitiesReference = testCharitiesReference,
        repaymentClaimDetailsAnswers = Some(
          RepaymentClaimDetailsAnswers(
            claimingGiftAid = Some(true),
            claimingTaxDeducted = Some(true),
            claimingUnderGiftAidSmallDonationsScheme = Some(true),
            claimingReferenceNumber = Some(true),
            claimReferenceNumber = Some("1234567890")
          )
        ),
        organisationDetailsAnswers = Some(
          OrganisationDetailsAnswers(
            nameOfCharityRegulator = Some(NameOfCharityRegulator.EnglandAndWales),
            reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.LowIncome),
            charityRegistrationNumber = Some("1137948"),
            areYouACorporateTrustee = Some(true),
            nameOfCorporateTrustee = Some("Joe Bloggs"),
            corporateTrusteePostcode = Some("AB12 3YZ"),
            corporateTrusteeDaytimeTelephoneNumber = Some("071234567890")
          )
        )
      )
      val json                    = Json.toJson(sessionData)
      val deserializedSessionData = json.as[SessionData]
      deserializedSessionData shouldBe sessionData
    }
  }

  "shouldUploadGiftAidSchedule" - {
    "should return false when repaymentClaimDetailsAnswers is None" in {
      given SessionData = SessionData(
        charitiesReference = testCharitiesReference,
        unsubmittedClaimId = Some("claim-123")
      )
      SessionData.shouldUploadGiftAidSchedule shouldBe false
    }

    "should return false when answers are incomplete" in {
      given SessionData = SessionData(
        charitiesReference = testCharitiesReference,
        unsubmittedClaimId = Some("claim-123"),
        repaymentClaimDetailsAnswers = Some(incompleteRepaymentClaimDetailsAnswers)
      )
      SessionData.shouldUploadGiftAidSchedule shouldBe false
    }

    "should return false when unsubmittedClaimId is None" in {
      given SessionData = SessionData(
        charitiesReference = testCharitiesReference,
        repaymentClaimDetailsAnswers = Some(completeRepaymentClaimDetailsAnswers)
      )
      SessionData.shouldUploadGiftAidSchedule shouldBe false
    }

    "should return true when all conditions are met" in {
      given SessionData = SessionData(
        charitiesReference = testCharitiesReference,
        unsubmittedClaimId = Some("claim-123"),
        repaymentClaimDetailsAnswers = Some(completeRepaymentClaimDetailsAnswers)
      )
      SessionData.shouldUploadGiftAidSchedule shouldBe true
    }
  }

  "shouldUploadOtherIncomeSchedule" - {
    "should return false when repaymentClaimDetailsAnswers is None" in {
      given SessionData = SessionData(
        charitiesReference = testCharitiesReference,
        unsubmittedClaimId = Some("claim-123")
      )
      SessionData.shouldUploadOtherIncomeSchedule shouldBe false
    }

    "should return false when answers are incomplete" in {
      given SessionData = SessionData(
        charitiesReference = testCharitiesReference,
        unsubmittedClaimId = Some("claim-123"),
        repaymentClaimDetailsAnswers = Some(incompleteRepaymentClaimDetailsAnswers)
      )
      SessionData.shouldUploadOtherIncomeSchedule shouldBe false
    }

    "should return false when unsubmittedClaimId is None" in {
      given SessionData = SessionData(
        charitiesReference = testCharitiesReference,
        repaymentClaimDetailsAnswers = Some(completeRepaymentClaimDetailsAnswers)
      )
      SessionData.shouldUploadOtherIncomeSchedule shouldBe false
    }

    "should return true when all conditions are met" in {
      given SessionData = SessionData(
        charitiesReference = testCharitiesReference,
        unsubmittedClaimId = Some("claim-123"),
        repaymentClaimDetailsAnswers = Some(completeRepaymentClaimDetailsAnswers)
      )
      SessionData.shouldUploadOtherIncomeSchedule shouldBe true
    }
  }

  "shouldUploadCommunityBuildingsSchedule" - {
    "should return false when repaymentClaimDetailsAnswers is None" in {
      given SessionData = SessionData(
        charitiesReference = testCharitiesReference,
        unsubmittedClaimId = Some("claim-123")
      )
      SessionData.shouldUploadCommunityBuildingsSchedule shouldBe false
    }

    "should return false when answers are incomplete" in {
      given SessionData = SessionData(
        charitiesReference = testCharitiesReference,
        unsubmittedClaimId = Some("claim-123"),
        repaymentClaimDetailsAnswers = Some(
          incompleteRepaymentClaimDetailsAnswers.copy(
            claimingUnderGiftAidSmallDonationsScheme = Some(true),
            claimingDonationsCollectedInCommunityBuildings = Some(true),
            connectedToAnyOtherCharities = Some(false),
            makingAdjustmentToPreviousClaim = Some(false),
            claimingDonationsNotFromCommunityBuilding = Some(false)
          )
        )
      )
      SessionData.shouldUploadCommunityBuildingsSchedule shouldBe false
    }

    "should return false when unsubmittedClaimId is None" in {
      given SessionData = SessionData(
        charitiesReference = testCharitiesReference,
        repaymentClaimDetailsAnswers = Some(
          completeRepaymentClaimDetailsAnswers.copy(
            claimingUnderGiftAidSmallDonationsScheme = Some(true),
            claimingDonationsCollectedInCommunityBuildings = Some(true),
            connectedToAnyOtherCharities = Some(false),
            makingAdjustmentToPreviousClaim = Some(false),
            claimingDonationsNotFromCommunityBuilding = Some(false)
          )
        )
      )
      SessionData.shouldUploadCommunityBuildingsSchedule shouldBe false
    }

    "should return true when all conditions are met" in {
      given SessionData = SessionData(
        charitiesReference = testCharitiesReference,
        unsubmittedClaimId = Some("claim-123"),
        repaymentClaimDetailsAnswers = Some(
          completeRepaymentClaimDetailsAnswers.copy(
            claimingUnderGiftAidSmallDonationsScheme = Some(true),
            claimingDonationsCollectedInCommunityBuildings = Some(true),
            connectedToAnyOtherCharities = Some(false),
            makingAdjustmentToPreviousClaim = Some(false),
            claimingDonationsNotFromCommunityBuilding = Some(false)
          )
        )
      )
      SessionData.shouldUploadCommunityBuildingsSchedule shouldBe true
    }
  }

  "shouldUploadConnectedCharitiesSchedule" - {
    "should return false when repaymentClaimDetailsAnswers is None" in {
      given SessionData = SessionData(
        charitiesReference = testCharitiesReference,
        unsubmittedClaimId = Some("claim-123")
      )
      SessionData.shouldUploadConnectedCharitiesSchedule shouldBe false
    }

    "should return false when answers are incomplete" in {
      given SessionData = SessionData(
        charitiesReference = testCharitiesReference,
        unsubmittedClaimId = Some("claim-123"),
        repaymentClaimDetailsAnswers = Some(
          incompleteRepaymentClaimDetailsAnswers.copy(
            claimingUnderGiftAidSmallDonationsScheme = Some(true),
            connectedToAnyOtherCharities = Some(true),
            claimingDonationsNotFromCommunityBuilding = Some(true),
            makingAdjustmentToPreviousClaim = Some(false)
          )
        )
      )
      SessionData.shouldUploadConnectedCharitiesSchedule shouldBe false
    }

    "should return false when unsubmittedClaimId is None" in {
      given SessionData = SessionData(
        charitiesReference = testCharitiesReference,
        repaymentClaimDetailsAnswers = Some(
          completeRepaymentClaimDetailsAnswers.copy(
            claimingUnderGiftAidSmallDonationsScheme = Some(true),
            connectedToAnyOtherCharities = Some(true),
            claimingDonationsNotFromCommunityBuilding = Some(true),
            makingAdjustmentToPreviousClaim = Some(false)
          )
        )
      )
      SessionData.shouldUploadConnectedCharitiesSchedule shouldBe false
    }

    "should return true when all conditions are met" in {
      given SessionData = SessionData(
        charitiesReference = testCharitiesReference,
        unsubmittedClaimId = Some("claim-123"),
        repaymentClaimDetailsAnswers = Some(
          completeRepaymentClaimDetailsAnswers.copy(
            claimingUnderGiftAidSmallDonationsScheme = Some(true),
            connectedToAnyOtherCharities = Some(true),
            claimingDonationsCollectedInCommunityBuildings = Some(false),
            claimingDonationsNotFromCommunityBuilding = Some(true),
            makingAdjustmentToPreviousClaim = Some(false)
          )
        )
      )
      SessionData.shouldUploadConnectedCharitiesSchedule shouldBe true
    }
  }

}

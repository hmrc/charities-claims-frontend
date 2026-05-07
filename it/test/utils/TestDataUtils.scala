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

package utils

import models.*
import play.api.libs.json.{JsValue, Json}
import util.TestUsers

trait TestDataUtils {

  val claimId                                 = "123"
  val updateClaimResponse                     = UpdateClaimResponse(true, "1234567890")
  val otherIncomefileRef: FileUploadReference = FileUploadReference("test-other-income-file-upload-ref")
  val giftAidFileRef: FileUploadReference = FileUploadReference("test-gift-aid-file-upload-ref")
  val connectedCharitiesFileRef: FileUploadReference = FileUploadReference("test-connected-charities-file-upload-ref")
  val communityBuildingsFileRef: FileUploadReference = FileUploadReference("test-community-buildings-file-upload-ref")

  lazy val claim        = Claim(
    claimId = claimId,
    userId = TestUsers.organisation1,
    claimSubmitted = false,
    lastUpdatedReference = "1234567890",
    creationTimestamp = "2025-11-10T13:45:56.016Z",
    claimData = ClaimData(
      repaymentClaimDetails = RepaymentClaimDetails(
        claimingTaxDeducted = false,
        claimingGiftAid = true,
        claimingUnderGiftAidSmallDonationsScheme = false,
        claimReferenceNumber = Some("claim-ref-123")
      ),
      organisationDetails = Some(
        OrganisationDetails(
          nameOfCharityRegulator = NameOfCharityRegulator.EnglandAndWales,
          reasonNotRegisteredWithRegulator = None,
          charityRegistrationNumber = Some("charity-reg"),
          areYouACorporateTrustee = true,
          doYouHaveCorporateTrusteeUKAddress = Some(true),
          doYouHaveAuthorisedOfficialTrusteeUKAddress = None,
          nameOfCorporateTrustee = Some("test trustee"),
          corporateTrusteePostcode = Some("NE1 075"),
          corporateTrusteeDaytimeTelephoneNumber = Some("0122314321")
        )
      ),
      agentUserOrganisationDetails = Some(AgentUserOrganisationDetails(
        nameOfCharityRegulator = NameOfCharityRegulator.EnglandAndWales,
        unregulatedReason = Some(ReasonNotRegisteredWithRegulator.LowIncome),
        charityRegistrationNumber = Some("123456"),
        whoShouldHmrcSendPaymentTo = WhoShouldHmrcSendPaymentTo.AgentOrNominee,
        daytimeTelephoneNumber = "07123456789",
        doYouHaveAgentUKAddress = true,
        postcode = Some("AA1 1AA"))),
      giftAidScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference"))
    )
  )
  val getClaimsResponse =
    GetClaimsResponse(
      claimsCount = 1,
      claimsList = List(
        ClaimInfo(claimId, Some("1234567890"), Some("Test charity"))
      )
    )

  val testUploadSummaryResponse = GetUploadSummaryResponse(
    uploads = Seq(
      UploadSummary(
        reference = FileUploadReference("f5da5578-8393-4cd1-be0e-d8ef1b78d8e7"),
        validationType = ValidationType.GiftAid,
        fileStatus = FileStatus.VALIDATED,
        uploadUrl = None
      )
    )
  )

  val testUploadSummaryOtherIncomeValidatedResponse = GetUploadSummaryResponse(
    uploads = Seq(
      UploadSummary(
        reference = otherIncomefileRef,
        validationType = ValidationType.OtherIncome,
        fileStatus = FileStatus.VALIDATED,
        uploadUrl = None
      )
    )
  )

  val testUploadSummaryCommunityBuildingsResponse = GetUploadSummaryResponse(
    uploads = Seq(
      UploadSummary(
        reference = FileUploadReference("f5da5578-8393-4cd1-be0e-d8ef1b78d8e7"),
        validationType = ValidationType.CommunityBuildings,
        fileStatus = FileStatus.VALIDATED,
        uploadUrl = None
      )
    )
  )

  val testUploadSummaryGiftAidValidatedResponse = GetUploadSummaryResponse(
    uploads = Seq(
      UploadSummary(
        reference = giftAidFileRef,
        validationType = ValidationType.GiftAid,
        fileStatus = FileStatus.VALIDATED,
        uploadUrl = None
      )
    )
  )

  val testUploadSummaryCommunityBuildingsValidatedResponse = GetUploadSummaryResponse(
    uploads = Seq(
      UploadSummary(
        reference = communityBuildingsFileRef,
        validationType = ValidationType.CommunityBuildings,
        fileStatus = FileStatus.VALIDATED,
        uploadUrl = None
      )
    )
  )

  val testUploadSummaryConnectedCharitiesValidatedResponse = GetUploadSummaryResponse(
    uploads = Seq(
      UploadSummary(
        reference = connectedCharitiesFileRef,
        validationType = ValidationType.ConnectedCharities,
        fileStatus = FileStatus.VALIDATED,
        uploadUrl = None
      )
    )
  )

  val getUploadResultGiftAidValidatedJson: JsValue = Json.parse(
    """
      {
        "reference": "f5da5578-8393-4cd1-be0e-d8ef1b78d8e7",
        "validationType": "GiftAid",
        "fileStatus": "VALIDATED",
        "giftAidScheduleData": {
          "earliestDonationDate": "2025-01-31",
          "prevOverclaimedGiftAid": 0.00,
          "totalDonations": 1450,
          "donations": [
            {
              "donationItem": 1,
              "donorTitle": "Mr",
              "donorFirstName": "Henry",
              "donorLastName": "House Martin",
              "donorHouse": "152A",
              "donorPostcode": "M99 2QD",
              "sponsoredEvent": false,
              "donationDate": "2025-03-24",
              "donationAmount": 240
            }
          ]
        }
      }
    """
  )

  val getUploadResultCommunityBuildingsValidatedJson: JsValue = Json.parse(
    """
        {
          "reference": "f5da5578-8393-4cd1-be0e-d8ef1b78d8e7",
          "validationType": "CommunityBuildings",
          "fileStatus": "VALIDATED",
          "communityBuildingsData": {
            "totalOfAllAmounts": "17520.00",
            "communityBuildings": [
              {
                "communityBuildingItem": 1,
                "buildingName": "The Vault",
                "firstLineOfAddress": "22 Liberty Place",
                "postcode": "L20 3UD",
                "taxYear1": 2023,
                "amountYear1": 1500,
                "taxYear2": 2024,
                "amountYear2": 2500
              }
            ]
          }
        }
      """
  )

  val getUploadResultConnectedCharitiesValidatedJson: JsValue = Json.parse(
    """
        {
          "reference": "f5da5578-8393-4cd1-be0e-d8ef1b78d8e7",
          "validationType": "ConnectedCharities",
          "fileStatus": "VALIDATED",
          "connectedCharitiesData": {
            "charities": [
              {
                "charityItem": 1,
                "charityName": "Charity of the 501st Legion",
                "charityReference": "CW501"
              }
            ]
          }
        }
      """
  )

  val submissionSummaryResponse = SubmissionSummaryResponse(
    claimDetails = ClaimDetails("test charity", "test ref", "2026-04-07T11:34:21.147Z", "Mr John"),
    giftAidDetails = None,
    otherIncomeDetails = None,
    gasdsDetails = None,
    adjustmentDetails = None,
    submissionReferenceNumber = "sub ref"
  )
}

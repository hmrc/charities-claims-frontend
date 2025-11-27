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

import play.api.libs.json.Format
import play.api.libs.json.Json

final case class Claim(
  claimId: String,
  userId: String,
  claimSubmitted: Boolean,
  creationTimestamp: String,
  claimData: ClaimData,
  submissionDetails: Option[SubmissionDetails] = None
)

object Claim {
  given Format[Claim] = Json.format[Claim]
}

final case class ClaimData(
  repaymentClaimDetails: RepaymentClaimDetails,
  organisationDetails: Option[OrganisationDetails] = None,
  giftAidScheduleData: Option[GiftAidScheduleData] = None,
  declarationDetails: Option[DeclarationDetails] = None,
  otherIncomeScheduleData: Option[OtherIncomeScheduleData] = None,
  gasdsScheduleData: Option[GasdsScheduleData] = None
)

object ClaimData {
  given Format[ClaimData] = Json.format[ClaimData]
}

final case class RepaymentClaimDetails(
  claimingGiftAid: Boolean,
  claimingTaxDeducted: Boolean,
  claimingUnderGasds: Boolean,
  claimReferenceNumber: Option[String] = None,
  claimingDonationsNotFromCommunityBuilding: Option[Boolean] = None,
  claimingDonationsCollectedInCommunityBuildings: Option[Boolean] = None,
  connectedToAnyOtherCharities: Option[Boolean] = None,
  makingAdjustmentToPreviousClaim: Option[Boolean] = None
)

object RepaymentClaimDetails {
  given Format[RepaymentClaimDetails] = Json.format[RepaymentClaimDetails]
}

final case class OrganisationDetails(
  nameOfCharityRegulator: String,
  charityRegistrationNumber: String,
  areYouACorporateTrustee: Boolean,
  nameOfCorporateTrustee: String,
  corporateTrusteePostcode: String,
  corporateTrusteeDaytimeTelephoneNumber: String
)

object OrganisationDetails {
  given Format[OrganisationDetails] = Json.format[OrganisationDetails]
}

final case class GiftAidScheduleData(
  earliestDonationDate: String,
  prevOverclaimedGiftAid: BigDecimal,
  totalDonations: BigDecimal,
  donations: Seq[Donation]
)

object GiftAidScheduleData {
  given Format[GiftAidScheduleData] = Json.format[GiftAidScheduleData]
}

final case class DeclarationDetails(
  understandFalseStatements: Boolean,
  includedAnyAdjustmentsInClaimPrompt: String
)

object DeclarationDetails {
  given Format[DeclarationDetails] = Json.format[DeclarationDetails]
}

final case class SubmissionDetails(
  submissionTimestamp: String,
  submissionReference: String
)

object SubmissionDetails {
  given Format[SubmissionDetails] = Json.format[SubmissionDetails]
}

final case class Donation(
  donationItem: Int,
  donationDate: String,
  donationAmount: BigDecimal,
  donorTitle: Option[String] = None,
  donorFirstName: Option[String] = None,
  donorLastName: Option[String] = None,
  donorHouse: Option[String] = None,
  donorPostcode: Option[String] = None,
  sponsoredEvent: Option[Boolean] = None,
  aggregatedDonations: Option[String] = None
)

object Donation {
  given Format[Donation] = Json.format[Donation]
}

final case class OtherIncomeScheduleData(
  prevOverclaimedOtherIncome: BigDecimal,
  totalGrossPayments: BigDecimal,
  totalTaxDeducted: BigDecimal,
  payments: Seq[Payment]
)

object OtherIncomeScheduleData {
  given Format[OtherIncomeScheduleData] = Json.format[OtherIncomeScheduleData]
}

final case class Payment(
  paymentItem: Int,
  nameOfPayer: String,
  dateOfPayment: String,
  grossPayment: BigDecimal,
  taxDeducted: BigDecimal
)

object Payment {
  given Format[Payment] = Json.format[Payment]
}

final case class GasdsScheduleData(
  adjustmentForGiftAidOverClaimed: BigDecimal,
  claims: Seq[GasdsClaim],
  connectedCharitiesScheduleData: Seq[ConnectedCharity],
  communityBuildingsScheduleData: Seq[CommunityBuilding]
)

object GasdsScheduleData {
  given Format[GasdsScheduleData] = Json.format[GasdsScheduleData]
}

final case class GasdsClaim(
  taxYear: Int,
  amountOfDonationsReceived: BigDecimal
)

object GasdsClaim {
  given Format[GasdsClaim] = Json.format[GasdsClaim]
}

final case class ConnectedCharity(
  charityItem: Int,
  charityName: String,
  charityReference: String
)

object ConnectedCharity {
  given Format[ConnectedCharity] = Json.format[ConnectedCharity]
}

final case class CommunityBuilding(
  buildingItem: Int,
  buildingName: String,
  firstLineOfAddress: String,
  postcode: String,
  taxYearOneEnd: Int,
  taxYearOneAmount: BigDecimal,
  taxYearTwoEnd: Int,
  taxYearTwoAmount: BigDecimal,
  taxYearThreeEnd: Int,
  taxYearThreeAmount: BigDecimal
)

object CommunityBuilding {
  given Format[CommunityBuilding] = Json.format[CommunityBuilding]
}

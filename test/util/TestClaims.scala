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

package util

import models.{
  Claim,
  ClaimData,
  GetClaimsResponse,
  NameOfCharityRegulator,
  OrganisationDetails,
  ReasonNotRegisteredWithRegulator,
  RepaymentClaimDetails
}
import play.api.libs.json.Json
import models.ClaimInfo
import play.api.libs.json.JsObject
import play.api.libs.json.JsArray

object TestClaims {

  lazy val testGetClaimsResponseUnsubmittedJsonString: String =
    TestResources.readTestResource("/test-get-claims-response-unsubmitted.json")

  lazy val testGetClaimsResponseUnsubmitted: GetClaimsResponse =
    Json.parse(testGetClaimsResponseUnsubmittedJsonString).as[GetClaimsResponse]

  lazy val testClaimUnsubmitted: Claim =
    Json
      .parse(testGetClaimsResponseUnsubmittedJsonString)
      .as[JsObject]
      .value("claimsList")
      .as[JsArray]
      .head
      .as[Claim]

  lazy val testGetClaimsResponseSubmittedJsonString: String =
    TestResources.readTestResource("/test-get-claims-response-submitted.json")

  lazy val testGetClaimsResponseSubmitted: GetClaimsResponse =
    Json.parse(testGetClaimsResponseSubmittedJsonString).as[GetClaimsResponse]

  lazy val testClaimSubmitted: Claim =
    Json
      .parse(testGetClaimsResponseSubmittedJsonString)
      .as[JsObject]
      .value("claimsList")
      .as[JsArray]
      .head
      .as[Claim]

  def testClaimWithRepaymentClaimDetailsOnly(
    claimId: String = "123",
    claimingTaxDeducted: Boolean = true,
    claimingGiftAid: Boolean = true,
    claimingUnderGiftAidSmallDonationsScheme: Boolean = false,
    claimReferenceNumber: Option[String] = Some("1234567890")
  ): Claim =
    Claim(
      claimId = claimId,
      userId = TestUsers.organisation1,
      claimSubmitted = false,
      lastUpdatedReference = "1234567890",
      creationTimestamp = "2025-11-10T13:45:56.016Z",
      claimData = ClaimData(
        repaymentClaimDetails = RepaymentClaimDetails(
          claimingTaxDeducted = claimingTaxDeducted,
          claimingGiftAid = claimingGiftAid,
          claimingUnderGiftAidSmallDonationsScheme = claimingUnderGiftAidSmallDonationsScheme,
          claimReferenceNumber = claimReferenceNumber
        )
      )
    )

  def testClaimWithOrganisationDetailsOnly(
    claimId: String = "123",
    claimingTaxDeducted: Boolean = true,
    claimingGiftAid: Boolean = true,
    claimingUnderGiftAidSmallDonationsScheme: Boolean = false,
    claimReferenceNumber: Option[String] = Some("1234567890"),
    nameOfCharityRegulator: NameOfCharityRegulator = NameOfCharityRegulator.EnglandAndWales,
    reasonNotRegisteredWithRegulator: Option[ReasonNotRegisteredWithRegulator] = Some(
      ReasonNotRegisteredWithRegulator.LowIncome
    ),
    charityRegistrationNumber: String = "1234567890",
    areYouACorporateTrustee: Boolean = true,
    doYouHaveCorporateTrusteeUKAddress: Boolean = true,
    doYouHaveAuthorisedOfficialTrusteeUKAddress: Boolean = false,
    nameOfCorporateTrustee: String = "John Doe",
    corporateTrusteePostcode: String = "AA1 2BB",
    corporateTrusteeDaytimeTelephoneNumber: String = "07912345678"
  ): Claim =
    Claim(
      claimId = claimId,
      userId = TestUsers.organisation1,
      claimSubmitted = false,
      lastUpdatedReference = "1234567890",
      creationTimestamp = "2025-11-10T13:45:56.016Z",
      claimData = ClaimData(
        repaymentClaimDetails = RepaymentClaimDetails(
          claimingTaxDeducted = claimingTaxDeducted,
          claimingGiftAid = claimingGiftAid,
          claimingUnderGiftAidSmallDonationsScheme = claimingUnderGiftAidSmallDonationsScheme,
          claimReferenceNumber = claimReferenceNumber
        ),
        organisationDetails = Some(
          OrganisationDetails(
            nameOfCharityRegulator = nameOfCharityRegulator,
            reasonNotRegisteredWithRegulator = reasonNotRegisteredWithRegulator,
            charityRegistrationNumber = Some(charityRegistrationNumber),
            areYouACorporateTrustee = areYouACorporateTrustee,
            doYouHaveCorporateTrusteeUKAddress = Some(doYouHaveCorporateTrusteeUKAddress),
            doYouHaveAuthorisedOfficialTrusteeUKAddress = Some(doYouHaveAuthorisedOfficialTrusteeUKAddress),
            nameOfCorporateTrustee = Some(nameOfCorporateTrustee),
            corporateTrusteePostcode = Some(corporateTrusteePostcode),
            corporateTrusteeDaytimeTelephoneNumber = Some(corporateTrusteeDaytimeTelephoneNumber)
          )
        )
      )
    )

  def testClaimInfo(
    claimId: String = "123",
    hmrcCharitiesReference: Option[String] = None,
    nameOfCharity: Option[String] = None
  ) = ClaimInfo(
    claimId = claimId,
    hmrcCharitiesReference = hmrcCharitiesReference,
    nameOfCharity = nameOfCharity
  )
}

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

import play.api.libs.json.{Format, Json}
import scala.util.Try
import utils.Required.*

final case class SessionData(
  // claimId of the unsubmitted claim stored in the backend,
  // if empty, the user has started a new claim
  unsubmittedClaimId: Option[String] = None,
  // HMRC Charities reference from enrolment (CHARID for Organisation, AGENTCHARID for Agent)
  charitiesReference: String,
  // lastUpdatedReference of the claim stored in the backend,
  // if empty, the user has started a new claim
  lastUpdatedReference: Option[String] = None,
  repaymentClaimDetailsAnswers: Option[RepaymentClaimDetailsAnswers] = None,
  organisationDetailsAnswers: Option[OrganisationDetailsAnswers] = None,
  declarationDetailsAnswers: Option[DeclarationDetailsAnswers] = None,
  giftAidSmallDonationsSchemeDonationDetailsAnswers: Option[GiftAidSmallDonationsSchemeDonationDetailsAnswers] = None,
  // File upload references and data retrieved from the validation service
  giftAidScheduleUpscanInitialization: Option[UpscanInitiateResponse] = None,
  giftAidScheduleFileUploadReference: Option[FileUploadReference] = None,
  giftAidScheduleData: Option[GiftAidScheduleData] = None,
  giftAidScheduleCompleted: Boolean = false,
  otherIncomeScheduleUpscanInitialization: Option[UpscanInitiateResponse] = None,
  otherIncomeScheduleFileUploadReference: Option[FileUploadReference] = None,
  otherIncomeScheduleData: Option[OtherIncomeScheduleData] = None,
  otherIncomeScheduleCompleted: Boolean = false,
  communityBuildingsScheduleUpscanInitialization: Option[UpscanInitiateResponse] = None,
  communityBuildingsScheduleFileUploadReference: Option[FileUploadReference] = None,
  communityBuildingsScheduleData: Option[CommunityBuildingsScheduleData] = None,
  communityBuildingsScheduleCompleted: Boolean = false,
  connectedCharitiesScheduleUpscanInitialization: Option[UpscanInitiateResponse] = None,
  connectedCharitiesScheduleFileUploadReference: Option[FileUploadReference] = None,
  connectedCharitiesScheduleData: Option[ConnectedCharitiesScheduleData] = None,
  connectedCharitiesScheduleCompleted: Boolean = false
)

object SessionData {

  given Format[SessionData] = Json.format[SessionData]

  def unsubmittedClaimId(using sd: SessionData): Option[String] = sd.unsubmittedClaimId

  extension (sd: SessionData) def and(f: SessionData ?=> SessionData): SessionData = f(using sd)

  def empty(charitiesRef: String): SessionData = SessionData(
    charitiesReference = charitiesRef
  )

  def from(claim: Claim, charitiesRef: String): SessionData =
    SessionData(
      unsubmittedClaimId = Some(claim.claimId),
      charitiesReference = charitiesRef,
      lastUpdatedReference = Some(claim.lastUpdatedReference),
      repaymentClaimDetailsAnswers = Some(RepaymentClaimDetailsAnswers.from(claim.claimData.repaymentClaimDetails)),
      organisationDetailsAnswers = claim.claimData.organisationDetails.map(OrganisationDetailsAnswers.from),
      declarationDetailsAnswers = claim.claimData.declarationDetails.map(DeclarationDetailsAnswers.from),
      giftAidSmallDonationsSchemeDonationDetailsAnswers =
        claim.claimData.giftAidSmallDonationsSchemeDonationDetails.map(
          GiftAidSmallDonationsSchemeDonationDetailsAnswers.from
        ),
      giftAidScheduleFileUploadReference = claim.claimData.giftAidScheduleFileUploadReference,
      giftAidScheduleCompleted = claim.claimData.giftAidScheduleFileUploadReference.isDefined,
      otherIncomeScheduleFileUploadReference = claim.claimData.otherIncomeScheduleFileUploadReference,
      otherIncomeScheduleCompleted = claim.claimData.otherIncomeScheduleFileUploadReference.isDefined,
      communityBuildingsScheduleFileUploadReference = claim.claimData.communityBuildingsScheduleFileUploadReference,
      communityBuildingsScheduleCompleted = claim.claimData.communityBuildingsScheduleFileUploadReference.isDefined,
      connectedCharitiesScheduleFileUploadReference = claim.claimData.connectedCharitiesScheduleFileUploadReference,
      connectedCharitiesScheduleCompleted = claim.claimData.connectedCharitiesScheduleFileUploadReference.isDefined
    )

  def toUpdateClaimRequest(sessionData: SessionData): Try[UpdateClaimRequest] =
    for {
      lastUpdatedReference                       <- required(sessionData)(_.lastUpdatedReference)
      repaymentClaimDetails                      <- RepaymentClaimDetailsAnswers
                                                      .toRepaymentClaimDetails(sessionData.repaymentClaimDetailsAnswers.get)
      organisationDetails                        <- sessionData.organisationDetailsAnswers
                                                      .flatMapTry(OrganisationDetailsAnswers.toOrganisationDetails)
      giftAidSmallDonationsSchemeDonationDetails <-
        sessionData.giftAidSmallDonationsSchemeDonationDetailsAnswers
          .flatMapTry(
            GiftAidSmallDonationsSchemeDonationDetailsAnswers.toGiftAidSmallDonationsSchemeDonationDetails
          )
      declarationDetails                         <- sessionData.declarationDetailsAnswers
                                                      .flatMapTry(DeclarationDetailsAnswers.toDeclarationDetails)
    } yield UpdateClaimRequest(
      lastUpdatedReference = lastUpdatedReference,
      repaymentClaimDetails = repaymentClaimDetails,
      organisationDetails = organisationDetails,
      giftAidSmallDonationsSchemeDonationDetails = giftAidSmallDonationsSchemeDonationDetails,
      declarationDetails = declarationDetails,
      giftAidScheduleFileUploadReference =
        // only include the file upload reference if the schedule has been accepted
        if sessionData.giftAidScheduleCompleted
        then sessionData.giftAidScheduleFileUploadReference
        else None,
      otherIncomeScheduleFileUploadReference =
        // only include the file upload reference if the schedule has been accepted
        if sessionData.otherIncomeScheduleCompleted
        then sessionData.otherIncomeScheduleFileUploadReference
        else None,
      communityBuildingsScheduleFileUploadReference =
        // only include the file upload reference if the schedule has been accepted
        if sessionData.communityBuildingsScheduleCompleted
        then sessionData.communityBuildingsScheduleFileUploadReference
        else None,
      connectedCharitiesScheduleFileUploadReference =
        // only include the file upload reference if the schedule has been accepted
        if sessionData.connectedCharitiesScheduleCompleted
        then sessionData.connectedCharitiesScheduleFileUploadReference
        else None
    )

  def shouldUploadGiftAidSchedule(using session: SessionData): Boolean =
    RepaymentClaimDetailsAnswers.getClaimingGiftAid.contains(true)

  def shouldUploadOtherIncomeSchedule(using session: SessionData): Boolean =
    RepaymentClaimDetailsAnswers.getClaimingTaxDeducted.contains(true)

  def shouldUploadCommunityBuildingsSchedule(using session: SessionData): Boolean =
    RepaymentClaimDetailsAnswers.getClaimingDonationsCollectedInCommunityBuildings.contains(true)

  def shouldUploadConnectedCharitiesSchedule(using session: SessionData): Boolean =
    RepaymentClaimDetailsAnswers.getConnectedToAnyOtherCharities.contains(true)
}

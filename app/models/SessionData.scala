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
import utils.Required.*

import scala.util.Try

final case class SessionData(
  // claimId of the unsubmitted claim stored in the backend,
  // if empty, the user has started a new claim
  unsubmittedClaimId: Option[String] = None,
  claimSubmitted: Option[Boolean] = None,
  // HMRC Charities reference from enrolment (CHARID for Organisation, AGENTCHARID for Agent)
  charitiesReference: String,
  // lastUpdatedReference of the claim stored in the backend,
  // if empty, the user has started a new claim
  lastUpdatedReference: Option[String] = None,
  repaymentClaimDetailsAnswers: Option[RepaymentClaimDetailsAnswers] = None,
  organisationDetailsAnswers: Option[OrganisationDetailsAnswers] = None,
  understandFalseStatements: Option[Boolean] = None,
  includedAnyAdjustmentsInClaimPrompt: Option[String] = None,
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
  connectedCharitiesScheduleCompleted: Boolean = false,
  unregulatedLimitExceeded: Boolean = false,
  unregulatedWarningBypassed: Boolean = false,
  adjustmentForOtherIncomePreviousOverClaimed: Option[BigDecimal] = None,
  prevOverclaimedGiftAid: Option[BigDecimal] = None,
  submissionReference: Option[String] = None
)

object SessionData {

  given Format[SessionData] = Json.format[SessionData]

  def unsubmittedClaimId(using sd: SessionData): Option[String] = sd.unsubmittedClaimId

  extension (sd: SessionData) def and(f: SessionData ?=> SessionData): SessionData = f(using sd)

  def empty(charitiesRef: String): SessionData = SessionData(
    charitiesReference = charitiesRef
  )

  def from(
    claim: Claim,
    charitiesRef: String,
    uploadsSummaryOpt: Option[GetUploadSummaryResponse] = None
  ): SessionData =
    SessionData(
      unsubmittedClaimId = Some(claim.claimId),
      charitiesReference = charitiesRef,
      lastUpdatedReference = Some(claim.lastUpdatedReference),
      repaymentClaimDetailsAnswers = Some(RepaymentClaimDetailsAnswers.from(claim.claimData.repaymentClaimDetails)),
      organisationDetailsAnswers = claim.claimData.organisationDetails.map(OrganisationDetailsAnswers.from),
      includedAnyAdjustmentsInClaimPrompt = claim.claimData.includedAnyAdjustmentsInClaimPrompt,
      understandFalseStatements = claim.claimData.understandFalseStatements,
      adjustmentForOtherIncomePreviousOverClaimed = claim.claimData.adjustmentForOtherIncomePreviousOverClaimed,
      prevOverclaimedGiftAid = claim.claimData.prevOverclaimedGiftAid,
      giftAidSmallDonationsSchemeDonationDetailsAnswers =
        claim.claimData.giftAidSmallDonationsSchemeDonationDetails.map(
          GiftAidSmallDonationsSchemeDonationDetailsAnswers.from
        ),
      giftAidScheduleFileUploadReference = claim.claimData.giftAidScheduleFileUploadReference
        .orElse(uploadsSummaryOpt.flatMap(_.findUpload(ValidationType.GiftAid)).map(_.reference)),
      giftAidScheduleUpscanInitialization = uploadsSummaryOpt
        .flatMap(_.findUpload(ValidationType.GiftAid))
        .flatMap(_.asUpscanInitiateResponse),
      giftAidScheduleCompleted = claim.claimData.giftAidScheduleFileUploadReference.isDefined,
      otherIncomeScheduleFileUploadReference = claim.claimData.otherIncomeScheduleFileUploadReference
        .orElse(uploadsSummaryOpt.flatMap(_.findUpload(ValidationType.OtherIncome)).map(_.reference)),
      otherIncomeScheduleUpscanInitialization = uploadsSummaryOpt
        .flatMap(_.findUpload(ValidationType.OtherIncome))
        .flatMap(_.asUpscanInitiateResponse),
      otherIncomeScheduleCompleted = claim.claimData.otherIncomeScheduleFileUploadReference.isDefined,
      communityBuildingsScheduleFileUploadReference = claim.claimData.communityBuildingsScheduleFileUploadReference
        .orElse(uploadsSummaryOpt.flatMap(_.findUpload(ValidationType.CommunityBuildings)).map(_.reference)),
      communityBuildingsScheduleCompleted = claim.claimData.communityBuildingsScheduleFileUploadReference.isDefined,
      communityBuildingsScheduleUpscanInitialization = uploadsSummaryOpt
        .flatMap(_.findUpload(ValidationType.CommunityBuildings))
        .flatMap(_.asUpscanInitiateResponse),
      connectedCharitiesScheduleFileUploadReference = claim.claimData.connectedCharitiesScheduleFileUploadReference
        .orElse(uploadsSummaryOpt.flatMap(_.findUpload(ValidationType.ConnectedCharities)).map(_.reference)),
      connectedCharitiesScheduleUpscanInitialization = uploadsSummaryOpt
        .flatMap(_.findUpload(ValidationType.ConnectedCharities))
        .flatMap(_.asUpscanInitiateResponse),
      connectedCharitiesScheduleCompleted = claim.claimData.connectedCharitiesScheduleFileUploadReference.isDefined,
      submissionReference = claim.submissionDetails.map(_.submissionReference)
    )

  def toUpdateClaimRequest(sessionData: SessionData): Try[UpdateClaimRequest] =
    for {
      lastUpdatedReference                       <- required(sessionData)(_.lastUpdatedReference)
      adjustmentForOtherIncomePreviousOverClaimed = sessionData.adjustmentForOtherIncomePreviousOverClaimed
      includedAnyAdjustmentsInClaimPrompt         = sessionData.includedAnyAdjustmentsInClaimPrompt
      understandFalseStatements                   = sessionData.understandFalseStatements
      prevOverclaimedGiftAid                      = sessionData.prevOverclaimedGiftAid
      repaymentClaimDetails                      <- RepaymentClaimDetailsAnswers
                                                      .toRepaymentClaimDetails(sessionData.repaymentClaimDetailsAnswers.get)
      organisationDetails                        <-
        sessionData.organisationDetailsAnswers
          .flatMapTry(OrganisationDetailsAnswers.toOrganisationDetails(_, isCASCCharityReference(using sessionData)))
      giftAidSmallDonationsSchemeDonationDetails <-
        sessionData.giftAidSmallDonationsSchemeDonationDetailsAnswers
          .flatMapTry(
            GiftAidSmallDonationsSchemeDonationDetailsAnswers.toGiftAidSmallDonationsSchemeDonationDetails
          )
    } yield UpdateClaimRequest(
      lastUpdatedReference = lastUpdatedReference,
      repaymentClaimDetails = repaymentClaimDetails,
      organisationDetails = organisationDetails,
      giftAidSmallDonationsSchemeDonationDetails = giftAidSmallDonationsSchemeDonationDetails,
      includedAnyAdjustmentsInClaimPrompt = includedAnyAdjustmentsInClaimPrompt,
      understandFalseStatements = understandFalseStatements,
      adjustmentForOtherIncomePreviousOverClaimed = adjustmentForOtherIncomePreviousOverClaimed,
      prevOverclaimedGiftAid = prevOverclaimedGiftAid,
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

  def setUnsubmittedClaimId(unsubmittedClaimId: String)(using session: SessionData): SessionData =
    session.copy(unsubmittedClaimId = Some(unsubmittedClaimId))

  def setCommunityBuildingsScheduleCompleted(value: Boolean)(using session: SessionData): SessionData =
    session.copy(communityBuildingsScheduleCompleted = value)

  def isRepaymentClaimDetailsComplete(using session: SessionData): Boolean =
    session.unsubmittedClaimId.isDefined
      && session.repaymentClaimDetailsAnswers.exists(_.hasRepaymentClaimDetailsCompleteAnswers)

  def isClaimNotSubmitted(using session: SessionData): Boolean =
    session.submissionReference.isEmpty

  def isCASCCharityReference(using session: SessionData): Boolean =
    session.charitiesReference.startsWith("CH") || session.charitiesReference.startsWith("CF")

  def isClaimDetailsComplete(using session: SessionData): Boolean =
    session.unsubmittedClaimId.isDefined
      && session.repaymentClaimDetailsAnswers.exists(_.hasRepaymentClaimDetailsCompleteAnswers)
      && session.organisationDetailsAnswers.exists(
        _.hasOrganisationDetailsCompleteAnswers(isCASCCharityReference(using session))
      )
      && (!shouldUploadConnectedCharitiesSchedule || session.connectedCharitiesScheduleCompleted)
      && (!shouldUploadOtherIncomeSchedule || session.otherIncomeScheduleCompleted)
      && (!shouldUploadCommunityBuildingsSchedule || session.communityBuildingsScheduleCompleted)
      && (!shouldUploadGiftAidSchedule || session.giftAidScheduleCompleted)

  def isClaimSubmitted(using session: SessionData): Boolean =
    session.submissionReference.isDefined

  def shouldUploadGiftAidSchedule(using session: SessionData): Boolean =
    RepaymentClaimDetailsAnswers.getClaimingGiftAid.contains(true)
      && isRepaymentClaimDetailsComplete

  def shouldUploadOtherIncomeSchedule(using session: SessionData): Boolean =
    RepaymentClaimDetailsAnswers.getClaimingTaxDeducted.contains(true)
      && isRepaymentClaimDetailsComplete

  def shouldUploadCommunityBuildingsSchedule(using session: SessionData): Boolean =
    RepaymentClaimDetailsAnswers.getClaimingDonationsCollectedInCommunityBuildings.contains(true)
      && RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true)
      && isRepaymentClaimDetailsComplete

  def shouldUploadConnectedCharitiesSchedule(using session: SessionData): Boolean =
    RepaymentClaimDetailsAnswers.getConnectedToAnyOtherCharities.contains(true)
      && isRepaymentClaimDetailsComplete

  def setUnregulatedLimitExceeded(value: Boolean)(using session: SessionData): SessionData =
    session.copy(unregulatedLimitExceeded = value)

  def isUnregulatedLimitExceeded(using session: SessionData): Boolean =
    session.unregulatedLimitExceeded

  def syncUploadReferencesAndFlagsWithCheckboxes(using session: SessionData): SessionData =
    session.copy(
      giftAidScheduleFileUploadReference =
        if RepaymentClaimDetailsAnswers.getClaimingGiftAid.contains(true)
        then session.giftAidScheduleFileUploadReference
        else None,
      prevOverclaimedGiftAid =
        if RepaymentClaimDetailsAnswers.getClaimingGiftAid.contains(true)
        then session.prevOverclaimedGiftAid
        else None,
      otherIncomeScheduleFileUploadReference =
        if RepaymentClaimDetailsAnswers.getClaimingTaxDeducted.contains(true)
        then session.otherIncomeScheduleFileUploadReference
        else None,
      adjustmentForOtherIncomePreviousOverClaimed =
        if RepaymentClaimDetailsAnswers.getClaimingTaxDeducted.contains(true)
        then session.adjustmentForOtherIncomePreviousOverClaimed
        else None,
      communityBuildingsScheduleFileUploadReference =
        if RepaymentClaimDetailsAnswers.getClaimingDonationsCollectedInCommunityBuildings.contains(true)
        then session.communityBuildingsScheduleFileUploadReference
        else None,
      connectedCharitiesScheduleFileUploadReference =
        if RepaymentClaimDetailsAnswers.getConnectedToAnyOtherCharities.contains(true)
        then session.connectedCharitiesScheduleFileUploadReference
        else None,
      giftAidScheduleCompleted =
        if RepaymentClaimDetailsAnswers.getClaimingGiftAid.contains(true)
        then session.giftAidScheduleCompleted
        else false,
      otherIncomeScheduleCompleted =
        if RepaymentClaimDetailsAnswers.getClaimingTaxDeducted.contains(true)
        then session.otherIncomeScheduleCompleted
        else false,
      communityBuildingsScheduleCompleted =
        if RepaymentClaimDetailsAnswers.getClaimingDonationsCollectedInCommunityBuildings.contains(true)
        then session.communityBuildingsScheduleCompleted
        else false,
      connectedCharitiesScheduleCompleted =
        if RepaymentClaimDetailsAnswers.getConnectedToAnyOtherCharities.contains(true)
        then session.connectedCharitiesScheduleCompleted
        else false
    )

  def getAbandonedUploads(using session: SessionData): Seq[FileUploadReference] =
    Seq(
      session.giftAidScheduleFileUploadReference.filterNot(_ =>
        RepaymentClaimDetailsAnswers.getClaimingGiftAid.contains(true)
          || session.giftAidScheduleCompleted
      ),
      session.otherIncomeScheduleFileUploadReference.filterNot(_ =>
        RepaymentClaimDetailsAnswers.getClaimingTaxDeducted.contains(true)
          || session.otherIncomeScheduleCompleted
      ),
      session.communityBuildingsScheduleFileUploadReference.filterNot(_ =>
        RepaymentClaimDetailsAnswers.getClaimingDonationsCollectedInCommunityBuildings.contains(true)
          || session.communityBuildingsScheduleCompleted
      ),
      session.connectedCharitiesScheduleFileUploadReference.filterNot(_ =>
        RepaymentClaimDetailsAnswers.getConnectedToAnyOtherCharities.contains(true)
          || session.connectedCharitiesScheduleCompleted
      )
    ).flatten
}

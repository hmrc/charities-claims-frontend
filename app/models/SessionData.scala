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
  repaymentClaimDetailsAnswersOld: RepaymentClaimDetailsAnswersOld,
  repaymentClaimDetailsAnswers: Option[RepaymentClaimDetailsAnswers] = None,
  organisationDetailsAnswers: Option[OrganisationDetailsAnswers] = None,
  declarationDetailsAnswers: Option[DeclarationDetailsAnswers] = None,
  giftAidSmallDonationsSchemeDonationDetailsAnswers: Option[GiftAidSmallDonationsSchemeDonationDetailsAnswers] = None,
  // File upload references and data retrieved from the validation service
  giftAidScheduleUpscanInitialization: Option[UpscanInitiateResponse] = None,
  giftAidScheduleFileUploadReference: Option[FileUploadReference] = None,
  giftAidScheduleData: Option[GiftAidScheduleData] = None,
  otherIncomeScheduleUpscanInitialization: Option[UpscanInitiateResponse] = None,
  otherIncomeScheduleFileUploadReference: Option[FileUploadReference] = None,
  otherIncomeScheduleData: Option[OtherIncomeScheduleData] = None,
  communityBuildingsScheduleUpscanInitialization: Option[UpscanInitiateResponse] = None,
  communityBuildingsScheduleFileUploadReference: Option[FileUploadReference] = None,
  communityBuildingsScheduleData: Option[CommunityBuildingsScheduleData] = None,
  connectedCharitiesScheduleUpscanInitialization: Option[UpscanInitiateResponse] = None,
  connectedCharitiesScheduleFileUploadReference: Option[FileUploadReference] = None,
  connectedCharitiesScheduleData: Option[ConnectedCharitiesScheduleData] = None
)

object SessionData {

  given Format[SessionData] = Json.format[SessionData]

  def empty(charitiesRef: String): SessionData = SessionData(
    charitiesReference = charitiesRef,
    repaymentClaimDetailsAnswersOld = RepaymentClaimDetailsAnswersOld()
  )

  def from(claim: Claim, charitiesRef: String): SessionData =
    SessionData(
      unsubmittedClaimId = Some(claim.claimId),
      charitiesReference = charitiesRef,
      lastUpdatedReference = Some(claim.lastUpdatedReference),
      repaymentClaimDetailsAnswersOld = RepaymentClaimDetailsAnswersOld.from(claim.claimData.repaymentClaimDetails),
      repaymentClaimDetailsAnswers = Some(RepaymentClaimDetailsAnswers.from(claim.claimData.repaymentClaimDetails)),
      organisationDetailsAnswers = claim.claimData.organisationDetails.map(OrganisationDetailsAnswers.from),
      declarationDetailsAnswers = claim.claimData.declarationDetails.map(DeclarationDetailsAnswers.from),
      giftAidSmallDonationsSchemeDonationDetailsAnswers =
        claim.claimData.giftAidSmallDonationsSchemeDonationDetails.map(
          GiftAidSmallDonationsSchemeDonationDetailsAnswers.from
        ),
      giftAidScheduleFileUploadReference = claim.claimData.giftAidScheduleFileUploadReference,
      otherIncomeScheduleFileUploadReference = claim.claimData.otherIncomeScheduleFileUploadReference,
      communityBuildingsScheduleFileUploadReference = claim.claimData.communityBuildingsScheduleFileUploadReference,
      connectedCharitiesScheduleFileUploadReference = claim.claimData.connectedCharitiesScheduleFileUploadReference
    )

  def toUpdateClaimRequest(sessionData: SessionData): Try[UpdateClaimRequest] =
    for {
      lastUpdatedReference                       <- required(sessionData)(_.lastUpdatedReference)
      repaymentClaimDetails                      <- RepaymentClaimDetailsAnswersOld
                                                      .toRepaymentClaimDetails(sessionData.repaymentClaimDetailsAnswersOld)
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
      giftAidScheduleFileUploadReference = sessionData.giftAidScheduleFileUploadReference,
      otherIncomeScheduleFileUploadReference = sessionData.otherIncomeScheduleFileUploadReference,
      communityBuildingsScheduleFileUploadReference = sessionData.communityBuildingsScheduleFileUploadReference,
      connectedCharitiesScheduleFileUploadReference = sessionData.connectedCharitiesScheduleFileUploadReference
    )
}

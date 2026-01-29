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
import models.SessionData
import utils.Required.required

import scala.util.Try

final case class RepaymentClaimDetailsAnswers(
  claimingGiftAid: Option[Boolean] = None,
  claimingTaxDeducted: Option[Boolean] = None,
  claimingUnderGiftAidSmallDonationsScheme: Option[Boolean] = None,
  // only when claiming Gift Aid
  claimingReferenceNumber: Option[Boolean] = None,
  claimReferenceNumber: Option[String] = None,
  // only when claiming under GASDS
  claimingDonationsNotFromCommunityBuilding: Option[Boolean] = None,
  claimingDonationsCollectedInCommunityBuildings: Option[Boolean] = None,
  connectedToAnyOtherCharities: Option[Boolean] = None,
  makingAdjustmentToPreviousClaim: Option[Boolean] = None,
  // only for agents
  hmrcCharitiesReference: Option[String] = None,
  nameOfCharity: Option[String] = None
) {

  def missingFields: List[String] =
    List(
      claimingGiftAid.isEmpty                                                  -> "claimingGiftAid.heading",
      claimingTaxDeducted.isEmpty                                              -> "claimingOtherIncome.heading",
      claimingUnderGiftAidSmallDonationsScheme.isEmpty                         -> "claimingGiftAidSmallDonations.heading",
      claimingReferenceNumber.isEmpty                                          -> "claimReferenceNumberCheck.heading",
      (claimingReferenceNumber.contains(true) && claimReferenceNumber.isEmpty) -> "claimReferenceNumberInput.heading"
      // TODO: add GASDS fields once pages are implemented
      // claimingUnderGiftAidSmallDonationsScheme.contains(true) && claimingDonationsNotFromCommunityBuilding.isEmpty -> "claimingDonationsNotFromCommunityBuilding.heading",
      // claimingUnderGiftAidSmallDonationsScheme.contains(true) && claimingDonationsCollectedInCommunityBuildings.isEmpty -> "claimingDonationsCollectedInCommunityBuildings.heading",
      // claimingUnderGiftAidSmallDonationsScheme.contains(true) && connectedToAnyOtherCharities.isEmpty -> "connectedToAnyOtherCharities.heading"
    ).collect { case (true, key) => key }

  def hasRepaymentClaimDetailsCompleteAnswers: Boolean = missingFields.isEmpty
}

object RepaymentClaimDetailsAnswers {

  given Format[RepaymentClaimDetailsAnswers] = Json.format[RepaymentClaimDetailsAnswers]

  private def get[A](f: RepaymentClaimDetailsAnswers => Option[A])(using session: SessionData): Option[A] =
    session.repaymentClaimDetailsAnswers.flatMap(f)

  private def set[A](value: A)(f: (RepaymentClaimDetailsAnswers, A) => RepaymentClaimDetailsAnswers)(using
    session: SessionData
  ): SessionData =
    val updated = session.repaymentClaimDetailsAnswers match
      case Some(existing) => f(existing, value)
      case None           => f(RepaymentClaimDetailsAnswers(), value)
    session.copy(repaymentClaimDetailsAnswers = Some(updated))

  def from(repaymentClaimDetails: RepaymentClaimDetails): RepaymentClaimDetailsAnswers =
    RepaymentClaimDetailsAnswers(
      claimingGiftAid = Some(repaymentClaimDetails.claimingGiftAid),
      claimingTaxDeducted = Some(repaymentClaimDetails.claimingTaxDeducted),
      claimingUnderGiftAidSmallDonationsScheme = Some(repaymentClaimDetails.claimingUnderGiftAidSmallDonationsScheme),
      claimingReferenceNumber = Some(repaymentClaimDetails.claimReferenceNumber.isDefined),
      claimReferenceNumber = repaymentClaimDetails.claimReferenceNumber,
      claimingDonationsNotFromCommunityBuilding = repaymentClaimDetails.claimingDonationsNotFromCommunityBuilding,
      claimingDonationsCollectedInCommunityBuildings =
        repaymentClaimDetails.claimingDonationsCollectedInCommunityBuildings,
      connectedToAnyOtherCharities = repaymentClaimDetails.connectedToAnyOtherCharities,
      makingAdjustmentToPreviousClaim = repaymentClaimDetails.makingAdjustmentToPreviousClaim,
      hmrcCharitiesReference = repaymentClaimDetails.hmrcCharitiesReference,
      nameOfCharity = repaymentClaimDetails.nameOfCharity
    )

  def getClaimingTaxDeducted(using session: SessionData): Option[Boolean] = get(_.claimingTaxDeducted)

  def setClaimingTaxDeducted(value: Boolean)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(claimingTaxDeducted = Some(v)))
      .copy(
        otherIncomeScheduleData = if (value) session.otherIncomeScheduleData else None,
        otherIncomeScheduleFileUploadReference = if (value) session.otherIncomeScheduleFileUploadReference else None
      )

  def shouldWarnAboutChangingClaimingTaxDeducted(value: Boolean)(using session: SessionData): Boolean =
    !value && session.otherIncomeScheduleFileUploadReference.isDefined

  def getMakingAdjustmentToPreviousClaim(using session: SessionData): Option[Boolean] = get(
    _.makingAdjustmentToPreviousClaim
  )

  def setMakingAdjustmentToPreviousClaim(value: Boolean)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(makingAdjustmentToPreviousClaim = Some(v)))

  def getClaimingGiftAid(using session: SessionData): Option[Boolean] = get(_.claimingGiftAid)

  def setClaimingGiftAid(value: Boolean)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(claimingGiftAid = Some(v)))
      .copy(
        giftAidScheduleData = if (value) session.giftAidScheduleData else None,
        giftAidScheduleFileUploadReference = if (value) session.giftAidScheduleFileUploadReference else None
      )

  def shouldWarnAboutChangingClaimingGiftAid(claimingGiftAid: Boolean)(using session: SessionData): Boolean =
    !claimingGiftAid && session.giftAidScheduleFileUploadReference.isDefined

  def getClaimingDonationsCollectedInCommunityBuildings(using session: SessionData): Option[Boolean] = get(
    _.claimingDonationsCollectedInCommunityBuildings
  )

  def setClaimingDonationsCollectedInCommunityBuildings(value: Boolean)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(claimingDonationsCollectedInCommunityBuildings = Some(v)))

  def getClaimingUnderGiftAidSmallDonationsScheme(using session: SessionData): Option[Boolean] = get(
    _.claimingUnderGiftAidSmallDonationsScheme
  )

  def setConnectedToAnyOtherCharities(value: Boolean)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(connectedToAnyOtherCharities = Some(v)))

  def getConnectedToAnyOtherCharities(using session: SessionData): Option[Boolean] = get(
    _.connectedToAnyOtherCharities
  )

  def setClaimingUnderGiftAidSmallDonationsScheme(value: Boolean)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(claimingUnderGiftAidSmallDonationsScheme = Some(v)))
      .copy(giftAidSmallDonationsSchemeDonationDetailsAnswers =
        if (value) session.giftAidSmallDonationsSchemeDonationDetailsAnswers else None
      )

  def getHmrcCharitiesReference(using session: SessionData): Option[String] =
    get(_.hmrcCharitiesReference)

  def setHmrcCharitiesReference(value: String)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(hmrcCharitiesReference = Some(v)))

  def getNameOfCharity(using session: SessionData): Option[String] =
    get(_.nameOfCharity)

  def setNameOfCharity(value: String)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(nameOfCharity = Some(v)))

  def shouldWarnAboutChangingClaimingUnderGiftAidSmallDonationsScheme(value: Boolean)(using
    session: SessionData
  ): Boolean =
    !value && session.giftAidSmallDonationsSchemeDonationDetailsAnswers.isDefined

  def getClaimingReferenceNumber(using session: SessionData): Option[Boolean] = get(_.claimingReferenceNumber)

  def setClaimingReferenceNumber(value: Boolean)(using session: SessionData): SessionData =
    set(value) { (answers, isClaiming) =>
      if (isClaiming) {
        answers.copy(claimingReferenceNumber = Some(true))
      } else {
        answers.copy(claimingReferenceNumber = Some(false), claimReferenceNumber = None)
      }
    }

  def getClaimReferenceNumber(using session: SessionData): Option[String] = get(_.claimReferenceNumber)

  def setClaimReferenceNumber(value: String)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(claimReferenceNumber = Some(v)))

  def toRepaymentClaimDetails(answers: RepaymentClaimDetailsAnswers): Try[RepaymentClaimDetails] =
    for
      claimingGiftAid                          <- required(answers)(_.claimingGiftAid)
      claimingTaxDeducted                      <- required(answers)(_.claimingTaxDeducted)
      claimingUnderGiftAidSmallDonationsScheme <- required(answers)(_.claimingUnderGiftAidSmallDonationsScheme)
    yield RepaymentClaimDetails(
      claimingGiftAid = claimingGiftAid,
      claimingTaxDeducted = claimingTaxDeducted,
      claimingUnderGiftAidSmallDonationsScheme = claimingUnderGiftAidSmallDonationsScheme,
      claimReferenceNumber = answers.claimReferenceNumber,
      claimingDonationsNotFromCommunityBuilding = answers.claimingDonationsNotFromCommunityBuilding,
      claimingDonationsCollectedInCommunityBuildings = answers.claimingDonationsCollectedInCommunityBuildings,
      connectedToAnyOtherCharities = answers.connectedToAnyOtherCharities,
      makingAdjustmentToPreviousClaim = answers.makingAdjustmentToPreviousClaim,
      hmrcCharitiesReference = answers.hmrcCharitiesReference,
      nameOfCharity = answers.nameOfCharity
    )
}

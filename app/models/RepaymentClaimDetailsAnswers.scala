/*
 * Copyright 2026 HM Revenue & Customs
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

import scala.util.Try
import scala.util.Success
import scala.util.Failure

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

  def repaymentClaimType: Option[RepaymentClaimType] =
    for
      claimingGiftAid                          <- claimingGiftAid
      claimingTaxDeducted                      <- claimingTaxDeducted
      claimingUnderGiftAidSmallDonationsScheme <- claimingUnderGiftAidSmallDonationsScheme
    yield RepaymentClaimType(claimingGiftAid, claimingTaxDeducted, claimingUnderGiftAidSmallDonationsScheme)

  def missingFields: List[String] =
    List(
      (claimingGiftAid.isEmpty
        && claimingTaxDeducted.isEmpty
        && claimingUnderGiftAidSmallDonationsScheme.isEmpty)       -> "repaymentClaimType.missingDetails",
      (claimingUnderGiftAidSmallDonationsScheme.contains(true)
        && claimingDonationsNotFromCommunityBuilding.isEmpty)      -> "claimGASDS.missingDetails",
      (claimingUnderGiftAidSmallDonationsScheme.contains(true)
        && claimingDonationsCollectedInCommunityBuildings.isEmpty) -> "claimingCommunityBuildingDonations.missingDetails",
      (claimingUnderGiftAidSmallDonationsScheme.contains(true)
        && ((claimingDonationsNotFromCommunityBuilding.contains(true)
          || claimingDonationsCollectedInCommunityBuildings.contains(true))
          && makingAdjustmentToPreviousClaim.isEmpty))             -> "changePreviousGASDSClaim.missingDetails",
      (claimingUnderGiftAidSmallDonationsScheme.contains(true)
        && connectedToAnyOtherCharities.isEmpty)                   -> "connectedToAnyOtherCharities.missingDetails",
      claimingReferenceNumber.isEmpty                              -> "claimReferenceNumberCheck.missingDetails",
      (claimingReferenceNumber.contains(
        true
      ) && claimReferenceNumber.isEmpty)                           -> "claimReferenceNumberInput.missingDetails"
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

  def getMissingFields(answers: Option[RepaymentClaimDetailsAnswers]): List[String] =
    answers match
      case Some(a) => a.missingFields
      case None    => defaultMissingFields

  private val defaultMissingFields: List[String] = List(
    "repaymentClaimType.missingDetails",
    "claimReferenceNumberCheck.missingDetails"
  )

  def getClaimingTaxDeducted(using session: SessionData): Option[Boolean] = get(_.claimingTaxDeducted)

  def setClaimingTaxDeducted(value: Boolean)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(claimingTaxDeducted = Some(v)))
      .copy(
        otherIncomeScheduleData = if (value) session.otherIncomeScheduleData else None,
        otherIncomeScheduleFileUploadReference = if (value) session.otherIncomeScheduleFileUploadReference else None
      )

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

  def setClaimingConnectedCharities(value: Boolean)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(connectedToAnyOtherCharities = Some(v)))
      .copy(
        connectedCharitiesScheduleData = if (value) session.connectedCharitiesScheduleData else None,
        connectedCharitiesScheduleFileUploadReference =
          if (value) session.connectedCharitiesScheduleFileUploadReference else None
      )

  def getRepaymentClaimType(using session: SessionData): Option[RepaymentClaimType] = get(answers =>
    for
      claimingGiftAid                          <- answers.claimingGiftAid
      claimingTaxDeducted                      <- answers.claimingTaxDeducted
      claimingUnderGiftAidSmallDonationsScheme <- answers.claimingUnderGiftAidSmallDonationsScheme
    yield RepaymentClaimType(
      claimingGiftAid,
      claimingTaxDeducted,
      claimingUnderGiftAidSmallDonationsScheme
    )
  )

  def setRepaymentClaimType(value: RepaymentClaimType, prevAnswer: Option[RepaymentClaimType])(using
    session: SessionData
  ): SessionData = {
    val updatedSession = set(value)((a, v) =>
      a.copy(
        claimingGiftAid = Some(v.claimingGiftAid),
        claimingTaxDeducted = Some(v.claimingTaxDeducted),
        claimingUnderGiftAidSmallDonationsScheme = Some(v.claimingUnderGiftAidSmallDonationsScheme)
      )
    )
    (value.claimingUnderGiftAidSmallDonationsScheme, prevAnswer) match {
      case (false, Some(prev)) if prev.claimingUnderGiftAidSmallDonationsScheme =>
        clearRepaymentClaimTypeFlow(using updatedSession)
      case (_, _)                                                               => updatedSession
    }
  }

  private def clearRepaymentClaimTypeFlow(using session: SessionData): SessionData =
    val updated = session.repaymentClaimDetailsAnswers match
      case Some(existing) =>
        existing.copy(
          makingAdjustmentToPreviousClaim = None,
          claimingDonationsNotFromCommunityBuilding = None,
          claimingDonationsCollectedInCommunityBuildings = None,
          connectedToAnyOtherCharities = None
        )
      case None           => RepaymentClaimDetailsAnswers()
    session.copy(repaymentClaimDetailsAnswers = Some(updated))

  def getClaimingDonationsCollectedInCommunityBuildings(using session: SessionData): Option[Boolean] = get(
    _.claimingDonationsCollectedInCommunityBuildings
  )

  // clear makingAdjustmentToPreviousClaim if claimingDonationsCollectedInCommunityBuildings & claimingDonationsNotFromCommunityBuilding are both false
  def setClaimingDonationsCollectedInCommunityBuildings(value: Boolean, prevScreenAnswer: Option[Boolean])(using
    session: SessionData
  ): SessionData = {
    val updatedSession = set(value)((a, v) => a.copy(claimingDonationsCollectedInCommunityBuildings = Some(v)))
    (value, prevScreenAnswer) match {
      case (false, Some(false)) => clearMakingAdjustmentToPreviousClaim(using updatedSession)
      case (_, _)               => updatedSession
    }
  }

  def getClaimingUnderGiftAidSmallDonationsScheme(using session: SessionData): Option[Boolean] = get(
    _.claimingUnderGiftAidSmallDonationsScheme
  )

  def setConnectedToAnyOtherCharities(value: Boolean)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(connectedToAnyOtherCharities = Some(v)))

  def getConnectedToAnyOtherCharities(using session: SessionData): Option[Boolean] = get(
    _.connectedToAnyOtherCharities
  )

  // clear makingAdjustmentToPreviousClaim if claimingDonationsCollectedInCommunityBuildings & claimingDonationsNotFromCommunityBuilding are both false
  def setClaimingDonationsNotFromCommunityBuilding(value: Boolean, nextScreenAnswer: Option[Boolean] = None)(using
    session: SessionData
  ): SessionData = {
    val updatedSession = set(value)((a, v) => a.copy(claimingDonationsNotFromCommunityBuilding = Some(v)))
    (value, nextScreenAnswer) match {
      case (false, Some(false)) => clearMakingAdjustmentToPreviousClaim(using updatedSession)
      case (_, _)               => updatedSession
    }
  }

  private def clearMakingAdjustmentToPreviousClaim(using session: SessionData): SessionData =
    val updated = session.repaymentClaimDetailsAnswers match
      case Some(existing) =>
        existing.copy(
          makingAdjustmentToPreviousClaim = None
        )
      case None           => RepaymentClaimDetailsAnswers()
    session.copy(repaymentClaimDetailsAnswers = Some(updated))

  def getClaimingDonationsNotFromCommunityBuilding(using session: SessionData): Option[Boolean] = get(
    _.claimingDonationsNotFromCommunityBuilding
  )

  def isClaimingGASDSWithDonations(using session: SessionData): Boolean =
    getClaimingUnderGiftAidSmallDonationsScheme.contains(true) &&
      (getClaimingDonationsCollectedInCommunityBuildings.contains(true) ||
        getClaimingDonationsNotFromCommunityBuilding.contains(true))

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
    for repaymentClaimType <- answers.repaymentClaimType.match {
                                case Some(repaymentClaimType) => Success(repaymentClaimType)
                                case None                     =>
                                  Failure(new MissingRequiredFieldsException("Mandatory fields are missing"))
                              }
    yield RepaymentClaimDetails(
      claimingGiftAid = repaymentClaimType.claimingGiftAid,
      claimingTaxDeducted = repaymentClaimType.claimingTaxDeducted,
      claimingUnderGiftAidSmallDonationsScheme = repaymentClaimType.claimingUnderGiftAidSmallDonationsScheme,
      claimReferenceNumber = answers.claimReferenceNumber,
      claimingDonationsNotFromCommunityBuilding = answers.claimingDonationsNotFromCommunityBuilding,
      claimingDonationsCollectedInCommunityBuildings = answers.claimingDonationsCollectedInCommunityBuildings,
      connectedToAnyOtherCharities = answers.connectedToAnyOtherCharities,
      makingAdjustmentToPreviousClaim = answers.makingAdjustmentToPreviousClaim,
      hmrcCharitiesReference = answers.hmrcCharitiesReference,
      nameOfCharity = answers.nameOfCharity
    )
}

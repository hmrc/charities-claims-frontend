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

final case class RepaymentClaimDetailsAnswers(
  claimingGiftAid: Option[Boolean] = None,
  claimingTaxDeducted: Option[Boolean] = None,
  claimingUnderGasds: Option[Boolean] = None,
  // only when claiming Gift Aid
  claimingReferenceNumber: Option[Boolean] = None,
  claimReferenceNumber: Option[String] = None,
  // only when claiming under GASDS
  claimingDonationsNotFromCommunityBuilding: Option[Boolean] = None,
  claimingDonationsCollectedInCommunityBuildings: Option[Boolean] = None,
  connectedToAnyOtherCharities: Option[Boolean] = None,
  makingAdjustmentToPreviousClaim: Option[Boolean] = None
)

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
      claimingUnderGasds = Some(repaymentClaimDetails.claimingUnderGasds),
      claimingReferenceNumber = Some(repaymentClaimDetails.claimReferenceNumber.isDefined),
      claimReferenceNumber = repaymentClaimDetails.claimReferenceNumber,
      claimingDonationsNotFromCommunityBuilding = repaymentClaimDetails.claimingDonationsNotFromCommunityBuilding,
      claimingDonationsCollectedInCommunityBuildings =
        repaymentClaimDetails.claimingDonationsCollectedInCommunityBuildings,
      connectedToAnyOtherCharities = repaymentClaimDetails.connectedToAnyOtherCharities,
      makingAdjustmentToPreviousClaim = repaymentClaimDetails.makingAdjustmentToPreviousClaim
    )

  def getClaimingTaxDeducted(using session: SessionData): Option[Boolean] = get(_.claimingTaxDeducted)

  def setClaimingTaxDeducted(value: Boolean)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(claimingTaxDeducted = Some(v)))

  def getClaimingGiftAid(using session: SessionData): Option[Boolean] = get(_.claimingGiftAid)

  def setClaimingGiftAid(value: Boolean)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(claimingGiftAid = Some(v)))

  def getClaimingUnderGasds(using session: SessionData): Option[Boolean] = get(_.claimingUnderGasds)

  def setClaimingUnderGasds(value: Boolean)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(claimingUnderGasds = Some(v)))

  def getClaimingReferenceNumber(using session: SessionData): Option[Boolean] = get(_.claimingReferenceNumber)

  def setClaimingReferenceNumber(value: Boolean)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(claimingReferenceNumber = Some(v)))

  def getClaimReferenceNumber(using session: SessionData): Option[String] = get(_.claimReferenceNumber)

  def setClaimReferenceNumber(value: String)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(claimReferenceNumber = Some(v)))
}

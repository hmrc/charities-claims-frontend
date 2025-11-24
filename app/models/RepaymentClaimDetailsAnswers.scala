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
  claimingReferenceNumber: Option[Boolean] = None,
  claimReferenceNumber: Option[String] = None
)

object RepaymentClaimDetailsAnswers {

  given Format[RepaymentClaimDetailsAnswers] = Json.format[RepaymentClaimDetailsAnswers]

  def getClaimingTaxDeducted(using session: SessionData): Option[Boolean] =
    session.repaymentClaimDetailsAnswers.flatMap(_.claimingTaxDeducted)

  def setClaimingTaxDeducted(value: Boolean)(using session: SessionData): SessionData =
    val updated = session.repaymentClaimDetailsAnswers match
      case Some(s1) => s1.copy(claimingTaxDeducted = Some(value))
      case None     => RepaymentClaimDetailsAnswers(claimingTaxDeducted = Some(value))
    session.copy(repaymentClaimDetailsAnswers = Some(updated))

  def getClaimingGiftAid(using session: SessionData): Option[Boolean] =
    session.repaymentClaimDetailsAnswers.flatMap(_.claimingGiftAid)

  def setClaimingGiftAid(value: Boolean)(using session: SessionData): SessionData =
    val updated = session.repaymentClaimDetailsAnswers match
      case Some(s1) => s1.copy(claimingGiftAid = Some(value))
      case None     => RepaymentClaimDetailsAnswers(claimingGiftAid = Some(value))
    session.copy(repaymentClaimDetailsAnswers = Some(updated))

  def getClaimingUnderGasds(using session: SessionData): Option[Boolean] =
    session.repaymentClaimDetailsAnswers.flatMap(_.claimingUnderGasds)

  def setClaimingUnderGasds(value: Boolean)(using session: SessionData): SessionData =
    val updated = session.repaymentClaimDetailsAnswers match
      case Some(s1) => s1.copy(claimingUnderGasds = Some(value))
      case None     => RepaymentClaimDetailsAnswers(claimingUnderGasds = Some(value))
    session.copy(repaymentClaimDetailsAnswers = Some(updated))

  def getClaimingReferenceNumber(using session: SessionData): Option[Boolean] =
    session.repaymentClaimDetailsAnswers.flatMap(_.claimingReferenceNumber)

  def setClaimingReferenceNumber(value: Boolean)(using session: SessionData): SessionData =
    val updated = session.repaymentClaimDetailsAnswers match
      case Some(s1) => s1.copy(claimingReferenceNumber = Some(value))
      case None     => RepaymentClaimDetailsAnswers(claimingReferenceNumber = Some(value))
    session.copy(repaymentClaimDetailsAnswers = Some(updated))

  def getClaimReferenceNumber(using session: SessionData): Option[String] =
    session.repaymentClaimDetailsAnswers.flatMap(_.claimReferenceNumber)

  def setClaimReferenceNumber(value: String)(using session: SessionData): SessionData =
    val updated = session.repaymentClaimDetailsAnswers match
      case Some(s1) => s1.copy(claimReferenceNumber = Some(value))
      case None     => RepaymentClaimDetailsAnswers(claimReferenceNumber = Some(value))
    session.copy(repaymentClaimDetailsAnswers = Some(updated))
}

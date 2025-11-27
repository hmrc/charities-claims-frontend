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

final case class SessionData(
  sectionOneAnswers: Option[SectionOneAnswers] = None
)

object SessionData {
  given Format[SessionData] = Json.format[SessionData]

  object SectionOne {

    def getClaimingTaxDeducted(using session: SessionData): Option[Boolean] =
      session.sectionOneAnswers.flatMap(_.claimingTaxDeducted)

    def setClaimingTaxDeducted(value: Boolean)(using session: SessionData): SessionData =
      val updated = session.sectionOneAnswers match
        case Some(s1) => s1.copy(claimingTaxDeducted = Some(value))
        case None     => SectionOneAnswers(claimingTaxDeducted = Some(value))
      session.copy(sectionOneAnswers = Some(updated))

    def getClaimingGiftAid(using session: SessionData): Option[Boolean] =
      session.sectionOneAnswers.flatMap(_.claimingGiftAid)

    def setClaimingGiftAid(value: Boolean)(using session: SessionData): SessionData =
      val updated = session.sectionOneAnswers match
        case Some(s1) => s1.copy(claimingGiftAid = Some(value))
        case None     => SectionOneAnswers(claimingGiftAid = Some(value))
      session.copy(sectionOneAnswers = Some(updated))

    def getClaimingUnderGasds(using session: SessionData): Option[Boolean] =
      session.sectionOneAnswers.flatMap(_.claimingUnderGasds)

    def setClaimingUnderGasds(value: Boolean)(using session: SessionData): SessionData =
      val updated = session.sectionOneAnswers match
        case Some(s1) => s1.copy(claimingUnderGasds = Some(value))
        case None     => SectionOneAnswers(claimingUnderGasds = Some(value))
      session.copy(sectionOneAnswers = Some(updated))

    def getHasClaimReferenceNumber(using session: SessionData): Option[Boolean] =
      session.sectionOneAnswers.flatMap(_.hasClaimReferenceNumber)

    def setHasClaimReferenceNumber(value: Boolean)(using session: SessionData): SessionData =
      val updated = session.sectionOneAnswers match
        case Some(s1) => s1.copy(hasClaimReferenceNumber = Some(value))
        case None     => SectionOneAnswers(hasClaimReferenceNumber = Some(value))
      session.copy(sectionOneAnswers = Some(updated))
  }
}

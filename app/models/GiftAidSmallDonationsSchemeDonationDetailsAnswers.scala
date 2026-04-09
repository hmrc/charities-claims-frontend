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
import scala.util.Try
import utils.Required.required
import scala.util.Success

final case class GiftAidSmallDonationsSchemeDonationDetailsAnswers(
  adjustmentForGiftAidOverClaimed: Option[BigDecimal] = None,
  claims: Option[Seq[GiftAidSmallDonationsSchemeClaim]] = None
)

object GiftAidSmallDonationsSchemeDonationDetailsAnswers {

  given Format[GiftAidSmallDonationsSchemeDonationDetailsAnswers] =
    Json.format[GiftAidSmallDonationsSchemeDonationDetailsAnswers]

  private def get[A](f: GiftAidSmallDonationsSchemeDonationDetailsAnswers => Option[A])(using
    session: SessionData
  ): Option[A] =
    session.giftAidSmallDonationsSchemeDonationDetailsAnswers.flatMap(f)

  private def set[A](value: A)(
    f: (GiftAidSmallDonationsSchemeDonationDetailsAnswers, A) => GiftAidSmallDonationsSchemeDonationDetailsAnswers
  )(using
    session: SessionData
  ): SessionData =
    val updated = session.giftAidSmallDonationsSchemeDonationDetailsAnswers match
      case Some(existing) => f(existing, value)
      case None           => f(GiftAidSmallDonationsSchemeDonationDetailsAnswers(), value)
    session.copy(giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(updated))

  def from(
    giftAidSmallDonationsSchemeScheduleData: GiftAidSmallDonationsSchemeDonationDetails
  ): GiftAidSmallDonationsSchemeDonationDetailsAnswers =
    GiftAidSmallDonationsSchemeDonationDetailsAnswers(
      adjustmentForGiftAidOverClaimed = Some(giftAidSmallDonationsSchemeScheduleData.adjustmentForGiftAidOverClaimed),
      claims = Some(giftAidSmallDonationsSchemeScheduleData.claims)
    )

  def toGiftAidSmallDonationsSchemeDonationDetails(
    answers: GiftAidSmallDonationsSchemeDonationDetailsAnswers
  ): Try[GiftAidSmallDonationsSchemeDonationDetails] =
    Success(
      GiftAidSmallDonationsSchemeDonationDetails(
        adjustmentForGiftAidOverClaimed = answers.adjustmentForGiftAidOverClaimed.getOrElse(0),
        claims = answers.claims.getOrElse(Seq.empty),
        connectedCharitiesScheduleData = Seq.empty,
        communityBuildingsScheduleData = Seq.empty
      )
    )

  def setAdjustmentForGiftAidOverClaimed(value: BigDecimal)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(adjustmentForGiftAidOverClaimed = Some(v)))

  def getAdjustmentForGiftAidOverClaimed(using session: SessionData): Option[BigDecimal] =
    get(a => a.adjustmentForGiftAidOverClaimed)
}

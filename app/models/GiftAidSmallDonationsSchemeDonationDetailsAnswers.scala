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

import play.api.libs.json.{Format, Json, Reads, Writes}

import scala.util.{Failure, Try}

final case class GiftAidSmallDonationsSchemeDonationDetailsAnswers(
  adjustmentForGiftAidOverClaimed: Option[BigDecimal] = None,
  claims: Option[Seq[Option[GiftAidSmallDonationsSchemeClaimAnswers]]] = None
) {

  def missingFields: List[String] =
    claims match {
      case None                                         => List("giftAidSmallDonationsSchemeDonationDetails.missingDetails")
      case Some(claimSeq) if claimSeq.forall(_.isEmpty) =>
        List("giftAidSmallDonationsSchemeDonationDetails.missingDetails")
      case Some(claimSeq)                               =>
        claimSeq.zipWithIndex.collect { case (None, i) =>
          s"giftAidSmallDonationsSchemeDonationDetails.claim${i + 1}.missingDetails"
        }.toList
    }

}

object GiftAidSmallDonationsSchemeDonationDetailsAnswers {

  given [A](using f: Format[A]): Format[Seq[Option[A]]] = {
    val reads  = Reads.seq(Reads.optionWithNull(using f))
    val writes = Writes.seq(Writes.optionWithNull(using f))
    Format(reads, writes)
  }

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
      claims = Some(
        giftAidSmallDonationsSchemeScheduleData.claims.map(c =>
          Some(GiftAidSmallDonationsSchemeClaimAnswers(c.taxYear, Some(c.amountOfDonationsReceived)))
        )
      )
    )

  def toGiftAidSmallDonationsSchemeDonationDetails(
    answers: GiftAidSmallDonationsSchemeDonationDetailsAnswers
  ): Try[GiftAidSmallDonationsSchemeDonationDetails] =
    if answers.claims.exists(_.size > 3)
    then Failure(new MissingRequiredFieldsException("GASDS claims cannot be more than 3"))
    else
      Try(
        GiftAidSmallDonationsSchemeDonationDetails(
          adjustmentForGiftAidOverClaimed = answers.adjustmentForGiftAidOverClaimed.getOrElse(0),
          claims = answers.claims.toSeq
            .flatMap(_.flatten)
            .map(c =>
              GiftAidSmallDonationsSchemeClaim(
                c.taxYear,
                c.amountOfDonationsReceived
                  .getOrElse(
                    throw new MissingRequiredFieldsException(
                      s"Amount of donations received is missing for tax year ${c.taxYear}"
                    )
                  )
              )
            )
        )
      )

  def setAdjustmentForGiftAidOverClaimed(value: BigDecimal)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(adjustmentForGiftAidOverClaimed = Some(v)))

  def getAdjustmentForGiftAidOverClaimed(using session: SessionData): Option[BigDecimal] =
    get(a => a.adjustmentForGiftAidOverClaimed)

  def getClaims(using session: SessionData): Seq[Option[GiftAidSmallDonationsSchemeClaimAnswers]] =
    get(a => a.claims).getOrElse(Seq.empty)

  def getClaim(index: Int)(using session: SessionData): Option[GiftAidSmallDonationsSchemeClaimAnswers] =
    get(a => a.claims.flatMap(_.lift(index)).flatten)

  def allClaims(using session: SessionData): Seq[GiftAidSmallDonationsSchemeClaimAnswers] =
    get(_.claims).getOrElse(Seq.empty).flatten

  def isClaimExist(index: Int)(using session: SessionData): Boolean =
    getClaim(index).isDefined

  def isValidIndex(index: Int): Boolean =
    index >= 1 && index <= 3

  def setClaim(index: Int, value: GiftAidSmallDonationsSchemeClaimAnswers)(using session: SessionData): SessionData =
    set(value)((a, v) =>
      a.copy(claims = a.claims match {
        case Some(existing) if index < existing.length =>
          Some(existing.updated(index, Some(v)))

        case Some(existing) =>
          val gap = Seq.fill(index - existing.length)(None)
          Some(existing ++ gap :+ Some(v))

        case None =>
          Some(Seq.fill(index)(None) :+ Some(v))
      })
    )

  def removeClaim(index: Int)(using session: SessionData): SessionData =
    session.giftAidSmallDonationsSchemeDonationDetailsAnswers match {
      case Some(existing) =>
        val updatedClaims =
          existing.copy(claims = existing.claims.map(_.patch(from = index, other = Nil, replaced = 1)))
        session.copy(giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(updatedClaims))
      case None           =>
        session
    }

  def getClaimsSize(using session: SessionData): Int =
    session.giftAidSmallDonationsSchemeDonationDetailsAnswers.flatMap(_.claims.map(_.size)).getOrElse(0)

  def getMissingFields(answers: Option[GiftAidSmallDonationsSchemeDonationDetailsAnswers]): List[String] =
    answers match
      case Some(a) => a.missingFields
      case None    => defaultMissingFields

  private val defaultMissingFields: List[String] = List(
    "giftAidSmallDonationsSchemeDonationDetails.missingDetails"
  )

}

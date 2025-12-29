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

final case class GiftAidSmallDonationsSchemeDonationDetailsAnswers(
  adjustmentForGiftAidOverClaimed: Option[BigDecimal] = None,
  claims: Option[Seq[GiftAidSmallDonationsSchemeClaim]] = None,
  connectedCharitiesScheduleData: Option[Seq[ConnectedCharity]] = None,
  communityBuildingsScheduleData: Option[Seq[CommunityBuilding]] = None
)

object GiftAidSmallDonationsSchemeDonationDetailsAnswers {

  given Format[GiftAidSmallDonationsSchemeDonationDetailsAnswers] =
    Json.format[GiftAidSmallDonationsSchemeDonationDetailsAnswers]

  def from(
    giftAidSmallDonationsSchemeScheduleData: GiftAidSmallDonationsSchemeDonationDetails
  ): GiftAidSmallDonationsSchemeDonationDetailsAnswers =
    GiftAidSmallDonationsSchemeDonationDetailsAnswers(
      adjustmentForGiftAidOverClaimed = Some(giftAidSmallDonationsSchemeScheduleData.adjustmentForGiftAidOverClaimed),
      claims = Some(giftAidSmallDonationsSchemeScheduleData.claims),
      connectedCharitiesScheduleData = Some(giftAidSmallDonationsSchemeScheduleData.connectedCharitiesScheduleData),
      communityBuildingsScheduleData = Some(giftAidSmallDonationsSchemeScheduleData.communityBuildingsScheduleData)
    )

  def toGiftAidSmallDonationsSchemeDonationDetails(
    answers: GiftAidSmallDonationsSchemeDonationDetailsAnswers
  ): Try[GiftAidSmallDonationsSchemeDonationDetails] =
    for {
      adjustmentForGiftAidOverClaimed <- required(answers)(_.adjustmentForGiftAidOverClaimed)
      claims                          <- required(answers)(_.claims)
      connectedCharitiesScheduleData  <- required(answers)(_.connectedCharitiesScheduleData)
      communityBuildingsScheduleData  <- required(answers)(_.communityBuildingsScheduleData)
    } yield GiftAidSmallDonationsSchemeDonationDetails(
      adjustmentForGiftAidOverClaimed = adjustmentForGiftAidOverClaimed,
      claims = claims,
      connectedCharitiesScheduleData = connectedCharitiesScheduleData,
      communityBuildingsScheduleData = communityBuildingsScheduleData
    )
}

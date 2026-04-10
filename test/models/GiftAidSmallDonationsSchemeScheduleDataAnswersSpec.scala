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

import util.BaseSpec
import play.api.libs.json.Json

class GiftAidSmallDonationsSchemeScheduleDataAnswersSpec extends BaseSpec {

  "GiftAidSmallDonationsSchemeScheduleDataAnswerss" - {
    "be serializable and deserializable" in {
      val giftAidSmallDonationsSchemeScheduleDataAnswerss = GiftAidSmallDonationsSchemeDonationDetailsAnswers(
        adjustmentForGiftAidOverClaimed = Some(1000.00),
        claims = Some(Seq(GiftAidSmallDonationsSchemeClaim(taxYear = 2025, amountOfDonationsReceived = 1000.00)))
      )

      val json = Json.toJson(giftAidSmallDonationsSchemeScheduleDataAnswerss)

      val deserializedGiftAidSmallDonationsSchemeScheduleDataAnswerss =
        json.as[GiftAidSmallDonationsSchemeDonationDetailsAnswers]
      deserializedGiftAidSmallDonationsSchemeScheduleDataAnswerss shouldBe giftAidSmallDonationsSchemeScheduleDataAnswerss
    }

    "be created from GiftAidSmallDonationsSchemeScheduleData" in {
      val giftAidSmallDonationsSchemeScheduleData = GiftAidSmallDonationsSchemeDonationDetails(
        adjustmentForGiftAidOverClaimed = 1000.00,
        claims = Seq(GiftAidSmallDonationsSchemeClaim(taxYear = 2025, amountOfDonationsReceived = 1000.00)),
        connectedCharitiesScheduleData = Seq.empty,
        communityBuildingsScheduleData = Seq.empty
      )

      val giftAidSmallDonationsSchemeScheduleDataAnswers =
        GiftAidSmallDonationsSchemeDonationDetailsAnswers.from(giftAidSmallDonationsSchemeScheduleData)

      giftAidSmallDonationsSchemeScheduleDataAnswers shouldBe GiftAidSmallDonationsSchemeDonationDetailsAnswers(
        adjustmentForGiftAidOverClaimed = Some(1000.00),
        claims = Some(Seq(GiftAidSmallDonationsSchemeClaim(taxYear = 2025, amountOfDonationsReceived = 1000.00)))
      )
    }

  }
}

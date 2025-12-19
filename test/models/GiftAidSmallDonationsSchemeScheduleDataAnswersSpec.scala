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
        claims = Some(Seq(GiftAidSmallDonationsSchemeClaim(taxYear = 2025, amountOfDonationsReceived = 1000.00))),
        connectedCharitiesScheduleData =
          Some(Seq(ConnectedCharity(charityItem = 1, charityName = "foobar", charityReference = "1234567890"))),
        communityBuildingsScheduleData = Some(
          Seq(
            CommunityBuilding(
              buildingItem = 1,
              buildingName = "foobar",
              firstLineOfAddress = "123",
              postcode = "AB1 2CD",
              taxYearOneEnd = 2025,
              taxYearOneAmount = 1001.00,
              taxYearTwoEnd = 2026,
              taxYearTwoAmount = 1002.00,
              taxYearThreeEnd = 2027,
              taxYearThreeAmount = 1003.00
            )
          )
        )
      )

      val json = Json.toJson(giftAidSmallDonationsSchemeScheduleDataAnswerss)

      val deserializedGiftAidSmallDonationsSchemeScheduleDataAnswerss =
        json.as[GiftAidSmallDonationsSchemeDonationDetailsAnswers]
      deserializedGiftAidSmallDonationsSchemeScheduleDataAnswerss shouldBe giftAidSmallDonationsSchemeScheduleDataAnswerss
    }

    "be created from GiftAidSmallDonationsSchemeScheduleData" in {
      val giftAidSmallDonationsSchemeScheduleData = giftAidSmallDonationsSchemeDonationDetails(
        adjustmentForGiftAidOverClaimed = 1000.00,
        claims = Seq(GiftAidSmallDonationsSchemeClaim(taxYear = 2025, amountOfDonationsReceived = 1000.00)),
        connectedCharitiesScheduleData =
          Seq(ConnectedCharity(charityItem = 1, charityName = "foobar", charityReference = "1234567890")),
        communityBuildingsScheduleData = Seq(
          CommunityBuilding(
            buildingItem = 1,
            buildingName = "foobar",
            firstLineOfAddress = "123",
            postcode = "AB1 2CD",
            taxYearOneEnd = 2025,
            taxYearOneAmount = 1001.00,
            taxYearTwoEnd = 2026,
            taxYearTwoAmount = 1002.00,
            taxYearThreeEnd = 2027,
            taxYearThreeAmount = 1003.00
          )
        )
      )

      val giftAidSmallDonationsSchemeScheduleDataAnswers =
        GiftAidSmallDonationsSchemeDonationDetailsAnswers.from(giftAidSmallDonationsSchemeScheduleData)

      giftAidSmallDonationsSchemeScheduleDataAnswers shouldBe GiftAidSmallDonationsSchemeDonationDetailsAnswers(
        adjustmentForGiftAidOverClaimed = Some(1000.00),
        claims = Some(Seq(GiftAidSmallDonationsSchemeClaim(taxYear = 2025, amountOfDonationsReceived = 1000.00))),
        connectedCharitiesScheduleData =
          Some(Seq(ConnectedCharity(charityItem = 1, charityName = "foobar", charityReference = "1234567890"))),
        communityBuildingsScheduleData = Some(
          Seq(
            CommunityBuilding(
              buildingItem = 1,
              buildingName = "foobar",
              firstLineOfAddress = "123",
              postcode = "AB1 2CD",
              taxYearOneEnd = 2025,
              taxYearOneAmount = 1001.00,
              taxYearTwoEnd = 2026,
              taxYearTwoAmount = 1002.00,
              taxYearThreeEnd = 2027,
              taxYearThreeAmount = 1003.00
            )
          )
        )
      )
    }

  }
}

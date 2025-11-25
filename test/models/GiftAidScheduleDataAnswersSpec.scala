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

class GiftAidScheduleDataAnswersSpec extends BaseSpec {

  "GiftAidScheduleDataAnswers" - {
    "be serializable and deserializable" in {
      val giftAidScheduleDataAnswers = GiftAidScheduleDataAnswers(
        earliestDonationDate = Some("2025-01-01"),
        prevOverclaimedGiftAid = Some(1000.00),
        totalDonations = Some(1000.00),
        donations = Some(
          Seq(
            Donation(
              donationItem = 1,
              donationDate = "2025-01-01",
              donationAmount = 1000.00,
              donorTitle = Some("Mr"),
              donorFirstName = Some("John"),
              donorLastName = Some("Doe"),
              donorHouse = Some("123"),
              donorPostcode = Some("AB1 2CD"),
              sponsoredEvent = Some(true),
              aggregatedDonations = Some("1234567890")
            )
          )
        )
      )

      val json                                   = Json.toJson(giftAidScheduleDataAnswers)
      val deserializedGiftAidScheduleDataAnswers = json.as[GiftAidScheduleDataAnswers]
      deserializedGiftAidScheduleDataAnswers shouldBe giftAidScheduleDataAnswers
    }

    "be created from GiftAidScheduleData" in {
      val giftAidScheduleData = GiftAidScheduleData(
        earliestDonationDate = "2025-01-01",
        prevOverclaimedGiftAid = 1000.00,
        totalDonations = 2000.00,
        donations = Seq(
          Donation(
            donationItem = 1,
            donationDate = "2025-01-01",
            donationAmount = 1200.00,
            donorTitle = Some("Mr"),
            donorFirstName = Some("John"),
            donorLastName = Some("Doe"),
            donorHouse = Some("123"),
            donorPostcode = Some("AB1 2CD"),
            sponsoredEvent = Some(true),
            aggregatedDonations = Some("1234567890")
          )
        )
      )

      val giftAidScheduleDataAnswers = GiftAidScheduleDataAnswers.from(giftAidScheduleData)

      giftAidScheduleDataAnswers.shouldBe(
        GiftAidScheduleDataAnswers(
          earliestDonationDate = Some("2025-01-01"),
          prevOverclaimedGiftAid = Some(1000.00d),
          totalDonations = Some(2000.00d),
          donations = Some(
            Seq(
              Donation(
                donationItem = 1,
                donationDate = "2025-01-01",
                donationAmount = 1200.00d,
                donorTitle = Some("Mr"),
                donorFirstName = Some("John"),
                donorLastName = Some("Doe"),
                donorHouse = Some("123"),
                donorPostcode = Some("AB1 2CD"),
                sponsoredEvent = Some(true),
                aggregatedDonations = Some("1234567890")
              )
            )
          )
        )
      )
    }

  }

}

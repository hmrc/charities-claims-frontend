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
import scala.util.Success

class GiftAidSmallDonationsSchemeDonationDetailsAnswersSpec extends BaseSpec {

  "GiftAidSmallDonationsSchemeScheduleDataAnswers" - {
    "be serializable and deserializable" in {
      val giftAidSmallDonationsSchemeScheduleDataAnswers = GiftAidSmallDonationsSchemeDonationDetailsAnswers(
        adjustmentForGiftAidOverClaimed = Some(1000.00),
        claims = Some(
          Seq(
            Some(
              GiftAidSmallDonationsSchemeClaim(taxYear = 2025, amountOfDonationsReceived = Some(BigDecimal(1000.00)))
            )
          )
        )
      )

      val json = Json.toJson(giftAidSmallDonationsSchemeScheduleDataAnswers)

      val deserializedGiftAidSmallDonationsSchemeScheduleDataAnswers =
        json.as[GiftAidSmallDonationsSchemeDonationDetailsAnswers]
      deserializedGiftAidSmallDonationsSchemeScheduleDataAnswers shouldBe giftAidSmallDonationsSchemeScheduleDataAnswers
    }

    "be created from GiftAidSmallDonationsSchemeScheduleData" in {
      val giftAidSmallDonationsSchemeScheduleData = GiftAidSmallDonationsSchemeDonationDetails(
        adjustmentForGiftAidOverClaimed = 1000.00,
        claims =
          Seq(GiftAidSmallDonationsSchemeClaim(taxYear = 2025, amountOfDonationsReceived = Some(BigDecimal(1000.00))))
      )

      val giftAidSmallDonationsSchemeScheduleDataAnswers =
        GiftAidSmallDonationsSchemeDonationDetailsAnswers.from(giftAidSmallDonationsSchemeScheduleData)

      giftAidSmallDonationsSchemeScheduleDataAnswers shouldBe GiftAidSmallDonationsSchemeDonationDetailsAnswers(
        adjustmentForGiftAidOverClaimed = Some(1000.00),
        claims = Some(
          Seq(
            Some(
              GiftAidSmallDonationsSchemeClaim(taxYear = 2025, amountOfDonationsReceived = Some(BigDecimal(1000.00)))
            )
          )
        )
      )
    }

    "get claims when there is only one" in {
      val claims = Seq(
        Some(GiftAidSmallDonationsSchemeClaim(taxYear = 2025, amountOfDonationsReceived = Some(BigDecimal(1000.00))))
      )

      given SessionData = SessionData(
        charitiesReference = "1234567890",
        giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
          GiftAidSmallDonationsSchemeDonationDetailsAnswers(
            claims = Some(claims)
          )
        )
      )

      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaims      shouldBe claims
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaimsSize  shouldBe 1
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaim(0)    shouldBe claims(0)
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaim(1)    shouldBe None
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaim(2)    shouldBe None
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaim(3)    shouldBe None
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.removeClaim(0) shouldBe
        SessionData(
          charitiesReference = "1234567890",
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
            GiftAidSmallDonationsSchemeDonationDetailsAnswers(
              claims = Some(Seq())
            )
          )
        )
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.removeClaim(1) shouldBe
        SessionData(
          charitiesReference = "1234567890",
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
            GiftAidSmallDonationsSchemeDonationDetailsAnswers(
              claims = Some(Seq(claims(0)))
            )
          )
        )
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.removeClaim(2) shouldBe
        SessionData(
          charitiesReference = "1234567890",
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
            GiftAidSmallDonationsSchemeDonationDetailsAnswers(
              claims = Some(Seq(claims(0)))
            )
          )
        )
    }

    "get claims when there are two" in {
      val claims = Seq(
        Some(GiftAidSmallDonationsSchemeClaim(taxYear = 2025, amountOfDonationsReceived = Some(BigDecimal(1000.00)))),
        Some(GiftAidSmallDonationsSchemeClaim(taxYear = 2026, amountOfDonationsReceived = Some(BigDecimal(2000.00))))
      )

      given SessionData = SessionData(
        charitiesReference = "1234567890",
        giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
          GiftAidSmallDonationsSchemeDonationDetailsAnswers(
            claims = Some(claims)
          )
        )
      )

      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaims      shouldBe claims
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaimsSize  shouldBe 2
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaim(0)    shouldBe claims(0)
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaim(1)    shouldBe claims(1)
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaim(2)    shouldBe None
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaim(3)    shouldBe None
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.removeClaim(0) shouldBe
        SessionData(
          charitiesReference = "1234567890",
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
            GiftAidSmallDonationsSchemeDonationDetailsAnswers(
              claims = Some(Seq(claims(1)))
            )
          )
        )
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.removeClaim(1) shouldBe
        SessionData(
          charitiesReference = "1234567890",
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
            GiftAidSmallDonationsSchemeDonationDetailsAnswers(
              claims = Some(Seq(claims(0)))
            )
          )
        )
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.removeClaim(2) shouldBe
        SessionData(
          charitiesReference = "1234567890",
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
            GiftAidSmallDonationsSchemeDonationDetailsAnswers(
              claims = Some(Seq(claims(0), claims(1)))
            )
          )
        )
    }

    "get claims when there are three" in {
      val claims = Seq(
        Some(GiftAidSmallDonationsSchemeClaim(taxYear = 2025, amountOfDonationsReceived = Some(BigDecimal(1000.00)))),
        Some(GiftAidSmallDonationsSchemeClaim(taxYear = 2026, amountOfDonationsReceived = Some(BigDecimal(2000.00)))),
        Some(GiftAidSmallDonationsSchemeClaim(taxYear = 2027, amountOfDonationsReceived = Some(BigDecimal(3000.00))))
      )

      given SessionData = SessionData(
        charitiesReference = "1234567890",
        giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
          GiftAidSmallDonationsSchemeDonationDetailsAnswers(
            claims = Some(claims)
          )
        )
      )

      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaims      shouldBe claims
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaimsSize  shouldBe 3
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaim(0)    shouldBe claims(0)
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaim(1)    shouldBe claims(1)
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaim(2)    shouldBe claims(2)
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaim(3)    shouldBe None
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.removeClaim(0) shouldBe
        SessionData(
          charitiesReference = "1234567890",
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
            GiftAidSmallDonationsSchemeDonationDetailsAnswers(
              claims = Some(Seq(claims(1), claims(2)))
            )
          )
        )
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.removeClaim(1) shouldBe
        SessionData(
          charitiesReference = "1234567890",
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
            GiftAidSmallDonationsSchemeDonationDetailsAnswers(
              claims = Some(Seq(claims(0), claims(2)))
            )
          )
        )
      GiftAidSmallDonationsSchemeDonationDetailsAnswers.removeClaim(2) shouldBe
        SessionData(
          charitiesReference = "1234567890",
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
            GiftAidSmallDonationsSchemeDonationDetailsAnswers(
              claims = Some(Seq(claims(0), claims(1)))
            )
          )
        )
    }
    "setClaim should create a new claims sequence when none exists" in {
      given SessionData = SessionData.empty("1234567890")

      val claim = GiftAidSmallDonationsSchemeClaim(2025, Some(BigDecimal(100)))

      val updated = GiftAidSmallDonationsSchemeDonationDetailsAnswers.setClaim(0, claim)

      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaims(using updated) shouldBe
        Seq(Some(claim))
    }

    "setClaim should fill gaps with None when inserting beyond index 0" in {
      given SessionData = SessionData.empty("1234567890")

      val claim = GiftAidSmallDonationsSchemeClaim(2027, Some(BigDecimal(300)))

      val updated = GiftAidSmallDonationsSchemeDonationDetailsAnswers.setClaim(2, claim)

      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaims(using updated) shouldBe
        Seq(None, None, Some(claim))
    }

    "setClaim should update an existing claim at the given index" in {
      val original     = GiftAidSmallDonationsSchemeClaim(2025, Some(BigDecimal(100)))
      val updatedClaim = GiftAidSmallDonationsSchemeClaim(2026, Some(BigDecimal(200)))

      given SessionData = SessionData(
        charitiesReference = "1234567890",
        giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
          GiftAidSmallDonationsSchemeDonationDetailsAnswers(
            claims = Some(Seq(Some(original)))
          )
        )
      )

      val updated = GiftAidSmallDonationsSchemeDonationDetailsAnswers.setClaim(0, updatedClaim)

      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaims(using updated) shouldBe
        Seq(Some(updatedClaim))
    }

    "setClaim should add the claim with index 1, 2 and 3 if atleast one claim already exists" in {
      val existing = GiftAidSmallDonationsSchemeClaim(2025, Some(BigDecimal(100)))
      val newClaim = GiftAidSmallDonationsSchemeClaim(2026, Some(BigDecimal(200)))

      given SessionData = SessionData(
        charitiesReference = "1234567890",
        giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
          GiftAidSmallDonationsSchemeDonationDetailsAnswers(
            claims = Some(Seq(Some(existing))) // length = 1
          )
        )
      )

      val updated = GiftAidSmallDonationsSchemeDonationDetailsAnswers.setClaim(1, newClaim)

      GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaims(using updated) shouldBe
        Seq(Some(existing), Some(newClaim))
    }

    "set, get and remove a single claim at index" in {
      for (index <- 0 until 3) {
        given SessionData = SessionData.empty("1234567890")
        GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaim(index) shouldBe None
        val claim = GiftAidSmallDonationsSchemeClaim(
          taxYear = 2025 + index,
          amountOfDonationsReceived = Some(1000.00 * index)
        )

        GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaimsSize shouldBe 0

        val updatedSession = GiftAidSmallDonationsSchemeDonationDetailsAnswers.setClaim(index, claim)

        updatedSession shouldEqual SessionData(
          charitiesReference = "1234567890",
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
            GiftAidSmallDonationsSchemeDonationDetailsAnswers(
              claims = Some(Seq.fill(index)(None) :+ Some(claim))
            )
          )
        )
        GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaim(index - 1)(using updatedSession) shouldBe None
        GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaim(index)(using updatedSession)     shouldBe Some(claim)
        GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaim(index + 1)(using updatedSession) shouldBe None

        GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaimsSize(using updatedSession) shouldBe index + 1
        GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaims(using updatedSession)     shouldBe Seq.fill(index)(
          None
        ) :+ Some(claim)

        GiftAidSmallDonationsSchemeDonationDetailsAnswers.toGiftAidSmallDonationsSchemeDonationDetails(
          updatedSession.giftAidSmallDonationsSchemeDonationDetailsAnswers.get
        ) shouldBe Success(
          GiftAidSmallDonationsSchemeDonationDetails(
            adjustmentForGiftAidOverClaimed = 0,
            claims = Seq(claim)
          )
        )

        val updatedSession2 = GiftAidSmallDonationsSchemeDonationDetailsAnswers.removeClaim(index)(using
          updatedSession
        )

        GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaimsSize(using updatedSession2) shouldBe index

        updatedSession2 shouldEqual SessionData(
          charitiesReference = "1234567890",
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
            GiftAidSmallDonationsSchemeDonationDetailsAnswers(
              claims = Some(Seq.fill(index)(None))
            )
          )
        )

        GiftAidSmallDonationsSchemeDonationDetailsAnswers.removeClaim(index)(using
          updatedSession2
        ) shouldEqual SessionData(
          charitiesReference = "1234567890",
          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(
            GiftAidSmallDonationsSchemeDonationDetailsAnswers(
              claims = Some(Seq.fill(index)(None))
            )
          )
        )
      }
    }

  }
}

/*
 * Copyright 2026 HM Revenue & Customs
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

import models.{SessionData, SessionDataEncrypted}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.OptionValues
import play.api.libs.json.*
import uk.gov.hmrc.crypto.{Decrypter, Encrypter, SymmetricCryptoFactory}

class SessionDataEncryptedSpec
  extends AnyWordSpec
    with Matchers
    with OptionValues:

  private val aesKey = "nRvFTLnNmueAbaMqjpcHQDAmh3JFsMV/XDX3AUfULNE="

  private given crypto: Encrypter & Decrypter =
    SymmetricCryptoFactory.aesGcmCrypto(aesKey)

  private given OFormat[SessionDataEncrypted] =
    SessionDataEncrypted.format(using crypto)

  private val charitiesReference = "AB123456"
  private val unsubmittedClaimId = "claim-123"
  private val lastUpdatedReference = "last-updated-123"

  private val sessionData: SessionData =
    SessionData(
      charitiesReference = charitiesReference,
      unsubmittedClaimId = Some(unsubmittedClaimId),
      lastUpdatedReference = Some(lastUpdatedReference),
      claimSubmitted = Some(false),
      isAgent = true
    )

  "SessionDataEncrypted" should {

    "wrap SessionData and return original SessionData when decrypted" in {

      val encrypted =
        SessionDataEncrypted.fromSessionData(sessionData)

      encrypted.toSessionData shouldBe sessionData
    }

    "write SessionData as encrypted JSON without plaintext values" in {

      val encrypted =
        SessionDataEncrypted.fromSessionData(sessionData)

      val json =
        Json.toJson(encrypted)

      val rawJson =
        Json.stringify(json)

      rawJson should include("data")

      rawJson shouldNot include(charitiesReference)
      rawJson shouldNot include(unsubmittedClaimId)
      rawJson shouldNot include(lastUpdatedReference)
      rawJson shouldNot include("claimSubmitted")
      rawJson shouldNot include("isAgent")
    }

    "read encrypted JSON back to original SessionData" in {

      val encrypted =
        SessionDataEncrypted.fromSessionData(sessionData)

      val json =
        Json.toJson(encrypted)

      val readBack =
        json.as[SessionDataEncrypted]

      readBack.toSessionData shouldBe sessionData
    }

    "support full JSON round-trip" in {

      val encrypted =
        SessionDataEncrypted.fromSessionData(sessionData)

      val json =
        Json.toJson(encrypted)

      val result =
        Json.fromJson[SessionDataEncrypted](json)

      result.isSuccess shouldBe true
      result.get.toSessionData shouldBe sessionData
    }

    "fail to read when data field is missing" in {

      val json =
        Json.obj(
          "wrongField" -> "some encrypted value"
        )

      val result =
        Json.fromJson[SessionDataEncrypted](json)

      result.isError shouldBe true
    }
  }

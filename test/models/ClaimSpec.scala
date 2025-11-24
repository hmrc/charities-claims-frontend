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
import util.TestClaims

class ClaimSpec extends BaseSpec {

  "Claim" - {
    "be serializable and deserializable when claim is unsubmitted" in {
      val claim             = TestClaims.testGetClaimsResponseUnsubmitted.claimsList.head
      val json              = Json.toJson(claim)
      val deserializedClaim = json.as[Claim]
      deserializedClaim shouldBe claim
    }

    "be serializable and deserializable when claim is submitted" in {
      val claim             = TestClaims.testGetClaimsResponseSubmitted.claimsList.head
      val json              = Json.toJson(claim)
      val deserializedClaim = json.as[Claim]
      deserializedClaim shouldBe claim
    }
  }

}

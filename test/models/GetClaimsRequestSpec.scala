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

class GetClaimsRequestSpec extends BaseSpec {

  "GetClaimsRequest" - {
    "be serializable and deserializable when claimSubmitted is false" in {
      val request           = GetClaimsRequest(claimSubmitted = false)
      val json              = Json.toJson(request)
      val deserializedClaim = json.as[GetClaimsRequest]
      deserializedClaim shouldBe request
    }

    "be serializable and deserializable when claimSubmitted is true" in {
      val claim             = GetClaimsRequest(claimSubmitted = true)
      val json              = Json.toJson(claim)
      val deserializedClaim = json.as[GetClaimsRequest]
      deserializedClaim shouldBe claim
    }
  }

}

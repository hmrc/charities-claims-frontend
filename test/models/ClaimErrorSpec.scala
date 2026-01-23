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

class ClaimErrorSpec extends BaseSpec {

  "ClaimError" - {
    "parse UPDATED_BY_ANOTHER_USER as UpdatedByAnotherUserException" in {
      val json   = """{"errorCode": "UPDATED_BY_ANOTHER_USER"}"""
      val result = Json.parse(json).as[ClaimError]
      result shouldBe a[UpdatedByAnotherUserException]
    }

    "parse MAX_CLAIMS_EXCEEDED as MaxClaimsExceededException" in {
      val json   = """{"errorCode": "MAX_CLAIMS_EXCEEDED"}"""
      val result = Json.parse(json).as[ClaimError]
      result shouldBe a[MaxClaimsExceededException]
    }

    "parse unknown error code as UnknownClaimError" in {
      val json   = """{"errorCode": "SOME_OTHER_ERROR"}"""
      val result = Json.parse(json).as[ClaimError]
      result shouldBe UnknownClaimError("SOME_OTHER_ERROR")
    }
  }
}

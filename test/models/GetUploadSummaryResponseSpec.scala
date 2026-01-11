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

import util.BaseSpec
import play.api.libs.json.Json

class GetUploadSummaryResponseSpec extends BaseSpec {

  "GetUploadSummaryResponse" - {
    "be serialised and deserialised correctly" in {
      val uploadSummary            = UploadSummary(
        reference = "test-ref-123",
        validationType = "GiftAid",
        fileStatus = "VALIDATED",
        uploadUrl = Some("https://example.com/upload")
      )
      val getUploadSummaryResponse = GetUploadSummaryResponse(uploads = Seq(uploadSummary))
      Json
        .parse(Json.prettyPrint(Json.toJson(getUploadSummaryResponse)))
        .as[GetUploadSummaryResponse] shouldBe getUploadSummaryResponse
    }
  }
}

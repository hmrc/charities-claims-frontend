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

class UploadSummarySpec extends BaseSpec {

  "UploadSummary" - {
    "be serialised and deserialised correctly with uploadUrl" in {
      val uploadSummary = UploadSummary(
        reference = FileUploadReference("test-ref-123"),
        validationType = ValidationType.GiftAid,
        fileStatus = FileStatus.VALIDATED,
        uploadUrl = Some("https://example.com/upload")
      )
      Json.parse(Json.prettyPrint(Json.toJson(uploadSummary))).as[UploadSummary] shouldBe uploadSummary
    }

    "be serialised and deserialised correctly without uploadUrl" in {
      val uploadSummary = UploadSummary(
        reference = FileUploadReference("test-ref-456"),
        validationType = ValidationType.OtherIncome,
        fileStatus = FileStatus.VALIDATING,
        uploadUrl = None
      )
      Json.parse(Json.prettyPrint(Json.toJson(uploadSummary))).as[UploadSummary] shouldBe uploadSummary
    }
  }
}

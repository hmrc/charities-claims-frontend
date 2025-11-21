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

import play.api.libs.json.Format
import play.api.libs.json.Json

final case class SectionOneAnswers(
  claimingGiftAid: Option[Boolean] = None,
  claimingTaxDeducted: Option[Boolean] = None,
  claimingUnderGasds: Option[Boolean] = None,
  claimReferenceNumber: Option[Boolean] = None
)

object SectionOneAnswers {
  given Format[SectionOneAnswers] = Json.format[SectionOneAnswers]
}

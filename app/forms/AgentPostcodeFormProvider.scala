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

package forms

import play.api.data.Form
import javax.inject.Inject

class AgentPostcodeFormProvider @Inject() extends Mappings {

  private val maxLength            = 8
  private val addressPostcodeRegex =
    "^\\s*((GIR 0AA)|((([a-zA-Z][0-9][0-9]?)|(([a-zA-Z][a-hj-yA-HJ-Y][0-9][0-9]?)|(([a-zA-Z][0-9][a-zA-Z])|([a-zA-Z][a-hj-yA-HJ-Y][0-9]?[a-zA-Z]))))\\s?[0-9][a-zA-Z]{2})\\s*)$"
  def apply(): Form[String]        = Form(
    "value" -> text("agentPostcode.error.required")
      .verifying(
        firstError(
          maxLength(maxLength, "agentPostcode.error.length"),
          regexp(addressPostcodeRegex, "agentPostcode.error.format")
        )
      )
  )
}

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

package forms

import forms.{Mappings, Validation}
import play.api.data.Form
import play.api.data.Forms.*

import javax.inject.Inject

class CharityNameFormProvider @Inject() extends Mappings {

  private val charityNameRegex     = "^[A-Za-z 0-9\\-']{1,160}$"
  private val maxCharityNameLength = 160

  def apply(): Form[String] =
    Form(
      "value" -> text("enterCharityName.error.required")
        .verifying(regexp(charityNameRegex, "enterCharityName.error.regex"))
        .verifying(maxLength(maxCharityNameLength, "enterCharityName.error.length"))
    )
}

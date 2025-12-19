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

import models.CorporateTrusteeDetails
import play.api.data.Form
import play.api.data.Forms.*

import javax.inject.Inject

class CorporateTrusteeDetailsFormProvider @Inject() extends Mappings {

  val name          = "name"
  val nameMaxLength = 160
  val nameRegex     = """^([a-zA-Z0-9 ]{1,160})$"""

  val phoneNumber          = "phone"
  val phoneNumberMaxLength = 35
  val phoneNumberRegex     = """^[0-9\\(\\)\\-\\s]{1,35}$"""

  val postCode          = "postcode"
  val postCodeMaxLength = 8
  val postCodeRegex     =
    """^\\s*((GIR 0AA)|((([a-zA-Z][0-9][0-9]?)|(([a-zA-Z][a-hj-yA-HJ-Y][0-9][0-9]?)|(([a-zA-Z][0-9][a-zA-Z])|([a-zA-Z][a-hj-yA-HJ-Y][0-9]?[a-zA-Z]))))\\s?[0-9][a-zA-Z]{2})\\s*)$"""

  def apply(
    isUKAddress: Boolean,
    nameRequired: String,
    nameInvalid: String,
    nameLength: String,
    nameHint: String,
    phoneNumberRequired: String,
    phoneNumberInvalid: String,
    phoneNumberLength: String,
    phoneNumberHint: String,
    postCodeRequired: String,
    postCodeInvalid: String,
    postCodeLength: String,
    postCodeHint: String
  ): Form[CorporateTrusteeDetails] =
    Form(
      mapping(
        name        -> text(nameRequired).verifying(
          firstError(
            regexp(nameRegex, nameInvalid),
            maxLength(nameMaxLength, nameLength)
          )
        ),
        phoneNumber -> text(phoneNumberRequired).verifying(
          firstError(
            regexp(phoneNumberRegex, phoneNumberInvalid),
            maxLength(phoneNumberMaxLength, phoneNumberLength)
          )
        ),
        postCode    -> (if (isUKAddress) {
                       text(postCodeRequired)
                         .verifying(
                           firstError(
                             regexp(postCodeRegex, postCodeInvalid),
                             maxLength(postCodeMaxLength, postCodeLength)
                           )
                         )
                         .transform[Option[String]](Some(_), _.getOrElse(""))
                     } else {
                       optional(
                         text(postCodeRequired)
                           .verifying(
                             firstError(
                               regexp(postCodeRegex, postCodeInvalid),
                               maxLength(postCodeMaxLength, postCodeLength)
                             )
                           )
                       )
                     })
      )(CorporateTrusteeDetails.apply)(x => Some(x.name, x.phoneNumber, x.postCode))
    )
}

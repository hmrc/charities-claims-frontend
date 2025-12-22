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

  val trusteeName   = "trusteeName"
  val nameMaxLength = 160
  val nameRegex     = """^([a-zA-Z0-9 ]{1,160})$"""

  val trusteePhoneNumber          = "trusteePhoneNumber"
  val trusteePhoneNumberMaxLength = 35
  val trusteePhoneNumberRegex     = """^[0-9\\(\\)\\-\\s]{1,35}$"""

  val addressPostCode          = "addressPostcode"
  val addressPostCodeMaxLength = 8
  val addressPostCodeRegex     =
    """^\\s*((GIR 0AA)|((([a-zA-Z][0-9][0-9]?)|(([a-zA-Z][a-hj-yA-HJ-Y][0-9][0-9]?)|(([a-zA-Z][0-9][a-zA-Z])|([a-zA-Z][a-hj-yA-HJ-Y][0-9]?[a-zA-Z]))))\\s?[0-9][a-zA-Z]{2})\\s*)$"""

  def apply(
    isUKAddress: Boolean,
    trusteeNameRequired: String,
    trusteeNameInvalid: String,
    trusteeNameLength: String,
    trusteePhoneNumberRequired: String,
    trusteePhoneNumberInvalid: String,
    trusteePhoneNumberLength: String,
    addressPostCodeRequired: String,
    addressPostCodeInvalid: String,
    addressPostCodeLength: String
  ): Form[CorporateTrusteeDetails] =
    Form(
      mapping(
        trusteeName        -> text(trusteeNameRequired).verifying(
          firstError(
            regexp(nameRegex, trusteeNameInvalid),
            maxLength(nameMaxLength, trusteeNameLength)
          )
        ),
        trusteePhoneNumber -> text(trusteePhoneNumberRequired).verifying(
          firstError(
            regexp(trusteePhoneNumberRegex, trusteePhoneNumberInvalid),
            maxLength(trusteePhoneNumberMaxLength, trusteePhoneNumberLength)
          )
        ),
        addressPostCode    -> (if (isUKAddress) {
                              text(addressPostCodeRequired)
                                .verifying(
                                  firstError(
                                    regexp(addressPostCodeRegex, addressPostCodeInvalid),
                                    maxLength(addressPostCodeMaxLength, addressPostCodeLength)
                                  )
                                )
                                .transform[Option[String]](Some(_), _.getOrElse(""))
                            } else {
                              optional(
                                text(addressPostCodeRequired)
                                  .verifying(
                                    firstError(
                                      regexp(addressPostCodeRegex, addressPostCodeInvalid),
                                      maxLength(addressPostCodeMaxLength, addressPostCodeLength)
                                    )
                                  )
                              )
                            })
      )(CorporateTrusteeDetails.apply)(x => Some(x.trusteeName, x.trusteePhoneNumber, x.addressPostCode))
    )
}

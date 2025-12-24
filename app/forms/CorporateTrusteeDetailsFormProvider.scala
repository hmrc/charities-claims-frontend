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

  val trusteeName           = "trusteeName"
  private val nameMaxLength = 160
  private val nameRegex     = "^([a-zA-Z0-9 ]{1,160})$"

  val trusteePhoneNumber                  = "trusteePhoneNumber"
  private val trusteePhoneNumberMaxLength = 35
  private val trusteePhoneNumberRegex     = "^[0-9\\(\\)\\-\\s]{1,35}$"

  val addressPostcode = "addressPostcode"

  def apply(
    isUKAddress: Boolean,
    trusteeNameRequired: String,
    trusteeNameLength: String,
    trusteeNameInvalid: String,
    trusteePhoneNumberRequired: String,
    trusteePhoneNumberLength: String,
    trusteePhoneNumberInvalid: String,
    addressPostcodeRequired: String,
    addressPostcodeLength: String,
    addressPostcodeInvalid: String
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
            maxLength(trusteePhoneNumberMaxLength, trusteePhoneNumberLength),
            regexp(trusteePhoneNumberRegex, trusteePhoneNumberInvalid)
          )
        ),
        addressPostcode    -> (if (isUKAddress) {
                              UKPostcodeMapping(addressPostcodeRequired, addressPostcodeLength, addressPostcodeInvalid)
                            } else {
                              optional(
                                text("")
                              )
                            })
      )(CorporateTrusteeDetails.apply)(x => Some(x.trusteeName, x.trusteePhoneNumber, x.addressPostcode))
    )
}

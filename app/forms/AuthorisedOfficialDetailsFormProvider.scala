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

import javax.inject.Inject
import models.AuthorisedOfficialDetails
import play.api.data.Form
import play.api.data.Forms._

class AuthorisedOfficialDetailsFormProvider @Inject() {

  private val titleRegex     = "^([a-zA-Z]{1,4})$"
  private val firstNameRegex = "^([a-zA-Z][a-zA-Z\\- ]*)$"
  private val lastNameRegex  = "^[A-Za-z0-9 ,\\.\\(\\)/&\\!\\-']+$"
  private val phoneRegex     = "^[0-9\\(\\)\\/\\-\\s]{1,35}$"
  private val postcodeRegex  =
    "^\\s*((GIR 0AA)|((([a-zA-Z][0-9][0-9]?)|(([a-zA-Z][a-zA-Z][0-9][0-9]?)|(([a-zA-Z][0-9][a-zA-Z])|([a-zA-Z][a-zA-Z][0-9][a-zA-Z]))))\\s?[0-9][a-zA-Z]{2}))\\s*$"

  def apply(isUkAddress: Boolean): Form[AuthorisedOfficialDetails] = Form(
    mapping(
      "title"       -> optional(
        text
          .verifying("authorisedOfficialDetails.title.error.format", _.matches(titleRegex))
          .verifying("authorisedOfficialDetails.title.error.length", _.length <= 4)
      ),
      "firstName"   -> text
        .verifying("authorisedOfficialDetails.firstName.error.required", _.nonEmpty)
        .verifying(
          "authorisedOfficialDetails.firstName.error.format",
          name => name.isEmpty || name.matches(firstNameRegex)
        )
        .verifying("authorisedOfficialDetails.firstName.error.length", _.length <= 35),
      "lastName"    -> text
        .verifying("authorisedOfficialDetails.lastName.error.required", _.nonEmpty)
        .verifying(
          "authorisedOfficialDetails.lastName.error.format",
          name => name.isEmpty || name.matches(lastNameRegex)
        )
        .verifying("authorisedOfficialDetails.lastName.error.length", _.length <= 35),
      "phoneNumber" -> text
        .verifying("authorisedOfficialDetails.phoneNumber.error.required", _.nonEmpty)
        .verifying(
          "authorisedOfficialDetails.phoneNumber.error.format",
          phone => phone.isEmpty || phone.matches(phoneRegex)
        )
        .verifying("authorisedOfficialDetails.phoneNumber.error.length", _.length <= 35),
      "postcode"    -> (if (isUkAddress) {
                       UKPostcodeMapping(
                         addressPostcodeRequired = "authorisedOfficialDetails.postcode.error.required",
                         addressPostcodeLength = "authorisedOfficialDetails.postcode.error.length",
                         addressPostcodeInvalid = "authorisedOfficialDetails.postcode.error.format"
                       )
                     } else {
                       optional(text)
                     })
    )(AuthorisedOfficialDetails.apply)(o => Some((o.title, o.firstName, o.lastName, o.phoneNumber, o.postcode)))
  )
}

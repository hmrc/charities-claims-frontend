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
import play.api.data.Forms.mapping

class AuthorisedOfficialDetailsFormProvider @Inject() extends Mappings {

  private val titleRegex     = "^( *[a-zA-Z]{1,4} *)$"
  private val firstNameRegex = "^( *[A-Za-z][A-Za-z'\\-]* *)$"
  private val lastNameRegex  = "^( *[A-Za-z][A-Za-z'\\-]* *)$"
  private val phoneRegex     = "^[0-9\\(\\)\\-\\s]{1,35}$"

  def apply(isUkAddress: Boolean): Form[AuthorisedOfficialDetails] = Form(
    mapping(
      "title"       -> optional(
        text("")
          .verifying(
            firstError(
              maxLength(4, "authorisedOfficialDetails.title.error.length"),
              regexp(titleRegex, "authorisedOfficialDetails.title.error.format")
            )
          )
      ),
      "firstName"   -> text("authorisedOfficialDetails.firstName.error.required")
        .verifying(
          firstError(
            maxLength(35, "authorisedOfficialDetails.firstName.error.length"),
            regexp(firstNameRegex, "authorisedOfficialDetails.firstName.error.format")
          )
        ),
      "lastName"    -> text("authorisedOfficialDetails.lastName.error.required")
        .verifying(
          firstError(
            maxLength(35, "authorisedOfficialDetails.lastName.error.length"),
            regexp(lastNameRegex, "authorisedOfficialDetails.lastName.error.format")
          )
        ),
      "phoneNumber" -> text("authorisedOfficialDetails.phoneNumber.error.required")
        .verifying(
          firstError(
            maxLength(35, "authorisedOfficialDetails.phoneNumber.error.length"),
            regexp(phoneRegex, "authorisedOfficialDetails.phoneNumber.error.format")
          )
        ),
      "postcode"    -> (if (isUkAddress) {
                       UKPostcodeMapping(
                         addressPostcodeRequired = "authorisedOfficialDetails.postcode.error.required",
                         addressPostcodeLength = "authorisedOfficialDetails.postcode.error.length",
                         addressPostcodeInvalid = "authorisedOfficialDetails.postcode.error.format"
                       )
                     } else {
                       optional(play.api.data.Forms.text)
                     })
    )(AuthorisedOfficialDetails.apply)(o => Some((o.title, o.firstName, o.lastName, o.phoneNumber, o.postcode)))
  )
}

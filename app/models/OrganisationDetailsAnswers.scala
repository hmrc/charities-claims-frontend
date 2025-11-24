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

final case class OrganisationDetailsAnswers(
  nameOfCharityRegulator: Option[String] = None,
  charityRegistrationNumber: Option[String] = None,
  areYouACorporateTrustee: Option[Boolean] = None,
  nameOfCorporateTrustee: Option[String] = None,
  corporateTrusteePostcode: Option[String] = None,
  corporateTrusteeDaytimeTelephoneNumber: Option[String] = None
)

object OrganisationDetailsAnswers {

  given Format[OrganisationDetailsAnswers] = Json.format[OrganisationDetailsAnswers]

  def from(organisationDetails: OrganisationDetails): OrganisationDetailsAnswers =
    OrganisationDetailsAnswers(
      nameOfCharityRegulator = Some(organisationDetails.nameOfCharityRegulator),
      charityRegistrationNumber = Some(organisationDetails.charityRegistrationNumber),
      areYouACorporateTrustee = Some(organisationDetails.areYouACorporateTrustee),
      nameOfCorporateTrustee = Some(organisationDetails.nameOfCorporateTrustee),
      corporateTrusteePostcode = Some(organisationDetails.corporateTrusteePostcode),
      corporateTrusteeDaytimeTelephoneNumber = Some(organisationDetails.corporateTrusteeDaytimeTelephoneNumber)
    )
}

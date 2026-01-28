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

import util.BaseSpec
import play.api.libs.json.Json

class OrganisationDetailsAnswersSpec extends BaseSpec {

  "OrganisationDetailsAnswers" - {
    "be serializable and deserializable" in {
      val organisationDetailsAnswers = OrganisationDetailsAnswers(
        nameOfCharityRegulator = Some(NameOfCharityRegulator.EnglandAndWales),
        reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.LowIncome),
        charityRegistrationNumber = Some("1234567890"),
        areYouACorporateTrustee = Some(true),
        doYouHaveCorporateTrusteeUKAddress = Some(true),
        doYouHaveAuthorisedOfficialTrusteeUKAddress = Some(false),
        nameOfCorporateTrustee = Some("John Doe"),
        corporateTrusteePostcode = Some("AA1 2BB"),
        corporateTrusteeDaytimeTelephoneNumber = Some("07912345678")
      )

      val json                                   = Json.toJson(organisationDetailsAnswers)
      val deserializedOrganisationDetailsAnswers = json.as[OrganisationDetailsAnswers]
      deserializedOrganisationDetailsAnswers shouldBe organisationDetailsAnswers
    }

    "be created from OrganisationDetails" in {
      val organisationDetails = OrganisationDetails(
        nameOfCharityRegulator = NameOfCharityRegulator.EnglandAndWales,
        reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.LowIncome),
        charityRegistrationNumber = Some("1234567890"),
        areYouACorporateTrustee = true,
        doYouHaveCorporateTrusteeUKAddress = Some(true),
        doYouHaveAuthorisedOfficialTrusteeUKAddress = Some(false),
        nameOfCorporateTrustee = Some("John Doe"),
        corporateTrusteePostcode = Some("AA1 2BB"),
        corporateTrusteeDaytimeTelephoneNumber = Some("07912345678")
      )

      val organisationDetailsAnswers = OrganisationDetailsAnswers.from(organisationDetails)

      organisationDetailsAnswers shouldBe OrganisationDetailsAnswers(
        nameOfCharityRegulator = Some(NameOfCharityRegulator.EnglandAndWales),
        reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.LowIncome),
        charityRegistrationNumber = Some("1234567890"),
        areYouACorporateTrustee = Some(true),
        doYouHaveCorporateTrusteeUKAddress = Some(true),
        doYouHaveAuthorisedOfficialTrusteeUKAddress = Some(false),
        nameOfCorporateTrustee = Some("John Doe"),
        corporateTrusteePostcode = Some("AA1 2BB"),
        corporateTrusteeDaytimeTelephoneNumber = Some("07912345678")
      )
    }

    "missingFields" - {
      "should return missing fields when OrganisationDetailsAnswers is empty" in {
        val organisationDetailsAnswers = OrganisationDetailsAnswers()

        val missingFields = organisationDetailsAnswers.missingFields

        missingFields should contain("nameOfCharityRegulator.missingDetails")
        missingFields should contain("corporateTrusteeClaim.missingDetails")
      }

      "should return charityRegulatorNumber is missing when regulator requires registration number" in {
        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.EnglandAndWales)
        )

        val missingFields = organisationDetailsAnswers.missingFields

        missingFields should contain("charityRegulatorNumber.missingDetails")
      }

      "should return corporateTrusteeDetails is missing when corporate trustee selected but details missing" in {
        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.None),
          reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.LowIncome),
          areYouACorporateTrustee = Some(true),
          doYouHaveCorporateTrusteeUKAddress = Some(true)
        )

        val missingFields = organisationDetailsAnswers.missingFields

        missingFields should contain("corporateTrusteeDetails.missingDetails")
      }

      "should return authorisedOfficialDetails is missing when not corporate trustee but details missing" in {
        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.None),
          reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.LowIncome),
          areYouACorporateTrustee = Some(false),
          doYouHaveAuthorisedOfficialTrusteeUKAddress = Some(true)
        )

        val missingFields = organisationDetailsAnswers.missingFields

        missingFields should contain("authorisedOfficialDetails.missingDetails")
      }

      "should return empty missing fields list when all required fields are complete" in {
        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.None),
          reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.LowIncome),
          areYouACorporateTrustee = Some(false),
          doYouHaveAuthorisedOfficialTrusteeUKAddress = Some(false),
          authorisedOfficialTrusteeFirstName = Some("John"),
          authorisedOfficialTrusteeLastName = Some("Doe"),
          authorisedOfficialTrusteeDaytimeTelephoneNumber = Some("07912345678")
        )

        val missingFields = organisationDetailsAnswers.missingFields

        missingFields shouldBe empty
      }
    }

    "hasOrganisationDetailsCompleteAnswers" - {
      "should return false when there are missing fields" in {
        val organisationDetailsAnswers = OrganisationDetailsAnswers()

        organisationDetailsAnswers.hasOrganisationDetailsCompleteAnswers shouldBe false
      }

      "should return true when there are no missing fields" in {
        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.None),
          reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.LowIncome),
          areYouACorporateTrustee = Some(false),
          doYouHaveAuthorisedOfficialTrusteeUKAddress = Some(false),
          authorisedOfficialTrusteeFirstName = Some("John"),
          authorisedOfficialTrusteeLastName = Some("Doe"),
          authorisedOfficialTrusteeDaytimeTelephoneNumber = Some("07912345678")
        )

        organisationDetailsAnswers.hasOrganisationDetailsCompleteAnswers shouldBe true
      }
    }
  }
}

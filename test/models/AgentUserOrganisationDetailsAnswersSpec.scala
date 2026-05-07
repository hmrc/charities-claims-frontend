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

package models

import util.BaseSpec
import play.api.libs.json.Json

class AgentUserOrganisationDetailsAnswersSpec extends BaseSpec {

  "AgentUserOrganisationDetailsAnswers" - {

    "be serializable and deserializable" in {
      val answers = AgentUserOrganisationDetailsAnswers(
        nameOfCharityRegulator = Some(NameOfCharityRegulator.EnglandAndWales),
        unregulatedReason = Some(ReasonNotRegisteredWithRegulator.LowIncome),
        charityRegistrationNumber = Some("123456"),
        whoShouldHmrcSendPaymentTo = Some(WhoShouldHmrcSendPaymentTo.AgentOrNominee),
        daytimeTelephoneNumber = Some("07123456789"),
        doYouHaveAgentUKAddress = Some(true),
        postcode = Some("AA1 1AA")
      )

      val json   = Json.toJson(answers)
      val result = json.as[AgentUserOrganisationDetailsAnswers]

      result shouldBe answers
    }

    "be created from AgentUserOrganisationDetails" in {
      val model = AgentUserOrganisationDetails(
        nameOfCharityRegulator = NameOfCharityRegulator.EnglandAndWales,
        unregulatedReason = Some(ReasonNotRegisteredWithRegulator.LowIncome),
        charityRegistrationNumber = Some("123456"),
        whoShouldHmrcSendPaymentTo = WhoShouldHmrcSendPaymentTo.AgentOrNominee,
        daytimeTelephoneNumber = "07123456789",
        doYouHaveAgentUKAddress = true,
        postcode = Some("AA1 1AA")
      )

      val result = AgentUserOrganisationDetailsAnswers.from(model)

      result shouldBe AgentUserOrganisationDetailsAnswers(
        nameOfCharityRegulator = Some(NameOfCharityRegulator.EnglandAndWales),
        unregulatedReason = Some(ReasonNotRegisteredWithRegulator.LowIncome),
        charityRegistrationNumber = Some("123456"),
        whoShouldHmrcSendPaymentTo = Some(WhoShouldHmrcSendPaymentTo.AgentOrNominee),
        daytimeTelephoneNumber = Some("07123456789"),
        doYouHaveAgentUKAddress = Some(true),
        postcode = Some("AA1 1AA")
      )
    }

    "missingFields" - {

      "not CASC charity" - {
        val isCASC = false

        "should return missing regulator when empty" in {
          val answers = AgentUserOrganisationDetailsAnswers()

          answers.missingFields(isCASC) should contain("nameOfCharityRegulator.missingDetails")
        }

        "should return missing unregulated reason when regulator is None" in {
          val answers = AgentUserOrganisationDetailsAnswers(
            nameOfCharityRegulator = Some(NameOfCharityRegulator.None)
          )

          answers.missingFields(isCASC) should contain("reasonNotRegisteredWithRegulator.missingDetails")
        }

        "should return missing registration number when regulator requires it" in {
          val answers = AgentUserOrganisationDetailsAnswers(
            nameOfCharityRegulator = Some(NameOfCharityRegulator.EnglandAndWales)
          )

          answers.missingFields(isCASC) should contain("charityRegulatorNumber.missingDetails")
        }

        "should return empty when complete" in {
          val answers = AgentUserOrganisationDetailsAnswers(
            nameOfCharityRegulator = Some(NameOfCharityRegulator.None),
            unregulatedReason = Some(ReasonNotRegisteredWithRegulator.LowIncome)
          )

          answers.missingFields(isCASC) shouldBe empty
        }
      }

      "CASC charity" - {
        val isCASC = true

        "should not require regulator" in {
          val answers = AgentUserOrganisationDetailsAnswers()

          answers.missingFields(isCASC) shouldBe empty
        }
      }
    }

    "hasAgentDetailsCompleteAnswers" - {

      "should return false when missing fields exist" in {
        val answers = AgentUserOrganisationDetailsAnswers()

        answers.hasAgentDetailsCompleteAnswers(isCASCCharityRef = false) shouldBe false
      }

      "should return true when no missing fields" in {
        val answers = AgentUserOrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.None),
          unregulatedReason = Some(ReasonNotRegisteredWithRegulator.LowIncome)
        )

        answers.hasAgentDetailsCompleteAnswers(isCASCCharityRef = false) shouldBe true
      }
    }

    "toAgentUserOrganisationDetails" - {

      "should successfully convert when all required fields exist" in {
        val answers = AgentUserOrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.None),
          unregulatedReason = Some(ReasonNotRegisteredWithRegulator.LowIncome),
          whoShouldHmrcSendPaymentTo = Some(WhoShouldHmrcSendPaymentTo.AgentOrNominee),
          daytimeTelephoneNumber = Some("07123456789"),
          doYouHaveAgentUKAddress = Some(true),
          postcode = Some("AA1 1AA")
        )

        val result =
          AgentUserOrganisationDetailsAnswers.toAgentUserOrganisationDetails(answers, isCASCCharity = false)

        result.isSuccess shouldBe true
      }

      "should fail when required field is missing" in {
        val answers = AgentUserOrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.None)
        )

        val result =
          AgentUserOrganisationDetailsAnswers.toAgentUserOrganisationDetails(answers, isCASCCharity = false)

        result.isFailure shouldBe true
      }

      "should ignore regulator when CASC charity" in {
        val answers = AgentUserOrganisationDetailsAnswers(
          whoShouldHmrcSendPaymentTo = Some(WhoShouldHmrcSendPaymentTo.AgentOrNominee),
          daytimeTelephoneNumber = Some("07123456789"),
          doYouHaveAgentUKAddress = Some(false)
        )

        val result =
          AgentUserOrganisationDetailsAnswers.toAgentUserOrganisationDetails(answers, isCASCCharity = true)

        result.isSuccess shouldBe true
      }

      "should set postcode only when UK address is true" in {
        val answers = AgentUserOrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.None),
          whoShouldHmrcSendPaymentTo = Some(WhoShouldHmrcSendPaymentTo.AgentOrNominee),
          daytimeTelephoneNumber = Some("07123456789"),
          doYouHaveAgentUKAddress = Some(false),
          postcode = Some("AA1 1AA")
        )

        val result =
          AgentUserOrganisationDetailsAnswers.toAgentUserOrganisationDetails(answers, isCASCCharity = false)

        result.get.postcode shouldBe None
      }
    }
  }
}

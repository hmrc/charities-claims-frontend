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

import play.api.libs.json.Json
import util.BaseSpec

class AgentUserOrganisationDetailsAnswersSpec extends BaseSpec {

  "AgentUserOrganisationDetailsAnswers" - {

    "be serializable and deserializable" in {

      val answers = AgentUserOrganisationDetailsAnswers(
        nameOfCharityRegulator = Some(NameOfCharityRegulator.EnglandAndWales),
        reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.LowIncome),
        charityRegistrationNumber = Some("123456"),
        whoShouldHmrcSendPaymentTo = Some(WhoShouldHmrcSendPaymentTo.AgentOrNominee),
        daytimeTelephoneNumber = Some("07123456789"),
        doYouHaveAgentUKAddress = Some(true),
        postcode = Some("AA1 1AA")
      )

      val json = Json.toJson(answers)

      json.as[AgentUserOrganisationDetailsAnswers] shouldBe answers
    }

    "be created from AgentUserOrganisationDetails" in {

      val model = AgentUserOrganisationDetails(
        nameOfCharityRegulator = NameOfCharityRegulator.EnglandAndWales,
        reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.LowIncome),
        charityRegistrationNumber = Some("123456"),
        whoShouldHmrcSendPaymentTo = WhoShouldHmrcSendPaymentTo.AgentOrNominee,
        daytimeTelephoneNumber = "07123456789",
        doYouHaveAgentUKAddress = true,
        postcode = Some("AA1 1AA")
      )

      AgentUserOrganisationDetailsAnswers.from(model) shouldBe
        AgentUserOrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.EnglandAndWales),
          reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.LowIncome),
          charityRegistrationNumber = Some("123456"),
          whoShouldHmrcSendPaymentTo = Some(WhoShouldHmrcSendPaymentTo.AgentOrNominee),
          daytimeTelephoneNumber = Some("07123456789"),
          doYouHaveAgentUKAddress = Some(true),
          postcode = Some("AA1 1AA")
        )
    }

    "missingFields" - {

      "when not CASC charity" - {

        val isCASC = false

        "should return missing regulator when empty" in {

          val answers = AgentUserOrganisationDetailsAnswers()

          answers.missingFields(isCASC) should contain(
            "nameOfCharityRegulator.agent.missingDetails"
          )
        }

        "should return missing reason when regulator is none" in {

          val answers = AgentUserOrganisationDetailsAnswers(
            nameOfCharityRegulator = Some(NameOfCharityRegulator.None)
          )

          answers.missingFields(isCASC) should contain(
            "reasonNotRegisteredWithRegulator.agent.missingDetails"
          )
        }

        "should return missing regulator number when regulator requires it" in {

          val answers = AgentUserOrganisationDetailsAnswers(
            nameOfCharityRegulator = Some(NameOfCharityRegulator.EnglandAndWales)
          )

          answers.missingFields(isCASC) should contain(
            "charityRegulatorNumber.agent.missingDetails"
          )
        }

        "should return missing postcode when UK address is true" in {

          val answers = AgentUserOrganisationDetailsAnswers(
            doYouHaveAgentUKAddress = Some(true)
          )

          answers.missingFields(isCASC) should contain(
            "agentPostcode.missingDetails"
          )
        }

        "should not require postcode when UK address is false" in {

          val answers = AgentUserOrganisationDetailsAnswers(
            doYouHaveAgentUKAddress = Some(false)
          )

          answers.missingFields(isCASC) should not contain
            "agentPostcode.missingDetails"
        }
      }

      "when CASC charity" - {

        val isCASC = true

        "should not require regulator details" in {

          val answers = AgentUserOrganisationDetailsAnswers()

          answers.missingFields(isCASC) should not contain
            "nameOfCharityRegulator.agent.missingDetails"
        }
      }
    }

    "hasAgentDetailsCompleteAnswers" - {

      "should return false when fields missing" in {

        val answers = AgentUserOrganisationDetailsAnswers()

        answers.hasAgentDetailsCompleteAnswers(false) shouldBe false
      }

      "should return true when all required fields present" in {

        val answers = AgentUserOrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.None),
          reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.LowIncome),
          whoShouldHmrcSendPaymentTo = Some(WhoShouldHmrcSendPaymentTo.AgentOrNominee),
          daytimeTelephoneNumber = Some("07123456789"),
          doYouHaveAgentUKAddress = Some(false)
        )

        answers.hasAgentDetailsCompleteAnswers(false) shouldBe true
      }
    }

    "toAgentUserOrganisationDetails" - {

      "should successfully convert valid answers" in {

        val answers = AgentUserOrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.None),
          reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.LowIncome),
          whoShouldHmrcSendPaymentTo = Some(WhoShouldHmrcSendPaymentTo.AgentOrNominee),
          daytimeTelephoneNumber = Some("07123456789"),
          doYouHaveAgentUKAddress = Some(true),
          postcode = Some("AA1 1AA")
        )

        val result =
          AgentUserOrganisationDetailsAnswers.toAgentUserOrganisationDetails(
            answers,
            isCASCCharity = false
          )

        result.isSuccess shouldBe true
      }

      "should fail when required field missing" in {

        val answers = AgentUserOrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.None)
        )

        val result =
          AgentUserOrganisationDetailsAnswers.toAgentUserOrganisationDetails(
            answers,
            isCASCCharity = false
          )

        result.isFailure shouldBe true
      }

      "should ignore regulator for CASC charity" in {

        val answers = AgentUserOrganisationDetailsAnswers(
          whoShouldHmrcSendPaymentTo = Some(WhoShouldHmrcSendPaymentTo.AgentOrNominee),
          daytimeTelephoneNumber = Some("07123456789"),
          doYouHaveAgentUKAddress = Some(false)
        )

        val result =
          AgentUserOrganisationDetailsAnswers.toAgentUserOrganisationDetails(
            answers,
            isCASCCharity = true
          )

        result.isSuccess shouldBe true
      }

      "should remove postcode when UK address is false" in {

        val answers = AgentUserOrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.None),
          whoShouldHmrcSendPaymentTo = Some(WhoShouldHmrcSendPaymentTo.AgentOrNominee),
          daytimeTelephoneNumber = Some("07123456789"),
          doYouHaveAgentUKAddress = Some(false),
          postcode = Some("AA1 1AA")
        )

        val result =
          AgentUserOrganisationDetailsAnswers.toAgentUserOrganisationDetails(
            answers,
            isCASCCharity = false
          )

        result.get.postcode shouldBe None
      }

      "should remove registration number when regulator is none" in {

        val answers = AgentUserOrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.None),
          charityRegistrationNumber = Some("123456"),
          whoShouldHmrcSendPaymentTo = Some(WhoShouldHmrcSendPaymentTo.AgentOrNominee),
          daytimeTelephoneNumber = Some("07123456789"),
          doYouHaveAgentUKAddress = Some(false)
        )

        val result =
          AgentUserOrganisationDetailsAnswers.toAgentUserOrganisationDetails(
            answers,
            isCASCCharity = false
          )

        result.get.charityRegistrationNumber shouldBe None
      }
    }
  }
}

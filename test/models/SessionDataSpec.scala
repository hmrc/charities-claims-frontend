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

class SessionDataSpec extends BaseSpec {

  "SessionData" - {
    "be serializable and deserializable" in {
      val sessionData             = SessionData(
        repaymentClaimDetailsAnswers = RepaymentClaimDetailsAnswers(
          claimingGiftAid = Some(true),
          claimingTaxDeducted = Some(true),
          claimingUnderGiftAidSmallDonationsScheme = Some(true),
          claimingReferenceNumber = Some(true),
          claimReferenceNumber = Some("1234567890")
        ),
        organisationDetailsAnswers = Some(
          OrganisationDetailsAnswers(
            nameOfCharityRegulator = Some(NameOfCharityRegulator.EnglandAndWales),
            reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.LowIncome),
            charityRegistrationNumber = Some("1137948"),
            areYouACorporateTrustee = Some(true),
            nameOfCorporateTrustee = Some("Joe Bloggs"),
            corporateTrusteePostcode = Some("AB12 3YZ"),
            corporateTrusteeDaytimeTelephoneNumber = Some("071234567890")
          )
        )
      )
      val json                    = Json.toJson(sessionData)
      val deserializedSessionData = json.as[SessionData]
      deserializedSessionData shouldBe sessionData
    }
  }

}

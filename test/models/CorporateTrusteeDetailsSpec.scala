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

class CorporateTrusteeDetailsSpec extends BaseSpec {

  "CorporateTrusteeDetails" - {
    "be serializable and deserializable with Postcode" in {
      val corporateTrusteeDetails = CorporateTrusteeDetails(
        nameOfCorporateTrustee = "Trustee ABC",
        corporateTrusteeDaytimeTelephoneNumber = "07123456786",
        corporateTrusteePostcode = Some("SW1A 1AA")
      )

      val json                                = Json.toJson(corporateTrusteeDetails)
      val deserializedCorporateTrusteeDetails = json.as[CorporateTrusteeDetails]
      deserializedCorporateTrusteeDetails shouldBe corporateTrusteeDetails

    }

    "be serializable and deserializable with without Postcode" in {
      val corporateTrusteeDetails = CorporateTrusteeDetails(
        nameOfCorporateTrustee = "Trustee ABC",
        corporateTrusteeDaytimeTelephoneNumber = "07123456786",
        corporateTrusteePostcode = None
      )

      val json                                = Json.toJson(corporateTrusteeDetails)
      val deserializedCorporateTrusteeDetails = json.as[CorporateTrusteeDetails]
      deserializedCorporateTrusteeDetails shouldBe corporateTrusteeDetails

    }
  }
}

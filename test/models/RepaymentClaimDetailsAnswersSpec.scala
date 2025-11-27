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

class RepaymentClaimDetailsAnswersSpec extends BaseSpec {

  "RepaymentClaimDetailsAnswers" - {
    "be serializable and deserializable" in {
      val repaymentClaimDetailsAnswers = RepaymentClaimDetailsAnswers(
        claimingGiftAid = Some(true),
        claimingTaxDeducted = Some(true),
        claimingUnderGasds = Some(true),
        claimingReferenceNumber = Some(true),
        claimReferenceNumber = Some("1234567890")
      )

      val json                                     = Json.toJson(repaymentClaimDetailsAnswers)
      val deserializedRepaymentClaimDetailsAnswers = json.as[RepaymentClaimDetailsAnswers]
      deserializedRepaymentClaimDetailsAnswers shouldBe repaymentClaimDetailsAnswers
    }

    "be created from RepaymentClaimDetails when claimReferenceNumber is defined" in {
      val repaymentClaimDetails        = RepaymentClaimDetails(
        claimingGiftAid = true,
        claimingTaxDeducted = false,
        claimingUnderGasds = true,
        claimReferenceNumber = Some("foobar")
      )
      val repaymentClaimDetailsAnswers = RepaymentClaimDetailsAnswers.from(repaymentClaimDetails)
      repaymentClaimDetailsAnswers shouldBe RepaymentClaimDetailsAnswers(
        claimingGiftAid = Some(true),
        claimingTaxDeducted = Some(false),
        claimingUnderGasds = Some(true),
        claimingReferenceNumber = Some(true),
        claimReferenceNumber = Some("foobar")
      )
    }

    "be created from RepaymentClaimDetails when claimReferenceNumber is not defined" in {
      val repaymentClaimDetails        = RepaymentClaimDetails(
        claimingGiftAid = false,
        claimingTaxDeducted = false,
        claimingUnderGasds = true,
        claimReferenceNumber = None
      )
      val repaymentClaimDetailsAnswers = RepaymentClaimDetailsAnswers.from(repaymentClaimDetails)

      repaymentClaimDetailsAnswers shouldBe RepaymentClaimDetailsAnswers(
        claimingGiftAid = Some(false),
        claimingTaxDeducted = Some(false),
        claimingUnderGasds = Some(true),
        claimingReferenceNumber = Some(false),
        claimReferenceNumber = None
      )
    }
  }

}

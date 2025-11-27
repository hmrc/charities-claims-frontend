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

package util

import models.GetClaimsResponse
import play.api.libs.json.Json
import models.Claim
import models.ClaimData
import models.RepaymentClaimDetails

object TestClaims {

  lazy val testGetClaimsResponseUnsubmittedJsonString: String =
    TestResources.readTestResource("/test-get-claims-response-unsubmitted.json")

  lazy val testGetClaimsResponseUnsubmitted: GetClaimsResponse =
    Json.parse(testGetClaimsResponseUnsubmittedJsonString).as[GetClaimsResponse]

  lazy val testGetClaimsResponseSubmittedJsonString: String =
    TestResources.readTestResource("/test-get-claims-response-submitted.json")

  lazy val testGetClaimsResponseSubmitted: GetClaimsResponse =
    Json.parse(testGetClaimsResponseSubmittedJsonString).as[GetClaimsResponse]

  def testClaimWithRepaymentClaimDetailsOnly(
    claimId: String = "123",
    claimingTaxDeducted: Boolean = true,
    claimingGiftAid: Boolean = true,
    claimingUnderGasds: Boolean = false,
    claimReferenceNumber: Option[String] = Some("1234567890")
  ): Claim =
    Claim(
      claimId = claimId,
      userId = TestUsers.organisation1,
      claimSubmitted = false,
      creationTimestamp = "2025-11-10T13:45:56.016Z",
      claimData = ClaimData(
        repaymentClaimDetails = RepaymentClaimDetails(
          claimingTaxDeducted = claimingTaxDeducted,
          claimingGiftAid = claimingGiftAid,
          claimingUnderGasds = claimingUnderGasds,
          claimReferenceNumber = claimReferenceNumber
        )
      )
    )

}

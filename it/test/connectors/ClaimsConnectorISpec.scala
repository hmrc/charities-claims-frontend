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

package connectors

import models.*
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.{NOT_FOUND, OK}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{ComponentSpecHelper, TestDataUtils, WiremockMethods}

class ClaimsConnectorISpec
  extends ComponentSpecHelper
    with WiremockMethods with TestDataUtils {

  private val connector = app.injector.instanceOf[ClaimsConnector]
  given HeaderCarrier = HeaderCarrier()

  "retrieveUnsubmittedClaims" should {

    "return claims when backend returns 200" in {

      val response =
        GetClaimsResponse(
          claimsCount = 1,
          claimsList = List(
            ClaimInfo(claimId, Some("1234567890"), Some("Test charity"))
          )
        )

      when(GET, "/charities-claims/claims\\?claimSubmitted=false")
        .thenReturn(OK, response)

      val result = connector.retrieveUnsubmittedClaims.futureValue

      result.claimsCount shouldBe 1
    }
  }

  "saveClaim" should {

    "send POST request and return response" in {

      val repaymentDetails =
        RepaymentClaimDetails(
          claimingGiftAid = true,
          claimingTaxDeducted = true,
          claimingUnderGiftAidSmallDonationsScheme = false
        )

      val response =
        SaveClaimResponse(
          claimId = claimId,
          lastUpdatedReference = "test-ref"
        )

      when(POST, "/charities-claims/claims")
        .thenReturn(OK, response)

      val result = connector.saveClaim(repaymentDetails).futureValue

      result.claimId shouldBe claimId
      result.lastUpdatedReference shouldBe "test-ref"
    }
  }

  "getClaim" should {

    "return claim when backend returns 200" in {
      when(GET, "/charities-claims/claims/claim-1")
        .thenReturn(OK, claim)

      val result = connector.getClaim("claim-1").futureValue

      result.value.claimId shouldBe claimId
    }

    "return None when backend returns 404" in {

      when(GET, "/charities-claims/claims/claim-404")
        .thenReturn(NOT_FOUND)

      val result = connector.getClaim("claim-404").futureValue

      result shouldBe None
    }
  }

  "updateClaim" should {

    "send PUT request and return response" in {
      val repaymentDetails = RepaymentClaimDetails(
        claimingGiftAid = true,
        claimingTaxDeducted = true,
        claimingUnderGiftAidSmallDonationsScheme = false,
        claimReferenceNumber = Some("1234567890")
      )
      val request =
        UpdateClaimRequest(
          lastUpdatedReference = "1234567890",
          repaymentClaimDetails = repaymentDetails
        )

      val response =
        UpdateClaimResponse(success = true, lastUpdatedReference = "1234567890")

      when(PUT, "/charities-claims/claims/claim-1", request)
        .thenReturn(OK, response)

      val result = connector.updateClaim("claim-1", request).futureValue

      result.success shouldBe true
    }
  }

  "deleteClaim" should {

    "return true when backend returns success" in {

      val response =
        DeleteClaimResponse(success = true)

      when(DELETE, "/charities-claims/claims/claim-1")
        .thenReturn(OK, response)

      val result = connector.deleteClaim("claim-1").futureValue

      result shouldBe true
    }
  }

  "submitClaim" should {

    "return true when backend returns success" in {

      val request =
        SubmitClaimRequest(
          claimId = "claim-1",
          lastUpdatedReference = "1234567890",
          declarationLanguage = "en"
        )

      val response =
        SubmitClaimResponse(success = true)

      when(POST, "/charities-claims/chris", request)
        .thenReturn(OK, response)

      val result = connector.submitClaim("claim-1","1234567890","en").futureValue

      result shouldBe true
    }
  }
}

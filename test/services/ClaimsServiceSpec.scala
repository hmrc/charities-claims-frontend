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

package services

import connectors.{ClaimsConnector, MissingRequiredFieldsException}
import models.requests.DataRequest
import models.{
  GetClaimsResponse,
  OrganisationDetailsAnswers,
  RepaymentClaimDetails,
  RepaymentClaimDetailsAnswers,
  SessionData
}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import util.{BaseSpec, TestClaims}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ClaimsServiceSpec extends BaseSpec {

  given HeaderCarrier = HeaderCarrier()

  "ClaimsService.save" - {

    "create a new claim when there is no unsubmittedClaimId in the session" in {
      val mockSaveService = mock[SaveService]
      val mockConnector   = mock[ClaimsConnector]

      val service = new ClaimsServiceImpl(mockSaveService, mockConnector)

      val repaymentAnswers = RepaymentClaimDetailsAnswers(
        claimingGiftAid = Some(true),
        claimingTaxDeducted = Some(true),
        claimingUnderGasds = Some(false),
        claimReferenceNumber = Some("1234567890")
      )

      val initialSessionData = SessionData(
        unsubmittedClaimId = None,
        repaymentClaimDetailsAnswers = repaymentAnswers
      )

      given DataRequest[?] = DataRequest(FakeRequest(), initialSessionData)

      (mockConnector
        .saveClaim(_: RepaymentClaimDetails)(using _: HeaderCarrier))
        .expects(*, *)
        .returning(Future.successful("generated-claim-id"))

      val expectedSessionData = initialSessionData.copy(unsubmittedClaimId = Some("generated-claim-id"))

      (mockSaveService
        .save(_: SessionData)(using _: DataRequest[?], _: HeaderCarrier))
        .expects(expectedSessionData, *, *)
        .returning(Future.successful(()))

      await(service.save)
    }

    "update an existing claim when there is an unsubmittedClaimId in the session" in {
      val mockSaveService = mock[SaveService]
      val mockConnector   = mock[ClaimsConnector]

      val service = new ClaimsServiceImpl(mockSaveService, mockConnector)

      val repaymentAnswers = RepaymentClaimDetailsAnswers(
        claimingGiftAid = Some(true),
        claimingTaxDeducted = Some(true),
        claimingUnderGasds = Some(false),
        claimReferenceNumber = Some("1234567890")
      )

      val existingClaimId    = "existing-claim-id"
      val initialSessionData = SessionData(
        unsubmittedClaimId = Some(existingClaimId),
        repaymentClaimDetailsAnswers = repaymentAnswers
      )

      given DataRequest[?] = DataRequest(FakeRequest(), initialSessionData)

      val existingClaim =
        TestClaims.testClaimWithRepaymentClaimDetailsOnly(claimId = existingClaimId)

      (mockConnector
        .retrieveUnsubmittedClaims(using _: HeaderCarrier))
        .expects(*)
        .returning(
          Future.successful(
            GetClaimsResponse(
              claimsCount = 1,
              claimsList = List(existingClaim)
            )
          )
        )

      (mockConnector
        .updateClaim(_: String, _: RepaymentClaimDetails)(using _: HeaderCarrier))
        .expects(existingClaimId, *, *)
        .returning(Future.successful(()))

      await(service.save)
    }

    "fail with MissingRequiredFieldsException when required fields are not present" in {
      val mockSaveService = mock[SaveService]
      val mockConnector   = mock[ClaimsConnector]

      val service = new ClaimsServiceImpl(mockSaveService, mockConnector)

      val incompleteAnswers = RepaymentClaimDetailsAnswers(
        claimingGiftAid = Some(true),
        claimingTaxDeducted = None, // missing
        claimingUnderGasds = Some(false)
      )

      val initialSessionData = SessionData(
        unsubmittedClaimId = None,
        repaymentClaimDetailsAnswers = incompleteAnswers
      )

      given DataRequest[?] = DataRequest(FakeRequest(), initialSessionData)

      an[MissingRequiredFieldsException] should be thrownBy await(service.save)
    }
  }
}

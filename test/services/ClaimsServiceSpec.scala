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

import play.api.test.FakeRequest
import play.api.test.Helpers.*
import util.BaseSpec
import connectors.ClaimsConnector
import uk.gov.hmrc.http.HeaderCarrier
import models.*
import models.requests.DataRequest

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ClaimsServiceSpec extends BaseSpec {

  given HeaderCarrier = HeaderCarrier()

  "ClaimsService.save" - {

    "create a new claim when there is no unsubmittedClaimId in the session" in {
      val mockSaveService = mock[SaveService]
      val mockConnector   = mock[ClaimsConnector]

      val service = new ClaimsServiceImpl(mockSaveService, mockConnector)

      val repaymentAnswers = RepaymentClaimDetailsAnswersOld(
        claimingGiftAid = Some(true),
        claimingTaxDeducted = Some(true),
        claimingUnderGiftAidSmallDonationsScheme = Some(false),
        claimReferenceNumber = Some("1234567890")
      )

      val initialSessionData = SessionData(
        charitiesReference = testCharitiesReference,
        unsubmittedClaimId = None,
        lastUpdatedReference = None,
        repaymentClaimDetailsAnswersOld = repaymentAnswers
      )

      given DataRequest[?] = DataRequest(FakeRequest(), initialSessionData)

      (mockConnector
        .saveClaim(_: RepaymentClaimDetails)(using _: HeaderCarrier))
        .expects(*, *)
        .returning(
          Future.successful(SaveClaimResponse(claimId = "generated-claim-id", lastUpdatedReference = "0123456789"))
        )

      val expectedSessionData = initialSessionData.copy(
        unsubmittedClaimId = Some("generated-claim-id"),
        lastUpdatedReference = Some("0123456789")
      )

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

      val repaymentAnswers = RepaymentClaimDetailsAnswersOld(
        claimingGiftAid = Some(true),
        claimingTaxDeducted = Some(true),
        claimingUnderGiftAidSmallDonationsScheme = Some(false),
        claimReferenceNumber = Some("1234567890")
      )

      val existingClaimId    = "existing-claim-id"
      val initialSessionData = SessionData(
        charitiesReference = testCharitiesReference,
        unsubmittedClaimId = Some(existingClaimId),
        lastUpdatedReference = Some("1234567890"),
        repaymentClaimDetailsAnswersOld = repaymentAnswers
      )

      given DataRequest[?] = DataRequest(FakeRequest(), initialSessionData)

      (mockConnector
        .updateClaim(_: String, _: UpdateClaimRequest)(using _: HeaderCarrier))
        .expects(existingClaimId, *, *)
        .returning(Future.successful(UpdateClaimResponse(success = true, lastUpdatedReference = "1234567891")))

      (mockSaveService
        .save(_: SessionData)(using _: DataRequest[?], _: HeaderCarrier))
        .expects(initialSessionData.copy(lastUpdatedReference = Some("1234567891")), *, *)
        .returning(Future.successful(()))

      await(service.save)
    }

    "fail with MissingRequiredFieldsException when required fields are not present" in {
      val mockSaveService = mock[SaveService]
      val mockConnector   = mock[ClaimsConnector]

      val service = new ClaimsServiceImpl(mockSaveService, mockConnector)

      val incompleteAnswers = RepaymentClaimDetailsAnswersOld(
        claimingGiftAid = Some(true),
        claimingTaxDeducted = None, // missing
        claimingUnderGiftAidSmallDonationsScheme = Some(false)
      )

      val initialSessionData = SessionData(
        charitiesReference = testCharitiesReference,
        unsubmittedClaimId = None,
        lastUpdatedReference = None,
        repaymentClaimDetailsAnswersOld = incompleteAnswers
      )

      given DataRequest[?] = DataRequest(FakeRequest(), initialSessionData)

      an[MissingRequiredFieldsException] should be thrownBy await(service.save)
    }
  }
}

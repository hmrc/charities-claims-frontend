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

import play.api.test.Helpers.*
import util.BaseSpec
import connectors.ClaimsConnector
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import uk.gov.hmrc.http.HeaderCarrier
import models.*

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import repositories.SessionCache

class ClaimsServiceSpec extends BaseSpec {

  given HeaderCarrier = HeaderCarrier()

  "ClaimsService.save" - {

    "create a new claim when there is no unsubmittedClaimId in the session" in {
      val mockSessionCache = mock[SessionCache]
      val mockConnector    = mock[ClaimsConnector]

      val service = new ClaimsServiceImpl(mockSessionCache, mockConnector)

      val repaymentAnswers = RepaymentClaimDetailsAnswers(
        claimingGiftAid = Some(true),
        claimingTaxDeducted = Some(true),
        claimingUnderGiftAidSmallDonationsScheme = Some(false),
        claimReferenceNumber = Some("1234567890")
      )

      val initialSessionData = SessionData(
        charitiesReference = testCharitiesReference,
        unsubmittedClaimId = None,
        lastUpdatedReference = None,
        repaymentClaimDetailsAnswers = Some(repaymentAnswers)
      )

      when(mockSessionCache.get()(using any())).thenReturn(Future.successful(Some(initialSessionData)))

      when(mockConnector.saveClaim(any())(using any())).thenReturn(
        Future.successful(SaveClaimResponse(claimId = "generated-claim-id", lastUpdatedReference = "0123456789"))
      )

      val expectedSessionData = initialSessionData.copy(
        unsubmittedClaimId = Some("generated-claim-id"),
        lastUpdatedReference = Some("0123456789")
      )

      when(mockSessionCache.store(eqTo(expectedSessionData))(using any())).thenReturn(Future.successful(()))

      await(service.save)
    }

    "update an existing claim when there is an unsubmittedClaimId in the session" in {
      val mockSessionCache = mock[SessionCache]
      val mockConnector    = mock[ClaimsConnector]

      val service = new ClaimsServiceImpl(mockSessionCache, mockConnector)

      val repaymentAnswers = RepaymentClaimDetailsAnswers(
        claimingGiftAid = Some(true),
        claimingTaxDeducted = Some(true),
        claimingUnderGiftAidSmallDonationsScheme = Some(false),
        claimReferenceNumber = Some("1234567890")
      )

      val existingClaimId = "existing-claim-id"

      val initialSessionData = SessionData(
        charitiesReference = testCharitiesReference,
        unsubmittedClaimId = Some(existingClaimId),
        lastUpdatedReference = Some("1234567890"),
        repaymentClaimDetailsAnswers = Some(repaymentAnswers)
      )

      when(mockSessionCache.get()(using any())).thenReturn(Future.successful(Some(initialSessionData)))

      when(mockConnector.updateClaim(eqTo(existingClaimId), any())(using any()))
        .thenReturn(Future.successful(UpdateClaimResponse(success = true, lastUpdatedReference = "1234567891")))

      when(
        mockSessionCache.store(eqTo(initialSessionData.copy(lastUpdatedReference = Some("1234567891"))))(using any())
      ).thenReturn(Future.successful(()))

      await(service.save)
    }

    "fail with OrganisationClaimAlreadyInProgressException when the claims limit is exceeded for an organisation user" in {
      val mockSessionCache = mock[SessionCache]
      val mockConnector    = mock[ClaimsConnector]

      val service = new ClaimsServiceImpl(mockSessionCache, mockConnector)

      val repaymentAnswers = RepaymentClaimDetailsAnswers(
        claimingGiftAid = Some(true),
        claimingTaxDeducted = Some(true),
        claimingUnderGiftAidSmallDonationsScheme = Some(false),
        claimReferenceNumber = Some("1234567890")
      )

      val initialSessionData = SessionData(
        charitiesReference = testCharitiesReference,
        unsubmittedClaimId = None,
        lastUpdatedReference = None,
        repaymentClaimDetailsAnswers = Some(repaymentAnswers),
        isAgent = false
      )

      when(mockSessionCache.get()(using any())).thenReturn(Future.successful(Some(initialSessionData)))

      when(mockConnector.saveClaim(any())(using any()))
        .thenReturn(Future.failed(UnsubmittedClaimsLimitExceededException()))

      an[OrganisationClaimAlreadyInProgressException] should be thrownBy await(service.save)
    }

    "fail with UnsubmittedClaimsLimitExceededException when the claims limit is exceeded for an agent user" in {
      val mockSessionCache = mock[SessionCache]
      val mockConnector    = mock[ClaimsConnector]

      val service = new ClaimsServiceImpl(mockSessionCache, mockConnector)

      val repaymentAnswers = RepaymentClaimDetailsAnswers(
        claimingGiftAid = Some(true),
        claimingTaxDeducted = Some(true),
        claimingUnderGiftAidSmallDonationsScheme = Some(false),
        claimReferenceNumber = Some("1234567890")
      )

      val initialSessionData = SessionData(
        charitiesReference = testCharitiesReference,
        unsubmittedClaimId = None,
        lastUpdatedReference = None,
        repaymentClaimDetailsAnswers = Some(repaymentAnswers),
        isAgent = true
      )

      when(mockSessionCache.get()(using any())).thenReturn(Future.successful(Some(initialSessionData)))

      when(mockConnector.saveClaim(any())(using any()))
        .thenReturn(Future.failed(UnsubmittedClaimsLimitExceededException()))

      an[UnsubmittedClaimsLimitExceededException] should be thrownBy await(service.save)
    }

    "fail with MissingRequiredFieldsException when required fields are not present" in {
      val mockSessionCache = mock[SessionCache]
      val mockConnector    = mock[ClaimsConnector]

      val service = new ClaimsServiceImpl(mockSessionCache, mockConnector)

      val incompleteAnswers = RepaymentClaimDetailsAnswers(
        claimingGiftAid = Some(true),
        claimingTaxDeducted = None, // missing
        claimingUnderGiftAidSmallDonationsScheme = Some(false)
      )

      val initialSessionData = SessionData(
        charitiesReference = testCharitiesReference,
        unsubmittedClaimId = None,
        lastUpdatedReference = None,
        repaymentClaimDetailsAnswers = Some(incompleteAnswers)
      )

      when(mockSessionCache.get()(using any())).thenReturn(Future.successful(Some(initialSessionData)))

      an[MissingRequiredFieldsException] should be thrownBy await(service.save)
    }
  }
}

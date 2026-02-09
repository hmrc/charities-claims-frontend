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

package services

import util.BaseSpec
import connectors.UnregulatedDonationsConnector
import models.{OrganisationDetailsAnswers, ReasonNotRegisteredWithRegulator, SessionData}
import models.requests.DataRequest
import uk.gov.hmrc.http.HeaderCarrier
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnregulatedDonationsServiceSpec extends BaseSpec {

  val mockConnector: UnregulatedDonationsConnector = mock[UnregulatedDonationsConnector]

  val service = new UnregulatedDonationsServiceImpl(
    unregulatedDonationsConnector = mockConnector,
    appConfig = testFrontendAppConfig
  )

  given HeaderCarrier = HeaderCarrier()

  // checkUnregulatedLimit tests

  "UnregulatedDonationsService" - {

    "checkUnregulatedLimit" - {

      "should return None when reasonNotRegisteredWithRegulator is None" in {
        // user has a regulator - no limit check required
        val sessionData = SessionData.empty(testCharitiesReference)
        given request: DataRequest[AnyContent] =
          DataRequest(FakeRequest(), sessionData)

        val result = await(service.checkUnregulatedLimit)

        result shouldEqual None
      }

      "should return None when reasonNotRegisteredWithRegulator is Exempt" in {
        val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
          ReasonNotRegisteredWithRegulator.Exempt
        )(using SessionData.empty(testCharitiesReference))

        given request: DataRequest[AnyContent] =
          DataRequest(FakeRequest(), sessionData)

        val result = await(service.checkUnregulatedLimit)

        result shouldEqual None
      }

      "should return None when reasonNotRegisteredWithRegulator is Waiting" in {
        val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
          ReasonNotRegisteredWithRegulator.Waiting
        )(using SessionData.empty(testCharitiesReference))

        given request: DataRequest[AnyContent] =
          DataRequest(FakeRequest(), sessionData)

        val result = await(service.checkUnregulatedLimit)

        result shouldEqual None
      }

      // LowIncome Limit tests

      "when charity reason is LowIncome" - {

        "should return None when total donations are under the limit" in {
          (mockConnector
            .getTotalUnregulatedDonations(_: String)(using _: HeaderCarrier))
            .expects(testCharitiesReference, *)
            .returning(Future.successful(Some(BigDecimal(1000))))
            .once()

          val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
            ReasonNotRegisteredWithRegulator.LowIncome
          )(using SessionData.empty(testCharitiesReference))

          given request: DataRequest[AnyContent] =
            DataRequest(FakeRequest(), sessionData)

          val result = await(service.checkUnregulatedLimit)

          // TODO: update this test when getCurrentClaimDonationsTotal is ready
          // currently returns None because placeholder returns 0, so 0 + 1000 < 5000
          result shouldEqual None
        }

        "should return Some(UnregulatedLimitExceeded) when total donations exceed the limit" in {
          // existing donations of 5001 already exceed the limit
          (mockConnector
            .getTotalUnregulatedDonations(_: String)(using _: HeaderCarrier))
            .expects(testCharitiesReference, *)
            .returning(Future.successful(Some(BigDecimal(5001))))
            .once()

          val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
            ReasonNotRegisteredWithRegulator.LowIncome
          )(using SessionData.empty(testCharitiesReference))

          given request: DataRequest[AnyContent] =
            DataRequest(FakeRequest(), sessionData)

          val result = await(service.checkUnregulatedLimit)

          result shouldEqual Some(UnregulatedLimitExceeded(5000, "5,000"))
        }

        "should handle when connector returns None (no existing unregulated donations)" in {
          (mockConnector
            .getTotalUnregulatedDonations(_: String)(using _: HeaderCarrier))
            .expects(testCharitiesReference, *)
            .returning(Future.successful(None))
            .once()

          val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
            ReasonNotRegisteredWithRegulator.LowIncome
          )(using SessionData.empty(testCharitiesReference))

          given request: DataRequest[AnyContent] =
            DataRequest(FakeRequest(), sessionData)

          val result = await(service.checkUnregulatedLimit)

          // TODO: Update when getCurrentClaimDonationsTotal is ready
          // currently 0 + 0 = 0, which is under 5000
          result shouldEqual None
        }
      }

      // Excepted Limit tests

      "when charity reason is Excepted" - {

        "should return None when total donations are under the limit" in {
          (mockConnector
            .getTotalUnregulatedDonations(_: String)(using _: HeaderCarrier))
            .expects(testCharitiesReference, *)
            .returning(Future.successful(Some(BigDecimal(50000))))
            .once()

          val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
            ReasonNotRegisteredWithRegulator.Excepted
          )(using SessionData.empty(testCharitiesReference))

          given request: DataRequest[AnyContent] =
            DataRequest(FakeRequest(), sessionData)

          val result = await(service.checkUnregulatedLimit)

          // TODO: update when getCurrentClaimDonationsTotal is ready
          // currently 0 + 50000 < 100000
          result shouldEqual None
        }

        "should return Some(UnregulatedLimitExceeded) when total donations exceed the limit" in {

          // existing donations of 100001 already exceed the limit
          (mockConnector
            .getTotalUnregulatedDonations(_: String)(using _: HeaderCarrier))
            .expects(testCharitiesReference, *)
            .returning(Future.successful(Some(BigDecimal(100001))))
            .once()

          val sessionData = OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(
            ReasonNotRegisteredWithRegulator.Excepted
          )(using SessionData.empty(testCharitiesReference))

          given request: DataRequest[AnyContent] =
            DataRequest(FakeRequest(), sessionData)

          val result = await(service.checkUnregulatedLimit)

          result shouldEqual Some(UnregulatedLimitExceeded(100000, "100,000"))
        }
      }
    }

    // TODO: recordUnregulatedDonation tests to be added later when F11 is implemented

  }
}

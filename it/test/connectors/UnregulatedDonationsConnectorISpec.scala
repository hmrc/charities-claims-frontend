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

import models.GetTotalUnregulatedDonationsResponse
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.{NOT_FOUND, OK}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{ComponentSpecHelper, TestDataUtils, WiremockMethods}

class UnregulatedDonationsConnectorISpec
  extends ComponentSpecHelper
    with WiremockMethods with TestDataUtils {

  private val connector: UnregulatedDonationsConnector = app.injector.instanceOf[UnregulatedDonationsConnector]

  given HeaderCarrier = HeaderCarrier()

  private val charityReference = "1234567890"

  "getTotalUnregulatedDonations" should {

    "return total when backend returns 200" in {

      val response =
        GetTotalUnregulatedDonationsResponse(
          unregulatedDonationsTotal = BigDecimal(250.75)
        )

      when(GET, s"/charities-claims/charities/$charityReference/unregulated-donations")
        .thenReturn(OK, response)

      val result = connector.getTotalUnregulatedDonations(charityReference).futureValue

      result shouldBe Some(BigDecimal(250.75))
    }

    "return None when backend returns 404" in {

      when(GET, s"/charities-claims/charities/$charityReference/unregulated-donations")
        .thenReturn(NOT_FOUND)

      val result = connector.getTotalUnregulatedDonations(charityReference).futureValue

      result shouldBe None
    }
  }
}

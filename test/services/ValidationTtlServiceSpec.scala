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

import connectors.ClaimsValidationConnector
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import util.BaseSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ValidationTtlServiceSpec extends BaseSpec with BeforeAndAfterEach {

  private val mockConnector = mock[ClaimsValidationConnector]
  private val service       = new ValidationTtlServiceImpl(mockConnector)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val claimId = "test-claim-id"

  "ValidationTtlService" - {

    "touchValidationTtl" - {

      "should call connector successfully" in {
        (mockConnector
          .touchTtl(_: String)(using _: HeaderCarrier))
          .expects(claimId, *)
          .returning(Future.unit)
          .once()

        val result = service.touchValidationTtl(claimId)

        await(result) shouldBe ()
      }

      "should recover when connector fails" in {
        (mockConnector
          .touchTtl(_: String)(using _: HeaderCarrier))
          .expects(claimId, *)
          .returning(Future.failed(new RuntimeException("exception")))
          .once()

        val result = service.touchValidationTtl(claimId)

        noException should be thrownBy await(result)
      }
    }
  }
}

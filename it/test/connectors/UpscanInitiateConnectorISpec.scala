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
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.OK
import uk.gov.hmrc.http.HeaderCarrier
import utils.{ComponentSpecHelper, TestDataUtils, WiremockMethods}

class UpscanInitiateConnectorISpec extends ComponentSpecHelper with WiremockMethods with TestDataUtils {

  private val connector = app.injector.instanceOf[UpscanInitiateConnector]

  given HeaderCarrier = HeaderCarrier()

  "initiate" should {

    "return UpscanInitiateResponse when backend returns 200" in {
      val request =
        UpscanInitiateRequest(
          successRedirect = "http://localhost/success",
          errorRedirect = "http://localhost/error",
          minimumFileSize = Some(1),
          maximumFileSize = Some(1000)
        )

      val response =
        UpscanInitiateResponse(
          reference = UpscanReference("test-reference"),
          uploadRequest = UploadRequest(
            href = "https://upload-url",
            fields = Map("key" -> "value")
          )
        )

      when(POST, "/upscan/v2/initiate")
        .thenReturn(OK, response)

      val result = connector.initiate(claimId, request).futureValue

      result.reference          shouldBe "test-reference"
      result.uploadRequest.href shouldBe "https://upload-url"
    }
  }
}

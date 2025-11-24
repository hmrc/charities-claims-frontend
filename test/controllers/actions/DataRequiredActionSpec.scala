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

package controllers.actions

import models.SessionData
import models.requests.OptionalDataRequest
import play.api.mvc.Results.*
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers}
import util.BaseSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class DataRequiredActionSpec extends BaseSpec {

  val request       = FakeRequest("GET", "/test")
  given SessionData = SessionData(None)

  "DataRequiredAction" - {
    "refines OptionalDataRequest into a DataRequest when session data exists" in {
      val action = new DefaultDataRequiredAction()

      val dataRequest =
        OptionalDataRequest(request, sessionData = Some(SessionData.SectionOne.setClaimingTaxDeducted(true)))
      val result      = action.invokeBlock(dataRequest, _ => Future.successful(Ok))
      status(result) shouldBe OK
    }

    "refines OptionalDataRequest into a DataRequest when session data object exists but is empty" in {
      val action = new DefaultDataRequiredAction()

      val dataRequest = OptionalDataRequest(request, sessionData = Some(SessionData(None)))
      val result      = action.invokeBlock(dataRequest, _ => Future.successful(Ok))
      status(result) shouldBe OK
    }

    "returns error page when Session data does not exist" in {
      val action = new DefaultDataRequiredAction()

      val dataRequest = OptionalDataRequest(request, sessionData = None)
      val result      = action.invokeBlock(dataRequest, _ => Future.successful(Ok))
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }
}

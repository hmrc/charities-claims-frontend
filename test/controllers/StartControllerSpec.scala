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

package controllers

import play.api.test.FakeRequest
import play.api.mvc.AnyContentAsEmpty
import controllers.ControllerSpec
import models.Mode.*
import play.api.Application

class StartControllerSpec extends ControllerSpec {

  "StartController" - {

    "start" - {
      "should redirect to the claiming gift aid page" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.StartController.start.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            repaymentclaimdetailsold.routes.ClaimingGiftAidController.onPageLoad(NormalMode).url
          )
        }
      }
    }

    "keepAlive" - {
      "should return OK" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.StartController.keepAlive.url)

          val result = route(application, request).value

          status(result) shouldEqual OK
        }
      }
    }

    "timedOut" - {
      "should redirect to the start page" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.StartController.timedOut.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.StartController.start.url)
        }
      }
    }
  }
}

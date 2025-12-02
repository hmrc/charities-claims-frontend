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
import play.api.Application
import handlers.ErrorHandler
import play.api.test.Helpers.*

class PageNotFoundControllerSpec extends ControllerSpec {

  "PageNotFoundController" - {

    "onPageLoad" - {
      "should render the page correctly" in {
        given application: Application = applicationBuilder().build()
        val errorHandler               = application.injector.instanceOf[ErrorHandler]

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.PageNotFoundController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual NOT_FOUND
          contentAsString(result) shouldEqual await(errorHandler.notFoundTemplate(using request)).body
        }
      }
    }
  }
}

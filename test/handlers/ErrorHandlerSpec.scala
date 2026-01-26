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

package handlers

import controllers.ControllerSpec
import play.api.Application
import play.api.test.FakeRequest
import play.api.mvc.AnyContentAsEmpty
import handlers.ErrorHandler
import models.{MaxClaimsExceededException, UpdatedByAnotherUserException}

class ErrorHandlerSpec extends ControllerSpec {

  "ErrorHandler" - {
    "should redirect if the exception is UpdatedByAnotherUserException" in {

      given application: Application = applicationBuilder().build()
      val errorHandler               = application.injector.instanceOf[ErrorHandler]

      running(application) {
        given request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/foo")

        val exception = UpdatedByAnotherUserException()
        val result    = errorHandler.resolveError(request, exception)

        status(result) shouldEqual SEE_OTHER
        redirectLocation(
          result
        ).value shouldEqual controllers.organisationDetails.routes.CannotViewOrManageClaimController.onPageLoad.url
      }

    }

    "should redirect if the exception is MaxClaimsExceededException" in {

      given application: Application = applicationBuilder().build()
      val errorHandler               = application.injector.instanceOf[ErrorHandler]

      running(application) {
        given request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/foo")

        val exception = MaxClaimsExceededException()
        val result    = errorHandler.resolveError(request, exception)

        status(result) shouldEqual SEE_OTHER
        redirectLocation(result).value shouldEqual controllers.routes.MaxClaimsExceededController.onPageLoad.url
      }

    }

    "should return InternalServerError if another exception is thrown" in {
      given application: Application = applicationBuilder().build()
      val errorHandler               = application.injector.instanceOf[ErrorHandler]

      running(application) {
        given request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/foo")
        val exception                                      = Exception("test")
        val result                                         = errorHandler.resolveError(request, exception)

        status(result) shouldEqual INTERNAL_SERVER_ERROR
      }
    }
  }
}

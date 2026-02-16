/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.Application
import play.api.mvc.{AnyContentAsFormUrlEncoded, MessagesControllerComponents}
import play.api.test.FakeRequest

class BaseControllerSpec extends ControllerSpec {

  "BaseController" - {

    "needsUpdateConfirmation" - {
      "should return true when in CheckMode, previous answer was Yes, and new answer is No" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          val controller = new BaseController {
            override val controllerComponents: MessagesControllerComponents =
              application.injector.instanceOf[MessagesControllerComponents]
          }

          controller.needsUpdateConfirmation(models.Mode.CheckMode, Some(true), false) shouldBe true
        }
      }

      "should return false when in NormalMode" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          val controller = new BaseController {
            override val controllerComponents: MessagesControllerComponents =
              application.injector.instanceOf[MessagesControllerComponents]
          }

          controller.needsUpdateConfirmation(models.Mode.NormalMode, Some(true), false) shouldBe false
        }
      }

      "should return false when answer is unchanged or not Yes to No" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          val controller = new BaseController {
            override val controllerComponents: MessagesControllerComponents =
              application.injector.instanceOf[MessagesControllerComponents]
          }

          controller.needsUpdateConfirmation(models.Mode.CheckMode, Some(false), false) shouldBe false
          controller.needsUpdateConfirmation(models.Mode.CheckMode, Some(true), true)   shouldBe false
          controller.needsUpdateConfirmation(models.Mode.CheckMode, None, false)        shouldBe false
        }
      }
    }

    "isConfirmingUpdate" - {
      "should return true when confirmingUpdate field is present with value 'true'" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, "/test")
              .withFormUrlEncodedBody("confirmingUpdate" -> "true")

          val controller = new BaseController {
            override val controllerComponents: MessagesControllerComponents =
              application.injector.instanceOf[MessagesControllerComponents]
          }

          controller.isConfirmingUpdate shouldBe true
        }
      }

      "should return false when confirmingUpdate is not present or has wrong value" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          val controller = new BaseController {
            override val controllerComponents: MessagesControllerComponents =
              application.injector.instanceOf[MessagesControllerComponents]
          }

          given request1: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, "/test")
              .withFormUrlEncodedBody("value" -> "true")
          controller.isConfirmingUpdate(using request1) shouldBe false

          given request2: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, "/test")
              .withFormUrlEncodedBody("confirmingUpdate" -> "false")
          controller.isConfirmingUpdate(using request2) shouldBe false

          given request3: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, "/test")
              .withFormUrlEncodedBody("other" -> "field")
          controller.isConfirmingUpdate(using request3) shouldBe false
        }
      }
    }
  }
}

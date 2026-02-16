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

    // OLD FLASH-BASED WARNING TESTS - COMMENTED OUT (replaced by WRN3 flow)
    // "isWarning" - {
    //   "should return true when warning flash is set to true" in {
    //     given application: Application = applicationBuilder().build()

    //     running(application) {
    //       given request: FakeRequest[AnyContentAsEmpty.type] =
    //         FakeRequest(GET, "/test")
    //           .withFlash("warning" -> "true")

    //       val controller = new BaseController {
    //         val controllerComponents: MessagesControllerComponents =
    //           application.injector.instanceOf[MessagesControllerComponents]
    //       }

    //       controller.isWarning shouldBe true
    //     }
    //   }

    //   "should return false when warning flash is not set" in {
    //     given application: Application = applicationBuilder().build()

    //     running(application) {
    //       given request: FakeRequest[AnyContentAsEmpty.type] =
    //         FakeRequest(GET, "/test")

    //       val controller = new BaseController {
    //         val controllerComponents = application.injector.instanceOf[MessagesControllerComponents]
    //       }

    //       controller.isWarning shouldBe false
    //     }
    //   }
    // }

    // "warningAnswerBoolean" - {
    //   "should return true when warningAnswer flash is true" in {
    //     given application: Application = applicationBuilder().build()

    //     running(application) {
    //       given request: FakeRequest[AnyContentAsEmpty.type] =
    //         FakeRequest(GET, "/test")
    //           .withFlash("warningAnswer" -> "true")

    //       val controller = new BaseController {
    //         val controllerComponents: MessagesControllerComponents =
    //           application.injector.instanceOf[MessagesControllerComponents]
    //       }

    //       controller.warningAnswerBoolean shouldBe Some(true)
    //     }
    //   }
    // }

    // "hadNoWarningShown" - {
    //   "should return true when warningShown is not present" in {
    //     given application: Application = applicationBuilder().build()

    //     running(application) {
    //       given request: FakeRequest[AnyContentAsFormUrlEncoded] =
    //         FakeRequest(POST, "/test")
    //           .withFormUrlEncodedBody("someField" -> "value")

    //       val controller = new BaseController {
    //         val controllerComponents: MessagesControllerComponents =
    //           application.injector.instanceOf[MessagesControllerComponents]
    //       }

    //       controller.hadNoWarningShown shouldBe true
    //     }
    //   }

    //   "should return false when warningShown is true in body" in {
    //     given application: Application = applicationBuilder().build()

    //     running(application) {
    //       given request: FakeRequest[AnyContentAsFormUrlEncoded] =
    //         FakeRequest(POST, "/test")
    //           .withFormUrlEncodedBody("warningShown" -> "true")

    //       val controller = new BaseController {
    //         val controllerComponents: MessagesControllerComponents =
    //           application.injector.instanceOf[MessagesControllerComponents]
    //       }

    //       controller.hadNoWarningShown shouldBe false
    //     }
    //   }
    // }

    // "warningWasShown" - {
    //   "should return false when warningShown is not present" in {
    //     given application: Application = applicationBuilder().build()

    //     running(application) {
    //       given request: FakeRequest[AnyContentAsFormUrlEncoded] =
    //         FakeRequest(POST, "/test")
    //           .withFormUrlEncodedBody("someField" -> "value")

    //       val controller = new BaseController {
    //         val controllerComponents: MessagesControllerComponents =
    //           application.injector.instanceOf[MessagesControllerComponents]
    //       }

    //       controller.warningWasShown shouldBe false
    //     }
    //   }

    //   "should return true when warningShown is true in body" in {
    //     given application: Application = applicationBuilder().build()

    //     running(application) {
    //       given request: FakeRequest[AnyContentAsFormUrlEncoded] =
    //         FakeRequest(POST, "/test")
    //           .withFormUrlEncodedBody("warningShown" -> "true")

    //       val controller = new BaseController {
    //         val controllerComponents: MessagesControllerComponents =
    //           application.injector.instanceOf[MessagesControllerComponents]
    //       }

    //       controller.warningWasShown shouldBe true
    //     }
    //   }
    // }

    // "warningAnswerString" - {
    //   "should return None when no warning answer exists" in {
    //     given application: Application = applicationBuilder().build()

    //     running(application) {
    //       given request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

    //       val controller = new BaseController {
    //         override val controllerComponents: MessagesControllerComponents =
    //           application.injector.instanceOf[MessagesControllerComponents]
    //       }

    //       controller.warningAnswerString(using request) shouldBe None
    //     }
    //   }

    //   "should return Some(string) when warning answer exists" in {
    //     given application: Application = applicationBuilder().build()

    //     running(application) {
    //       given request: FakeRequest[AnyContentAsEmpty.type] =
    //         FakeRequest().withFlash("warningAnswer" -> "someAnswer")

    //       val controller = new BaseController {
    //         override val controllerComponents: MessagesControllerComponents =
    //           application.injector.instanceOf[MessagesControllerComponents]
    //       }

    //       controller.warningAnswerString(using request) shouldBe Some("someAnswer")
    //     }
    //   }
    // }
    // END OLD FLASH-BASED WARNING TESTS

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

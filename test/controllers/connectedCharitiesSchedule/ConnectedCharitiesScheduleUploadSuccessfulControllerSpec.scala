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

package controllers.connectedCharitiesSchedule

import controllers.ControllerSpec
import controllers.connectedCharitiesSchedule.routes
import models.{FileUploadReference, RepaymentClaimDetailsAnswers}
import play.api.Application
import play.api.i18n.{Lang, MessagesApi}
import play.api.test.FakeRequest
import util.TestScheduleData
import views.html.ConnectedCharitiesScheduleUploadSuccessfulView

class ConnectedCharitiesScheduleUploadSuccessfulControllerSpec extends ControllerSpec {

  "ConnectedCharitiesScheduleUploadSuccessfulControllerSpec" - {
    "onPageLoad" - {

      "should render page successfully" in {
        val sessionData = RepaymentClaimDetailsAnswers
          .setClaimingConnectedCharities(true)
          .copy(
            connectedCharitiesScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference")),
            connectedCharitiesScheduleData = Some(TestScheduleData.exampleConnectedCharitiesScheduleData)
          )

        val application = applicationBuilder(sessionData = sessionData).build()
        val view        = application.injector.instanceOf[ConnectedCharitiesScheduleUploadSuccessfulView]
        val messages    = application.injector.instanceOf[MessagesApi].preferred(Seq(Lang("en")))

        running(application) {
          val request = FakeRequest(GET, routes.ConnectedCharitiesScheduleUploadSuccessfulController.onPageLoad.url)

          val result = route(application, request).value
          status(result) shouldEqual OK
          contentAsString(result) shouldBe view()(using request, messages).body
        }
      }

    }

    "onSubmit" - {
      "should redirect to the next page" in {
        val sessionData              = RepaymentClaimDetailsAnswers
          .setClaimingConnectedCharities(true)
          .copy(
            connectedCharitiesScheduleFileUploadReference = Some(FileUploadReference("test-file-upload-reference"))
          )
        val application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          val request = FakeRequest(POST, routes.ConnectedCharitiesScheduleUploadSuccessfulController.onSubmit.url)

          val result = route(application, request).value
          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
        }
      }
    }
  }
}

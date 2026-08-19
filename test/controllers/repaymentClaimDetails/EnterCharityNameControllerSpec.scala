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

package controllers.repaymentClaimDetails

import controllers.ControllerSpec
import forms.CharityNameFormProvider
import models.Mode.*
import play.api.Application
import play.api.data.Form
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup
import views.html.EnterCharityNameView

class EnterCharityNameControllerSpec extends ControllerSpec {

  val formProvider                      = new CharityNameFormProvider
  val form: Form[String]                = formProvider()
  val formWithOver160Chars: String      =
    "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum"
  val normalModeOnPageLoadRoute: String = routes.EnterCharityNameController.onPageLoad(NormalMode).url
  val normalModeOnSubmitRoute: String   = routes.EnterCharityNameController.onSubmit(NormalMode).url

  "EnterCharityNameController" - {
    "should return OK and the correct view for a GET in NormalMode - agent user" in {
      given application: Application = applicationBuilder(affinityGroup = AffinityGroup.Agent).build()

      running(application) {
        given request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.EnterCharityNameController.onPageLoad(NormalMode).url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[EnterCharityNameView]

        status(result) shouldEqual OK
        contentAsString(result) shouldEqual view(form, NormalMode).body
      }
    }

    "should redirect to claims list for a GET in NormalMode - organisation user" in {
      given application: Application = applicationBuilder(affinityGroup = AffinityGroup.Organisation).build()

      running(application) {
        given request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.EnterCharityNameController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) shouldEqual SEE_OTHER
        redirectLocation(result) shouldEqual Some(
          controllers.routes.ClaimsTaskListController.onPageLoad.url
        )
      }
    }

    "should redirect to claims list for a GET in Checkmode - organisation user" in {
      given application: Application = applicationBuilder(affinityGroup = AffinityGroup.Organisation).build()

      running(application) {
        given request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.EnterCharityNameController.onPageLoad(CheckMode).url)

        val result = route(application, request).value

        status(result) shouldEqual SEE_OTHER
        redirectLocation(result) shouldEqual Some(
          controllers.routes.ClaimsTaskListController.onPageLoad.url
        )
      }
    }

    "should redirect to claims list for a POST in NormalMode - organisation user" in {
      given application: Application = applicationBuilder(affinityGroup = AffinityGroup.Organisation).build()

      running(application) {
        given request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(POST, routes.EnterCharityNameController.onSubmit(NormalMode).url)

        val result = route(application, request).value

        status(result) shouldEqual SEE_OTHER
        redirectLocation(result) shouldEqual Some(
          controllers.routes.ClaimsTaskListController.onPageLoad.url
        )
      }
    }

    "should redirect to claims list for a POST in Checkmode - organisation user" in {
      given application: Application = applicationBuilder(affinityGroup = AffinityGroup.Organisation).build()

      running(application) {
        given request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(POST, routes.EnterCharityNameController.onSubmit(CheckMode).url)

        val result = route(application, request).value

        status(result) shouldEqual SEE_OTHER
        redirectLocation(result) shouldEqual Some(
          controllers.routes.ClaimsTaskListController.onPageLoad.url
        )
      }
    }
  }
}

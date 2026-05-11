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

import models.Mode.*
import controllers.ControllerSpec
import views.html.EnterCharityNameView
import play.api.Application
import forms.CharityNameFormProvider
import models.RepaymentClaimDetailsAnswers
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup

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
//
//    "should NOT return OK and the correct view for a GET in NormalMode - organisation user" in {
//      given application: Application = applicationBuilder().build()
//
//      running(application) {
//        given request: FakeRequest[AnyContentAsEmpty.type] =
//          FakeRequest(GET, routes.EnterCharityNameController.onPageLoad(NormalMode).url)
//
//        val result = route(application, request).value
//        val view   = application.injector.instanceOf[EnterCharityNameView]
//
//        status(result) shouldEqual SEE_OTHER
//        redirectLocation(result) shouldEqual Some(
//          controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
//        )
//      }
//    }
//
//    "must populate the view correctly on a GET when the question has previously been answered" in {
//
//      val sessionData = RepaymentClaimDetailsAnswers.setNameOfCharity("Test Name")
//
//      given application: Application =
//        applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()
//
//      running(application) {
//        val request = FakeRequest(GET, normalModeOnPageLoadRoute)
//
//        val result = route(application, request).value
//
//        val view = application.injector.instanceOf[EnterCharityNameView]
//
//        status(result) shouldEqual OK
//        contentAsString(result) shouldEqual view(form.fill("Test Name"), NormalMode)(using
//          request,
//          messages(application)
//        ).toString
//      }
//    }
//
//    "must return a Bad Request and errors when invalid data with invalid characters are submitted" in {
//
//      given application: Application = applicationBuilder(affinityGroup = AffinityGroup.Agent).build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, normalModeOnSubmitRoute)
//            .withFormUrlEncodedBody(("value", "Invalid Te$t"))
//
//        val boundForm = form.bind(Map("value" -> "Invalid Te$t"))
//        val result    = route(application, request).value
//
//        val view = application.injector.instanceOf[EnterCharityNameView]
//
//        status(result) shouldEqual BAD_REQUEST
//        contentAsString(result) shouldEqual view(boundForm, NormalMode)(using request, messages(application)).toString
//      }
//    }
//
//    "must return a Bad Request and errors when no data is submitted" in {
//
//      given application: Application = applicationBuilder(affinityGroup = AffinityGroup.Agent).build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, normalModeOnSubmitRoute)
//            .withFormUrlEncodedBody(("value", ""))
//
//        val boundForm = form.bind(Map("value" -> ""))
//        val result    = route(application, request).value
//
//        val view = application.injector.instanceOf[EnterCharityNameView]
//
//        status(result) shouldEqual BAD_REQUEST
//        contentAsString(result) shouldEqual view(boundForm, NormalMode)(using request, messages(application)).toString
//      }
//    }
//
//    "must return a Bad Request and errors when data exceeding the 160 char limit is submitted" in {
//
//      given application: Application = applicationBuilder(affinityGroup = AffinityGroup.Agent).build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, normalModeOnSubmitRoute)
//            .withFormUrlEncodedBody(("value", formWithOver160Chars))
//
//        val boundForm = form.bind(Map("value" -> formWithOver160Chars))
//        val result    = route(application, request).value
//
//        val view = application.injector.instanceOf[EnterCharityNameView]
//
//        status(result) shouldEqual BAD_REQUEST
//        contentAsString(result) shouldEqual view(boundForm, NormalMode)(using request, messages(application)).toString
//      }
//    }
  }
}

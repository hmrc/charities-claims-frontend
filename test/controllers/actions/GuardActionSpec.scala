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

package controllers.actions

import models.SessionData
import models.requests.DataRequest
import play.api.mvc.{ActionBuilder, AnyContent, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import util.BaseSpec

import scala.concurrent.{ExecutionContext, Future}

class GuardActionSpec extends BaseSpec {

  private given ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  private val guardAction = new GuardAction()

  private val sessionData = SessionData.empty(testCharitiesReference)

  private val fakeActionBuilder: ActionBuilder[DataRequest, AnyContent] =
    new ActionBuilder[DataRequest, AnyContent] {
      override def parser                     = play.api.mvc.BodyParsers.utils.ignore(play.api.mvc.AnyContentAsEmpty)
      override protected def executionContext = ec
      override def invokeBlock[A](
        request: play.api.mvc.Request[A],
        block: DataRequest[A] => Future[play.api.mvc.Result]
      ) =
        block(DataRequest(request, sessionData))
    }

  "GuardAction" - {
    "should allow request through when predicate returns true" in {
      val action = fakeActionBuilder.andThen(guardAction(_ ?=> true)).apply((_: DataRequest[AnyContent]) => Results.Ok)
      val result = action(FakeRequest())

      status(result) shouldEqual OK
    }

    "should redirect to ClaimsTaskListController when predicate returns false" in {
      val action =
        fakeActionBuilder.andThen(guardAction(_ ?=> false)).apply((_: DataRequest[AnyContent]) => Results.Ok)
      val result = action(FakeRequest())

      status(result) shouldEqual SEE_OTHER
      redirectLocation(result) shouldEqual Some(controllers.routes.ClaimsTaskListController.onPageLoad.url)
    }
  }
}

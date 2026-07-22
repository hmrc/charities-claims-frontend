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

package controllers

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import com.google.inject.Inject
import controllers.BaseController
import models.SessionData
import views.html.{Warning15UnsubmittedClaimExistsView, Warning16UnsubmittedClaimExistsForCharityView}
import controllers.actions.Actions

import scala.concurrent.Future

class CannotProgressThisClaimController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  agentView: Warning16UnsubmittedClaimExistsForCharityView,
  organisationView: Warning15UnsubmittedClaimExistsView
) extends BaseController {

  def onPageLoad: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.isClaimNotSubmitted)
      .async { implicit request =>
        if request.isAgent then Future.successful(Ok(agentView()))
        else Future.successful(Ok(organisationView()))
      }
}

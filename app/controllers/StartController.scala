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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import com.google.inject.Inject
import controllers.actions.AuthorisedAction
import play.api.i18n.I18nSupport
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.TimedOutView
import viewmodels.TimeoutMode
import javax.inject.Singleton

@Singleton
class StartController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  authorisedAction: AuthorisedAction,
  timedOutView: TimedOutView
) extends FrontendBaseController
    with I18nSupport {

  val start: Action[AnyContent] =
    Action(_ => Redirect(routes.ClaimsTaskListController.onPageLoad))

  val keepAlive: Action[AnyContent] =
    authorisedAction(_ => Ok)

  val timedOutNotSaved: Action[AnyContent] =
    Action(implicit request => Ok(timedOutView(TimeoutMode.NotSaved)))

  val timedOutSaved: Action[AnyContent] =
    Action(implicit request => Ok(timedOutView(TimeoutMode.Saved)))

  val timedOut: Action[AnyContent] =
    Action(implicit request => Ok(timedOutView(TimeoutMode.Normal)))

}

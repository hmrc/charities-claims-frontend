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

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.Actions
import play.api.mvc.*
import views.html.ConnectedCharitiesScheduleUploadSuccessfulView

import scala.concurrent.Future
import models.SessionData

class ConnectedCharitiesScheduleUploadSuccessfulController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: ConnectedCharitiesScheduleUploadSuccessfulView,
  actions: Actions
) extends BaseController {

  val onPageLoad: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadConnectedCharitiesSchedule)
      .async { implicit request =>
        if request.sessionData.connectedCharitiesScheduleCompleted then Future.successful(Ok(view()))
        else Future.successful(Redirect(controllers.routes.ClaimsTaskListController.onPageLoad))

      }

  val onSubmit: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadConnectedCharitiesSchedule)
      .async { implicit request =>
        Future.successful(Redirect(controllers.routes.ClaimsTaskListController.onPageLoad))
      }

}

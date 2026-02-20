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

package controllers.otherIncomeSchedule

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import com.google.inject.Inject
import controllers.BaseController
import views.html.ScheduleUploadFailureView
import controllers.actions.Actions
import models.SessionData

class ProblemUpdatingOtherIncomeScheduleQuarantineController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: ScheduleUploadFailureView,
  actions: Actions
) extends BaseController {

  def onPageLoad: Action[AnyContent] = actions
    .authAndGetDataWithGuard(SessionData.shouldUploadOtherIncomeSchedule) { implicit request =>
      Ok(
        view(
          messagesKeyPrefix = "problemUpdatingOtherIncomeScheduleQuarantine",
          submitAction = routes.ProblemUpdatingOtherIncomeScheduleQuarantineController.onSubmit,
          dashboardLink = controllers.routes.ClaimsTaskListController.onPageLoad
        )
      )
    }

  def onSubmit: Action[AnyContent] = actions
    .authAndGetDataWithGuard(SessionData.shouldUploadOtherIncomeSchedule) { implicit request =>
      Redirect(routes.UploadOtherIncomeScheduleController.onPageLoad)
    }
}

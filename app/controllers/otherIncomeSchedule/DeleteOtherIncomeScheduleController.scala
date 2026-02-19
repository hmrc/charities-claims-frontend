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

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.Actions
import forms.YesNoFormProvider
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ClaimsValidationService
import views.html.DeleteOtherIncomeScheduleView

import scala.concurrent.{ExecutionContext, Future}
import models.SessionData

class DeleteOtherIncomeScheduleController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: DeleteOtherIncomeScheduleView,
  actions: Actions,
  formProvider: YesNoFormProvider,
  claimsValidationService: ClaimsValidationService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean] = formProvider("deleteOtherIncomeSchedule.error.required")

  def onPageLoad: Action[AnyContent] = actions.authAndGetDataWithGuard(SessionData.shouldUploadOtherIncomeSchedule) {
    implicit request =>
      Ok(view(form))
  }

  def onSubmit: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadOtherIncomeSchedule)
      .async { implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
            value =>
              if value then {
                claimsValidationService.deleteOtherIncomeSchedule.map { _ =>
                  Redirect(controllers.routes.ClaimsTaskListController.onPageLoad)
                }
              } else {
                Future.successful(Redirect(routes.ProblemWithOtherIncomeScheduleController.onPageLoad))
              }
          )
      }
}

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

package controllers.organisationDetails

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.Actions
import forms.YesNoFormProvider
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ClaimsValidationService
import views.html.DeleteCommunityBuildingsScheduleView

import scala.concurrent.{ExecutionContext, Future}

class DeleteCommunityBuildingsScheduleController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: DeleteCommunityBuildingsScheduleView,
  actions: Actions,
  formProvider: YesNoFormProvider,
  claimsValidationService: ClaimsValidationService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean] = formProvider("deleteCommunityBuildingsSchedule.error.required")

  def onPageLoad: Action[AnyContent] = actions.authAndGetData() { implicit request =>
    Ok(view(form))
  }

  def onSubmit: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value =>
          if value then {
            claimsValidationService.deleteCommunityBuildingsSchedule.map { _ =>
              Redirect(controllers.routes.ClaimsTaskListController.onPageLoad)
            }
          } else {
            // no deletion, redirect to Add Schedule screen G2
            // TODO: This redirects to placeholder G2 screen - route to be updated in the future
            Future.successful(Redirect(controllers.organisationDetails.routes.AddScheduleController.onPageLoad))
          }
      )
  }
}

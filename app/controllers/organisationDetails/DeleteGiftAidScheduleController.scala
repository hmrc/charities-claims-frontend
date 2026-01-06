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
import views.html.DeleteGiftAidScheduleView

import scala.concurrent.{ExecutionContext, Future}

class DeleteGiftAidScheduleController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: DeleteGiftAidScheduleView,
  actions: Actions,
  formProvider: YesNoFormProvider
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean] = formProvider("deleteGiftAidSchedule.error.required")

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
            // TODO: Call backend deletion endpoint when available
            // Will need to use ExecutionContext when making actual backend call
            // Expected backend response: { "success": true }
            // On success, redirect to R2 screen
            // On error, handle error response appropriately

            // TODO: Replace with actual R2 route when screen is implemented
            // For now, redirecting to page not found as placeholder
            Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad))
          } else {
            // TODO: Replace with actual G2 route when screen is implemented
            // User selected "No" - don't delete, go back to G2 screen
            // For now, redirecting to page not found as placeholder
            Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad))
          }
      )
  }
}

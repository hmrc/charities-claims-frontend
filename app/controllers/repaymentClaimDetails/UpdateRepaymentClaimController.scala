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

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.Actions
import forms.YesNoFormProvider
import models.RepaymentClaimDetailsAnswers
import play.api.Logging
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ClaimsValidationService, SaveService}
import views.html.UpdateRepaymentClaimView

import scala.concurrent.{ExecutionContext, Future}

class UpdateRepaymentClaimController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: UpdateRepaymentClaimView,
  actions: Actions,
  formProvider: YesNoFormProvider
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean] = formProvider("updateRepaymentClaim.error.required")

  // TODO: if user selects yes and continue then we make the update. if no we redirect to CYA.

  def onPageLoad: Action[AnyContent] = actions.authAndGetData() { implicit request =>
    Ok(view(form))
  }

  def onSubmit: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        {
          case true =>
            // TODO: User selected 'Yes' - we make the update and then continue with user journey
            // TODO: This will be changed later to redirect to the correct controller once built
            Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad))

          case false =>
            // TODO: User selected 'No' - redirect to CYA with no changes made
            // TODO: This will be changed later to redirect to the correct controller once built
            Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad))
        }
      )
  }
}

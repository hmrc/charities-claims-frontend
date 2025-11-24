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

package controllers.sectionone

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.Actions
import forms.YesNoFormProvider
import models.SessionData
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SaveService
import views.html.ClaimingReferenceNumberCheckView

import scala.concurrent.{ExecutionContext, Future}

class ClaimingReferenceNumberCheckController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: ClaimingReferenceNumberCheckView,
  actions: Actions,
  formProvider: YesNoFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean] = formProvider("claimReferenceNumberCheck.error.required")

  val onPageLoad: Action[AnyContent] = actions.authAndGetData() { implicit request =>
    val previousAnswer = SessionData.SectionOne.getClaimingReferenceNumber
    Ok(view(form.withDefault(previousAnswer)))
  }

  val onSubmit: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value =>
          saveService
            .save(SessionData.SectionOne.setClaimingReferenceNumber(value))
            .map { _ =>
              if value
              then Redirect(routes.ClaimReferenceNumberInputController.onPageLoad)
              else Redirect(routes.ClaimDeclarationController.onPageLoad)
            }
      )
  }
}

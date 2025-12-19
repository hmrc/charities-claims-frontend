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

package controllers.organisationDetails

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.Actions
import forms.AuthorisedOfficialDetailsFormProvider
import models.OrganisationDetailsAnswers
import services.SaveService
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.AuthorisedOfficialDetailsView

import scala.concurrent.{ExecutionContext, Future}

class AuthorisedOfficialDetailsController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  formProvider: AuthorisedOfficialDetailsFormProvider,
  view: AuthorisedOfficialDetailsView,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  def onPageLoad: Action[AnyContent] = actions.authAndGetData() { implicit request =>
    val isUkAddress = OrganisationDetailsAnswers.getDoYouHaveUKAddress.getOrElse(false)
    val form        = formProvider(isUkAddress)

    val previousAnswer = OrganisationDetailsAnswers.getAuthorisedOfficialDetails
    val preparedForm   = previousAnswer.fold(form)(form.fill)

    Ok(view(preparedForm, isUkAddress))
  }

  def onSubmit: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    val isUkAddress = OrganisationDetailsAnswers.getDoYouHaveUKAddress.getOrElse(false)
    val form        = formProvider(isUkAddress)

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, isUkAddress))),
        value =>
          saveService
            .save(OrganisationDetailsAnswers.setAuthorisedOfficialDetails(value))
            .map { _ =>
              // change to CYA page when its created
              Redirect(routes.AuthorisedOfficialDetailsController.onPageLoad)
            }
      )
  }
}

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
import models.Mode
import models.Mode.*
import services.SaveService
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.AuthorisedOfficialDetailsView

import scala.concurrent.{ExecutionContext, Future}

class AuthorisedOfficialDetailsController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: AuthorisedOfficialDetailsView,
  actions: Actions,
  formProvider: AuthorisedOfficialDetailsFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    OrganisationDetailsAnswers.getDoYouHaveAuthorisedOfficialTrusteeUKAddress match {
      case Some(isUkAddress) =>
        val form           = formProvider(isUkAddress)
        val previousAnswer = OrganisationDetailsAnswers.getAuthorisedOfficialDetails
        val preparedForm   = previousAnswer.fold(form)(form.fill)

        Future.successful(Ok(view(preparedForm, isUkAddress, mode)))

      case None =>
        Future.successful(Redirect(routes.AuthorisedOfficialAddressController.onPageLoad(mode)))
    }
  }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    OrganisationDetailsAnswers.getDoYouHaveAuthorisedOfficialTrusteeUKAddress match {
      case Some(isUkAddress) =>
        val form = formProvider(isUkAddress)
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, isUkAddress, mode))),
            value =>
              saveService
                .save(OrganisationDetailsAnswers.setAuthorisedOfficialDetails(value))
                .map { _ =>
                  Redirect(routes.OrganisationDetailsCheckYourAnswersController.onPageLoad)
                }
          )

      case None =>
        Future.successful(Redirect(routes.AuthorisedOfficialAddressController.onPageLoad(mode)))
    }
  }
}

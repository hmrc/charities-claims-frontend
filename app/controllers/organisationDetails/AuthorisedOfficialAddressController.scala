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
import forms.YesNoFormProvider
import models.OrganisationDetailsAnswers
import models.Mode
import models.Mode.*
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SaveService
import views.html.AuthorisedOfficialAddressView

import scala.concurrent.{ExecutionContext, Future}

class AuthorisedOfficialAddressController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: AuthorisedOfficialAddressView,
  actions: Actions,
  formProvider: YesNoFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean] = formProvider("authorisedOfficialAddress.error.required")

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    if OrganisationDetailsAnswers.getAreYouACorporateTrustee.contains(false)
    then {
      val previousAnswer = OrganisationDetailsAnswers.getDoYouHaveUKAddress
      Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))
    } else { Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad)) }
  }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          saveService
            .save(OrganisationDetailsAnswers.setDoYouHaveUKAddress(value))
            .map { _ =>
              (value, mode) match {
                case (_, CheckMode)  =>
                  Redirect(routes.OrganisationDetailsCheckYourAnswersController.onPageLoad)
                case (_, NormalMode) =>
                  Redirect(routes.AuthorisedOfficialAddressController.onPageLoad(NormalMode))
              }
            }
      )
  }
}

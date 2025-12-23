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
import controllers.actions.Actions
import forms.CharityRegulatorNumberFormProvider
import models.Mode.*
import controllers.BaseController
import models.{Mode, NameOfCharityRegulator, OrganisationDetailsAnswers}
import services.SaveService
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.CharityRegulatorNumberView

import scala.concurrent.{ExecutionContext, Future}

class CharityRegulatorNumberController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  formProvider: CharityRegulatorNumberFormProvider,
  view: CharityRegulatorNumberView,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form = formProvider()

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    val previousAnswer = OrganisationDetailsAnswers.getCharityRegistrationNumber

    val nameOfCharityAnswer: Option[NameOfCharityRegulator] = OrganisationDetailsAnswers.getNameOfCharityRegulator
    if nameOfCharityAnswer.isEmpty || nameOfCharityAnswer.contains(NameOfCharityRegulator.None)
    then Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad))
    else Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))
  }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          saveService
            .save(OrganisationDetailsAnswers.setCharityRegistrationNumber(value))
            .map { _ =>
              (value, mode) match {
                case (_, CheckMode)  => Redirect(routes.OrganisationDetailsCheckYourAnswersController.onPageLoad)
                case (_, NormalMode) => Redirect(routes.CorporateTrusteeClaimController.onPageLoad(NormalMode))
              }
            }
      )
  }
}

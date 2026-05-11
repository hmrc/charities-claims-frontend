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
import forms.RadioListFormProvider
import models.*
import models.Mode.*
import models.SessionData.isCASCCharityReference
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.SaveService
import views.html.NameOfCharityRegulatorView

import scala.concurrent.{ExecutionContext, Future}

class NameOfCharityRegulatorController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: NameOfCharityRegulatorView,
  actions: Actions,
  formProvider: RadioListFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[NameOfCharityRegulator] = formProvider("nameOfCharityRegulator.error.required")

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] =
    actions.authAndGetDataWithGuard(SessionData.isRepaymentClaimDetailsComplete).async { implicit request =>
      given sessionData: SessionData = request.sessionData
      if isCASCCharityReference then Future.successful(Redirect(controllers.routes.ClaimsTaskListController.onPageLoad))
      else {
        Future.successful(Ok(view(form.withDefault(previousAnswer(request.isAgent)), mode)))
      }
    }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] =
    actions.authAndGetDataWithGuard(SessionData.isRepaymentClaimDetailsComplete).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            saveService
              .save(updatedSession(value, request.isAgent))
              .map(_ =>
                Redirect(NameOfCharityRegulatorController.nextPage(value, mode, previousAnswer(request.isAgent)))
              )
        )
    }
}

private def updatedSession(
  value: NameOfCharityRegulator,
  isAgent: Boolean
)(using sessionData: SessionData): SessionData =
  if (isAgent)
    AgentUserOrganisationDetailsAnswers.setNameOfCharityRegulator(value)
  else
    OrganisationDetailsAnswers.setNameOfCharityRegulator(value)

private def previousAnswer(isAgent: Boolean)(using sessionData: SessionData) =
  if (isAgent) {
    AgentUserOrganisationDetailsAnswers.getNameOfCharityRegulator
  } else {
    OrganisationDetailsAnswers.getNameOfCharityRegulator
  }

object NameOfCharityRegulatorController {

  def nextPage(value: NameOfCharityRegulator, mode: Mode, previousAnswer: Option[NameOfCharityRegulator]): Call =
    (value, mode, previousAnswer) match {
      // NormalMode
      case (NameOfCharityRegulator.None, NormalMode, _)                                                    =>
        routes.ReasonNotRegisteredWithRegulatorController.onPageLoad(NormalMode)
      case (_, NormalMode, _)                                                                              =>
        routes.CharityRegulatorNumberController.onPageLoad(NormalMode)

      // CheckMode: new data
      case (NameOfCharityRegulator.None, CheckMode, None)                                                  =>
        routes.ReasonNotRegisteredWithRegulatorController.onPageLoad(CheckMode)
      case (_, CheckMode, None)                                                                            =>
        routes.CharityRegulatorNumberController.onPageLoad(CheckMode)

      // CheckMode: regulator → None
      case (NameOfCharityRegulator.None, CheckMode, Some(prev)) if prev != NameOfCharityRegulator.None     =>
        routes.ReasonNotRegisteredWithRegulatorController.onPageLoad(CheckMode)

      // CheckMode: None → regulator
      case (newVal, CheckMode, Some(NameOfCharityRegulator.None)) if newVal != NameOfCharityRegulator.None =>
        routes.CharityRegulatorNumberController.onPageLoad(CheckMode)

      // CheckMode: regulator → different regulator
      case (newVal, CheckMode, Some(prev))
          if newVal != NameOfCharityRegulator.None && prev != NameOfCharityRegulator.None && newVal != prev =>
        routes.CharityRegulatorNumberController.onPageLoad(CheckMode)

      // unchanged
      case (_, CheckMode, _)                                                                               =>
        routes.OrganisationDetailsCheckYourAnswersController.onPageLoad
    }
}

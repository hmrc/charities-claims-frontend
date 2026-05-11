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
import forms.CharityRegulatorNumberFormProvider
import models.Mode.*
import models.SessionData.isCASCCharityReference
import models.*
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.SaveService
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

  private val form = formProvider()

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] =
    actions.authAndGetDataWithGuard(SessionData.isRepaymentClaimDetailsComplete).async { implicit request =>

      given SessionData = request.sessionData

      if isCASCCharityReference then {
        Future.successful(
          Redirect(controllers.routes.ClaimsTaskListController.onPageLoad)
        )
      } else {

        val regulator = getRegulatorAnswer(request.isAgent)

        if (regulator.isEmpty || regulator.contains(NameOfCharityRegulator.None)) {
          Future.successful(
            Redirect(controllers.routes.ClaimsTaskListController.onPageLoad)
          )
        } else {
          Future.successful(
            Ok(
              view(
                form.withDefault(getRegistrationNumber(request.isAgent)),
                mode
              )
            )
          )
        }
      }
    }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] =
    actions.authAndGetDataWithGuard(SessionData.isRepaymentClaimDetailsComplete).async { implicit request =>

      given SessionData = request.sessionData

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            saveService
              .save(updatedSession(value, request.isAgent))
              .map { _ =>
                Redirect(
                  CharityRegulatorNumberController.nextPage(
                    mode,
                    request.isAgent
                  )
                )
              }
        )
    }

  private def getRegistrationNumber(isAgent: Boolean)(using
    SessionData
  ): Option[String] =
    if (isAgent)
      AgentUserOrganisationDetailsAnswers.getCharityRegistrationNumber
    else
      OrganisationDetailsAnswers.getCharityRegistrationNumber

  private def getRegulatorAnswer(isAgent: Boolean)(using
    SessionData
  ): Option[NameOfCharityRegulator] =
    if (isAgent)
      AgentUserOrganisationDetailsAnswers.getNameOfCharityRegulator
    else
      OrganisationDetailsAnswers.getNameOfCharityRegulator

  private def updatedSession(
    value: String,
    isAgent: Boolean
  )(using sessionData: SessionData): SessionData =
    if (isAgent)
      AgentUserOrganisationDetailsAnswers.setCharityRegistrationNumber(value)
    else
      OrganisationDetailsAnswers.setCharityRegistrationNumber(value)
}

object CharityRegulatorNumberController {

  def nextPage(mode: Mode, isAgent: Boolean): Call =
    (isAgent, mode) match {
      case (true, NormalMode) =>
        routes.WhoShouldWeSendPaymentToController.onSubmit(NormalMode)

      case (false, NormalMode) =>
        routes.CorporateTrusteeClaimController.onPageLoad(NormalMode)

      case (_, CheckMode) =>
        routes.OrganisationDetailsCheckYourAnswersController.onPageLoad
    }
}

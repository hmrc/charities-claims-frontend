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
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import views.html.CharityExemptView
import models.{
  AgentUserOrganisationDetailsAnswers,
  Mode,
  OrganisationDetailsAnswers,
  ReasonNotRegisteredWithRegulator,
  SessionData
}
import models.Mode.*
import models.SessionData.isCASCCharityReference

import scala.concurrent.Future

class CharityExemptController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  view: CharityExemptView
) extends BaseController {

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] =
    actions.authAndGetDataWithGuard(SessionData.isRepaymentClaimDetailsComplete).async { implicit request =>
      given sessionData: SessionData = request.sessionData
      if isCASCCharityReference then Future.successful(Redirect(controllers.routes.ClaimsTaskListController.onPageLoad))
      else {
        val previousAnswer: Option[ReasonNotRegisteredWithRegulator] = if (request.isAgent) {
          AgentUserOrganisationDetailsAnswers.getReasonNotRegisteredWithRegulator
        } else {
          OrganisationDetailsAnswers.getReasonNotRegisteredWithRegulator
        }
        previousAnswer match {
          case Some(ReasonNotRegisteredWithRegulator.Exempt) => Future.successful(Ok(view(mode)))
          case _                                             => Future.successful(Redirect(controllers.routes.ClaimsTaskListController.onPageLoad))
        }
      }
    }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] =
    actions.authAndGetDataWithGuard(SessionData.isRepaymentClaimDetailsComplete).async { implicit request =>
      Future.successful(Redirect(CharityExemptController.nextPage(mode, request.isAgent)))
    }
}

object CharityExemptController {
  def nextPage(mode: Mode, isAgent: Boolean): Call =
    (isAgent, mode) match {
      case (true, NormalMode)  => routes.WhoShouldWeSendPaymentToController.onSubmit(NormalMode)
      case (false, NormalMode) => routes.CorporateTrusteeClaimController.onPageLoad(NormalMode)
      case (_, CheckMode)      => routes.OrganisationDetailsCheckYourAnswersController.onPageLoad
    }
}

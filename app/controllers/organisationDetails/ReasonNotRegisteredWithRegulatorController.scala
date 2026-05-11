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
import models.{Mode, NameOfCharityRegulator, OrganisationDetailsAnswers, ReasonNotRegisteredWithRegulator, SessionData}
import models.Mode.*
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SaveService
import views.html.ReasonNotRegisteredWithRegulatorView
import models.SessionData.isCASCCharityReference

import scala.concurrent.{ExecutionContext, Future}

class ReasonNotRegisteredWithRegulatorController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: ReasonNotRegisteredWithRegulatorView,
  actions: Actions,
  formProvider: RadioListFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[ReasonNotRegisteredWithRegulator] = formProvider("reasonNotRegisteredWithRegulator.error.required")

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] =
    actions.authAndGetDataWithGuard(SessionData.isRepaymentClaimDetailsComplete).async { implicit request =>
      given sessionData: SessionData = request.sessionData
      if isCASCCharityReference then Future.successful(Redirect(controllers.routes.ClaimsTaskListController.onPageLoad))
      else {
        val NameOfCharityAnswer: Option[NameOfCharityRegulator] =
          OrganisationDetailsAnswers.getNameOfCharityRegulator
        NameOfCharityAnswer match {
          case Some(NameOfCharityRegulator.None) =>
            val previousAnswer: Option[ReasonNotRegisteredWithRegulator] =
              OrganisationDetailsAnswers.getReasonNotRegisteredWithRegulator
            Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))

          case _ =>
            Future.successful(Redirect(controllers.routes.ClaimsTaskListController.onPageLoad))
        }
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
              .save(OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(value))
              .map { _ =>
                value match {
                  // Excepted/Exempt always flow through A2.3/A2.4 (conditional pages)
                  case ReasonNotRegisteredWithRegulator.Excepted =>
                    Redirect(routes.CharityExceptedController.onPageLoad(mode))
                  case ReasonNotRegisteredWithRegulator.Exempt   =>
                    Redirect(routes.CharityExemptController.onPageLoad(mode))
                  // LowIncome/Waiting skip A2.3/A2.4
                  case _                                         =>
                    mode match
                      case CheckMode                     => Redirect(routes.OrganisationDetailsCheckYourAnswersController.onPageLoad)
                      case NormalMode if request.isAgent =>
                        Redirect(routes.WhoShouldWeSendPaymentToController.onPageLoad(NormalMode))
                      case _                             => Redirect(routes.CorporateTrusteeClaimController.onPageLoad(NormalMode))
                }
              }
        )
    }
}

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
import controllers.actions.{AccessType, Actions, GuardAction}
import forms.RadioListFormProvider
import models.Mode.*
import models.{AgentUserOrganisationDetailsAnswers, Mode, SessionData, WhoShouldHmrcSendPaymentTo}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SaveService
import views.html.WhoShouldWeSendPaymentToView

import scala.concurrent.{ExecutionContext, Future}

class WhoShouldWeSendPaymentToController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: WhoShouldWeSendPaymentToView,
  actions: Actions,
  guard: GuardAction,
  formProvider: RadioListFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[WhoShouldHmrcSendPaymentTo] = formProvider("whoShouldWeSendPaymentTo.error.required")

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] =
    actions
      .authAndGetData()
      .andThen(
        guard(
          predicate = SessionData.isClaimNotSubmitted,
          access = AccessType.AgentOnly
        )
      )
      .async { implicit request =>
        val previousAnswer = AgentUserOrganisationDetailsAnswers.getWhoShouldHmrcSendPaymentTo
        Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))
      }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] =
    actions
      .authAndGetData()
      .andThen(
        guard(
          predicate = SessionData.isClaimNotSubmitted,
          access = AccessType.AgentOnly
        )
      )
      .async { implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
            value =>
              saveService
                .save(AgentUserOrganisationDetailsAnswers.setWhoShouldHmrcSendPaymentTo(value))
                .map(_ =>
                  mode match {
                    case NormalMode => Redirect(routes.EnterTelephoneNumberController.onPageLoad(mode))
                    case CheckMode  => Redirect(routes.OrganisationDetailsCheckYourAnswersController.onPageLoad)
                  }
                )
          )
      }
}

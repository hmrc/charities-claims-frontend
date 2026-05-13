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

import models.Mode.*
import services.SaveService
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import com.google.inject.Inject
import controllers.BaseController
import views.html.EnterTelephoneNumberView
import controllers.actions.{AccessType, Actions, GuardAction}
import forms.PhoneNumberFormProvider
import models.{AgentUserOrganisationDetailsAnswers, Mode, SessionData}
import play.api.data.Form

import scala.concurrent.{ExecutionContext, Future}

class EnterTelephoneNumberController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: EnterTelephoneNumberView,
  actions: Actions,
  guard: GuardAction,
  formProvider: PhoneNumberFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.isRepaymentClaimDetailsComplete)
      .andThen(
        guard(
          predicate = SessionData.isClaimNotSubmitted,
          access = AccessType.AgentOnly
        )
      )
      .async { implicit request =>
        val previousAnswer = AgentUserOrganisationDetailsAnswers.getDaytimeTelephoneNumber
        Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))
      }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.isRepaymentClaimDetailsComplete)
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
                .save(AgentUserOrganisationDetailsAnswers.setDaytimeTelephoneNumber(value))
                .map(_ =>
                  mode match {
                    case NormalMode => Redirect(routes.AgentHasUKAddressController.onPageLoad(mode))
                    case CheckMode  => Redirect(routes.OrganisationDetailsCheckYourAnswersController.onPageLoad)
                  }
                )
          )
      }
}

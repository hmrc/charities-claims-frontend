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
import models.OrganisationDetailsAnswers
import models.Mode
import models.Mode.*
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SaveService
import views.html.ReasonNotRegisteredWithRegulatorView
import models.ReasonNotRegisteredWithRegulator
import models.NameOfCharityRegulator

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

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    val NameOfCharityAnswer: Option[NameOfCharityRegulator] =
      OrganisationDetailsAnswers.getNameOfCharityRegulator
    NameOfCharityAnswer match {
      case Some(NameOfCharityRegulator.None) =>
        val previousAnswer: Option[ReasonNotRegisteredWithRegulator] =
          OrganisationDetailsAnswers.getReasonNotRegisteredWithRegulator
        Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))

      case _ =>
        Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad))
    }
  }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          saveService
            .save(OrganisationDetailsAnswers.setReasonNotRegisteredWithRegulator(value))
            .map { _ =>
              value match {
                case ReasonNotRegisteredWithRegulator.Excepted => Redirect(routes.CharityExceptedController.onPageLoad)
                case ReasonNotRegisteredWithRegulator.Exempt   => Redirect(routes.CharityExemptController.onPageLoad)
                case _                                         => Redirect(routes.CorporateTrusteeClaimController.onPageLoad(NormalMode))
              }
            }
      )
  }
}

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

package controllers.repaymentClaimDetails

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.BaseController
import controllers.actions.{Actions, GuardAction}
import forms.RadioListFormProvider
import models.SessionData
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.WhoShouldSendThePaymentToView

import scala.concurrent.Future

class WhoShouldSendThePaymentToController @Inject()(
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  guard: GuardAction,
  view: WhoShouldSendThePaymentToView,
  appConfig: FrontendAppConfig,
  formProvider: RadioListFormProvider,
) extends BaseController {

  val form: Form[Boolean] = formProvider("whoShouldSendThePaymentTo.error.required")

  def onPageLoad: Action[AnyContent] =
    actions
      .authAndGetData()
      .andThen(guard(SessionData.isClaimNotSubmitted))
      .async { implicit request =>
        val previousAnswer: Option[String] = OrganisationDetailsAnswers.getNameOfCharityRegulator
        
        Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))
      }

  def onSubmit: Action[AnyContent] =
    actions
      .authAndGetData()
      .andThen(guard(SessionData.isClaimNotSubmitted))
      .async { implicit request =>
        Future.successful(Redirect(appConfig.charityRepaymentDashboardUrl))
      }
}

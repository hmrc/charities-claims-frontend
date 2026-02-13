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

package controllers

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.BaseController
import controllers.actions.Actions
import forms.YesNoFormProvider
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UnregulatedDonationsService
import views.html.RegisterCharityWithARegulatorView
import java.text.DecimalFormat

class RegisterCharityWithARegulatorController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: RegisterCharityWithARegulatorView,
  actions: Actions,
  formProvider: YesNoFormProvider,
  appConfig: FrontendAppConfig,
  unregulatedDonationsService: UnregulatedDonationsService
) extends BaseController {

  val form: Form[Boolean] = formProvider("registerCharityWithARegulator.error.required")

  // default is set to Excepted limit - fallback
  private lazy val defaultFormattedLimit: String = new DecimalFormat("#,###").format(appConfig.exceptedLimit)

  def onPageLoad: Action[AnyContent] = actions.authAndGetData() { implicit request =>
    // gets dynamic limit based on charity type (LowIncome = £5,000, Excepted = £100,000)
    val formattedLimit = unregulatedDonationsService.getApplicableLimit.getOrElse(defaultFormattedLimit)
    Ok(view(appConfig.registerCharityWithARegulatorUrl, formattedLimit)(form))
  }

  def onSubmit: Action[AnyContent] = actions.authAndGetData() { implicit request =>
    val formattedLimit = unregulatedDonationsService.getApplicableLimit.getOrElse(defaultFormattedLimit)
    form
      .bindFromRequest()
      .fold(
        formWithErrors => BadRequest(view(appConfig.registerCharityWithARegulatorUrl, formattedLimit)(formWithErrors)),
        {
          case true =>
            Redirect(routes.ClaimsTaskListController.onPageLoad)

          case false =>
            // TODO: User selected 'No' - redirect to D3 placeholder screen, route to be updated in the future
            Redirect(controllers.routes.DeclarationDetailsConfirmationController.onPageLoad)
        }
      )
  }
}

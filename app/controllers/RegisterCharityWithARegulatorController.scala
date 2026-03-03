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
import models.SessionData
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{SaveService, UnregulatedDonationsService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.RegisterCharityWithARegulatorView
import java.text.DecimalFormat
import scala.concurrent.{ExecutionContext, Future}

class RegisterCharityWithARegulatorController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: RegisterCharityWithARegulatorView,
  actions: Actions,
  formProvider: YesNoFormProvider,
  appConfig: FrontendAppConfig,
  unregulatedDonationsService: UnregulatedDonationsService,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean] = formProvider("registerCharityWithARegulator.error.required")

  // default is set to Excepted limit - fallback
  private lazy val defaultFormattedLimit: String = new DecimalFormat("#,###").format(appConfig.exceptedLimit)

  def onPageLoad: Action[AnyContent] =
    actions.authAndGetDataWithGuard(SessionData.isUnregulatedLimitExceeded) { implicit request =>
      // gets dynamic limit based on charity type (LowIncome = £5,000, Excepted = £100,000)
      val formattedLimit = unregulatedDonationsService.getApplicableLimit.getOrElse(defaultFormattedLimit)
      Ok(view(appConfig.registerCharityWithARegulatorUrl, formattedLimit)(form))
    }

  def onSubmit: Action[AnyContent] =
    actions.authAndGetDataWithGuard(SessionData.isUnregulatedLimitExceeded).async { implicit request =>
      given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      val formattedLimit = unregulatedDonationsService.getApplicableLimit.getOrElse(defaultFormattedLimit)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future
              .successful(BadRequest(view(appConfig.registerCharityWithARegulatorUrl, formattedLimit)(formWithErrors))),
          {
            case true =>
              // user selected Yes - reset the flag and redirect back to claims task list
              val updatedSession = request.sessionData.copy(unregulatedLimitExceeded = false)
              saveService.save(updatedSession).map { _ =>
                Redirect(routes.ClaimsTaskListController.onPageLoad)
              }

            case false =>
              // user selected No, continue with claim - redirect to D3 declaration screen
              // TODO: route to be updated when D3 route is confirmed
              Future.successful(Redirect(controllers.routes.DeclarationDetailsConfirmationController.onPageLoad))
          }
        )
    }
}

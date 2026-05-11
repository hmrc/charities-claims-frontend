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
import controllers.actions.Actions
import models.Mode.*
import models.SessionData
import models.SessionData.isCASCCharityReference
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AboutTheOrganisationView

import scala.concurrent.Future

class AboutTheOrganisationController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  view: AboutTheOrganisationView
) extends FrontendBaseController
    with I18nSupport {

  val onPageLoad: Action[AnyContent] = actions.authAndGetDataWithGuard(SessionData.isRepaymentClaimDetailsComplete) {
    implicit request =>
      Ok(view(request.isAgent))
  }

  val onSubmit: Action[AnyContent] =
    actions.authAndGetDataWithGuard(SessionData.isRepaymentClaimDetailsComplete).async { implicit request =>
      given sessionData: SessionData = request.sessionData
      Future.successful(Redirect(AboutTheOrganisationController.nextPage(request.isAgent, isCASCCharityReference)))
    }
}

object AboutTheOrganisationController {
  def nextPage(isAgent: Boolean, isCASCCharityReference: Boolean): Call =
    (isAgent, isCASCCharityReference) match {
      case (true, true)  => routes.WhoShouldWeSendPaymentToController.onSubmit(NormalMode)
      case (false, true) => routes.CorporateTrusteeClaimController.onPageLoad(NormalMode)
      case (_, _)        => routes.NameOfCharityRegulatorController.onPageLoad(NormalMode)
    }
}

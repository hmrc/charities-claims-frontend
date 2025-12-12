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
import models.Mode.NormalMode
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.CharityExceptedView
import models.{OrganisationDetailsAnswers, ReasonNotRegisteredWithRegulator}

import scala.concurrent.{ExecutionContext, Future}

class CharityExceptedController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  view: CharityExceptedView
)(using ec: ExecutionContext)
    extends BaseController {

  def onPageLoad: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    val previousAnswer: Option[ReasonNotRegisteredWithRegulator] =
      OrganisationDetailsAnswers.getReasonNotRegisteredWithRegulator
    previousAnswer match {
      case Some(ReasonNotRegisteredWithRegulator.Excepted) => Future.successful(Ok(view()))
      case _                                               => Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad))
    }
  }

  def onSubmit: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    Future.successful(Redirect(routes.CorporateTrusteeClaimController.onPageLoad(NormalMode)))
  }
}

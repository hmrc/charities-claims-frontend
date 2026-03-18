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

package controllers.claimDeclaration

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.Actions
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.ClaimDeclarationView
import controllers.claimDeclaration.routes
import models.SessionData
import services.SaveService

import scala.concurrent.Future

class ClaimDeclarationController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  saveService: SaveService,
  view: ClaimDeclarationView
) extends BaseController {

  def onPageLoad: Action[AnyContent] =
    actions.authAndGetDataWithGuard(SessionData.isClaimDetailsComplete).async { implicit request =>
      (
        sessionData.adjustmentForOtherIncomePreviousOverClaimed
          .exists(_ > BigDecimal(0.0)),
        sessionData.prevOverclaimedGiftAid.exists(_ > BigDecimal(0.0))
      ) match {
        case (false, false) => Future.successful(Ok(view()))
        case (_, _)         =>
          // adjustment details must have been entered if either prev OtherIncome or/and GiftAid are > 0
          if sessionData.includedAnyAdjustmentsInClaimPrompt.isDefined then Future.successful(Ok(view()))
          else Future.successful(Redirect(routes.ClaimDeclarationController.onPageLoad))
      }
    }

  def onSubmit: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.isClaimDetailsComplete)
      .async { implicit request =>
        // read and understood the declaration
        if (
            // adjustment details must have been entered if either prev OtherIncome or/and GiftAid are > 0
            sessionData.adjustmentForOtherIncomePreviousOverClaimed
              .exists(_ > BigDecimal(0.0)) ||
              sessionData.prevOverclaimedGiftAid.exists(_ > BigDecimal(0.0))
          ) && sessionData.includedAnyAdjustmentsInClaimPrompt.isEmpty
        then {
          Future.successful(Redirect(controllers.routes.ClaimsTaskListController.onPageLoad))
        } else {
          saveService.save(request.sessionData.copy(understandFalseStatements = Some(true)))
          Future.successful(Redirect(routes.ClaimCompleteController.onPageLoad))
        }

      }
}

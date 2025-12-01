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

package controllers.repaymentclaimdetails

import com.google.inject.Inject
import connectors.ClaimsConnector
import controllers.actions.Actions
import models.Mode.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SaveService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  claimsConnector: ClaimsConnector,
  saveService: SaveService,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = actions.authAndGetData() { implicit request =>
    request.sessionData.repaymentClaimDetailsAnswers match {
      case Some(repaymentClaimDetailsAnswers) if repaymentClaimDetailsAnswers.hasCompleteAnswers =>
        Ok(view(repaymentClaimDetailsAnswers))

      case _ =>
        Redirect(routes.ClaimingGiftAidController.onPageLoad(NormalMode))
    }
  }
  def onSubmit: Action[AnyContent]   = actions.authAndGetData().async { implicit request =>
    request.sessionData.repaymentClaimDetailsAnswers match {
      case Some(repaymentClaimDetailsAnswers) if repaymentClaimDetailsAnswers.hasCompleteAnswers =>
        claimsConnector
          .saveClaim(repaymentClaimDetailsAnswers)
          .flatMap { claimId =>
            saveService
              .save(request.sessionData.copy(unsubmittedClaimId = Some(claimId)))
              .map { _ =>
                Redirect(
                  // TODO: replace with correct url when ready
                  "next-page-after-check-your-answers"
                )
              }
          }

      case _ =>
        Future.successful(Redirect(routes.ClaimingGiftAidController.onPageLoad(NormalMode)))
    }

  }
}

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
import connectors.ClaimsConnector
import controllers.actions.Actions
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RepaymentClaimSummaryView

import scala.concurrent.{ExecutionContext, Future}

class RepaymentClaimSummaryController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  claimsConnector: ClaimsConnector,
  view: RepaymentClaimSummaryView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  def onPageLoad: Action[AnyContent] =
    actions.authAndGetData().async { implicit request =>
      if request.sessionData.submissionReference.isDefined then
        claimsConnector.getSubmissionClaimSummary(request.sessionData.submissionReference.get).map { summaryResult =>
          Ok(view(summaryResult))
        }
      else Future.successful(Redirect(controllers.routes.ClaimsTaskListController.onPageLoad))
    }
}

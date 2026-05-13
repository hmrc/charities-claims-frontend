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

import play.api.mvc.*
import com.google.inject.Inject
import controllers.BaseController
import views.html.ClaimCompleteView
import controllers.actions.Actions

import scala.concurrent.Future

class ClaimCompleteController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: ClaimCompleteView,
  actions: Actions
) extends BaseController {

  val onPageLoad: Action[AnyContent] =
    actions.authAndGetData().async { implicit request =>
      sessionData.submissionReference match {
        case Some(value) =>
          val nextPage: Call = controllers.claimDeclaration.routes.RepaymentClaimSummaryController.onPageLoad
          Future.successful(Ok(view(nextPage, value, isAgent = request.isAgent)))
        case _           => Future.successful(Redirect(controllers.routes.ClaimsTaskListController.onPageLoad))
      }
    }
}

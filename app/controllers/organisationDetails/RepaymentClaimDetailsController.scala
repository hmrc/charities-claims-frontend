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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import com.google.inject.Inject
import controllers.BaseController
import views.html.RepaymentClaimDetailsView
import controllers.actions.Actions

import scala.concurrent.Future

class RepaymentClaimDetailsController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  view: RepaymentClaimDetailsView
) extends BaseController {

  def onPageLoad: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    Future.successful(Ok(view()))
  }

  def onSubmit: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    // Placeholder: PageNotFoundController until R1.1 is built
    Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad))
  }
}

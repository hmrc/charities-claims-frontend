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

package controllers.otherIncomeSchedule

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.BaseController
import controllers.actions.Actions
import controllers.otherIncomeSchedule.routes
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.AboutOtherIncomeScheduleView
import models.RepaymentClaimDetailsAnswers

import scala.concurrent.Future

class AboutOtherIncomeScheduleController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  view: AboutOtherIncomeScheduleView,
  appConfig: FrontendAppConfig
) extends BaseController {

  def onPageLoad: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    if RepaymentClaimDetailsAnswers.getClaimingTaxDeducted.contains(true) then {
      if request.sessionData.otherIncomeScheduleCompleted
      then {
        Future.successful(Redirect(routes.YourOtherIncomeScheduleUploadController.onPageLoad))
      } else {
        Future.successful(Ok(view(appConfig.otherIncomeScheduleSpreadsheetGuidanceUrl)))
      }
    } else {
      Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad))
    }
  }

  def onSubmit: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    Future.successful(Redirect(routes.UploadOtherIncomeScheduleController.onPageLoad))
  }
}

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

package controllers.giftAidSchedule

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.BaseController
import controllers.actions.Actions
import controllers.giftAidSchedule.routes
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.AboutGiftAidScheduleView
import models.RepaymentClaimDetailsAnswers

import scala.concurrent.Future
import models.SessionData

class AboutGiftAidScheduleController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  view: AboutGiftAidScheduleView,
  appConfig: FrontendAppConfig
) extends BaseController {

  def onPageLoad: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadGiftAidSchedule)
      .async { implicit request =>
        if RepaymentClaimDetailsAnswers.getClaimingGiftAid.contains(true) then {
          if request.sessionData.giftAidScheduleCompleted
          then {
            Future.successful(Redirect(routes.YourGiftAidScheduleUploadController.onPageLoad))
          } else {
            Future.successful(Ok(view(appConfig.giftAidScheduleSpreadsheetsToClaimBackTaxOnDonationsUrl)))
          }
        } else {
          Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad))
        }
      }

  def onSubmit: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadGiftAidSchedule)
      .async { implicit request =>
        Future.successful(Redirect(routes.UploadGiftAidScheduleController.onPageLoad))
      }
}

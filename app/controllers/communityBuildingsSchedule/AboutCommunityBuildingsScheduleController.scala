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

package controllers.communityBuildingsSchedule

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.BaseController
import controllers.actions.Actions
import controllers.communityBuildingsSchedule.routes
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.AboutCommunityBuildingsScheduleView
import models.RepaymentClaimDetailsAnswers

import scala.concurrent.Future

class AboutCommunityBuildingsScheduleController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  view: AboutCommunityBuildingsScheduleView,
  appConfig: FrontendAppConfig
) extends BaseController {

  def onPageLoad: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    if RepaymentClaimDetailsAnswers.getClaimingDonationsCollectedInCommunityBuildings
        .contains(true) && RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true)
    then {
      if request.sessionData.communityBuildingsScheduleCompleted
      then {
        Future.successful(Redirect(routes.AboutCommunityBuildingsScheduleController.onPageLoad))
        // TODO when available
        // Future.successful(Redirect(routes.YourCommunityBuildingsScheduleUploadController.onPageLoad))
      } else {
        Future.successful(Ok(view(appConfig.communityBuildingsScheduleSpreadsheetGuidanceUrl)))
      }
    } else {
      Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad))
    }
  }

  def onSubmit: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    Future.successful(Redirect(routes.AboutCommunityBuildingsScheduleController.onPageLoad))
    // TODO: when controller available
    // Future.successful(Redirect(routes.UploadCommunityBuildingsScheduleController.onPageLoad))
  }
}

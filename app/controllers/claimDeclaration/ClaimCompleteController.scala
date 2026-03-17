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
import models.SessionData

class ClaimCompleteController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: ClaimCompleteView,
  actions: Actions
) extends BaseController {

  val onPageLoad: Action[AnyContent] =
    actions.authAndGetDataWithGuard(SessionData.shouldUploadCommunityBuildingsSchedule).async { implicit request =>
      val nextPage       =
        "charity-repayment-claim-summary" // TODO - get the page that needs to be directed to
      val claimRefNumber = "ABCDE123456789012345667899XYZ" // TODO - get the correct claim Ref Number
      Future.successful(Ok(view(nextPage, claimRefNumber)))
    }

}

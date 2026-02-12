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

package controllers.otherIncomeSchedule

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.Actions
import play.api.mvc.*
import services.{ClaimsValidationService, SaveService}
import views.html.OtherIncomeScheduleUploadSuccessfulView

import scala.concurrent.ExecutionContext

class OtherIncomeScheduleUploadSuccessfulController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: OtherIncomeScheduleUploadSuccessfulView,
  actions: Actions,
  claimsValidationService: ClaimsValidationService,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val onPageLoad: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    claimsValidationService.getOtherIncomeScheduleData
      .map(_ => Ok(view()))

  }

  val onSubmit: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    saveService
      .save(
        request.sessionData.copy(
          otherIncomeScheduleData = None
        )
      )
      .map(_ => Redirect(controllers.routes.ClaimsTaskListController.onPageLoad))
  }

}

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

package controllers.connectedCharitiesSchedule

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.ClaimsValidationConnector
import controllers.BaseController
import controllers.actions.Actions
import controllers.connectedCharitiesSchedule.routes
import models.*
import play.api.mvc.*
import services.{ClaimsValidationService, PaginationService}
import views.html.ProblemWithConnectedCharitiesScheduleView

import scala.concurrent.{ExecutionContext, Future}

class ProblemWithConnectedCharitiesScheduleController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: ProblemWithConnectedCharitiesScheduleView,
  actions: Actions,
  claimsValidationConnector: ClaimsValidationConnector,
  claimsValidationService: ClaimsValidationService,
  appConfig: FrontendAppConfig
)(using ec: ExecutionContext)
    extends BaseController {

  val onPageLoad: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadConnectedCharitiesSchedule)
      .async { implicit request =>
        val claimId = request.sessionData.unsubmittedClaimId.get

        request.sessionData.connectedCharitiesScheduleFileUploadReference match {
          case None =>
            Future.successful(Redirect(routes.UploadConnectedCharitiesScheduleController.onPageLoad))

          case Some(fileUploadReference) =>
            claimsValidationConnector
              .getUploadResult(claimId, fileUploadReference)
              .map {
                case GetUploadResultValidationFailedConnectedCharities(reference, connectedCharitiesData, errors) =>
                  val currentPage      = request.getQueryString("page").flatMap(_.toIntOption).getOrElse(1)
                  val paginationResult = PaginationService.paginateValidationErrors(
                    allErrors = errors,
                    currentPage = currentPage,
                    baseUrl = routes.ProblemWithConnectedCharitiesScheduleController.onPageLoad.url
                  )
                  Ok(
                    view(
                      claimId = claimId,
                      errors = paginationResult.paginatedData,
                      paginationViewModel = paginationResult.paginationViewModel,
                      paginationStatus = paginationResult,
                      connectedCharitiesScheduleSpreadsheetGuidanceUrl =
                        appConfig.connectedCharitiesScheduleSpreadsheetGuidanceUrl
                    )
                  )

                case _ =>
                  Redirect(routes.YourConnectedCharitiesScheduleUploadController.onPageLoad)
              }
        }
      }

  val onSubmit: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadConnectedCharitiesSchedule)
      .async { implicit request =>
        claimsValidationService.deleteConnectedCharitiesSchedule
          .map(_ => Redirect(routes.UploadConnectedCharitiesScheduleController.onPageLoad))
      }
}

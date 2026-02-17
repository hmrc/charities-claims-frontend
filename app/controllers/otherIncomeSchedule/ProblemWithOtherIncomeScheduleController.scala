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
import connectors.ClaimsValidationConnector
import controllers.BaseController
import controllers.actions.Actions
import controllers.otherIncomeSchedule.routes
import models.*
import play.api.mvc.*
import services.{ClaimsValidationService, PaginationService}
import views.html.ProblemWithOtherIncomeScheduleView

import scala.concurrent.{ExecutionContext, Future}

class ProblemWithOtherIncomeScheduleController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: ProblemWithOtherIncomeScheduleView,
  actions: Actions,
  claimsValidationConnector: ClaimsValidationConnector,
  claimsValidationService: ClaimsValidationService,
  appConfig: FrontendAppConfig
)(using ec: ExecutionContext)
    extends BaseController {

  val onPageLoad: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadOtherIncomeSchedule)
      .async { implicit request =>
        request.sessionData.unsubmittedClaimId match {
          case None =>
            // if the claim id is not found, we need to redirect to the repayment claim details page
            Future
              .successful(Redirect(controllers.repaymentClaimDetails.routes.RepaymentClaimDetailsController.onPageLoad))

          case Some(claimId) =>
            request.sessionData.otherIncomeScheduleFileUploadReference match {
              case None =>
                // if the file upload reference is not found, we need to redirect to the upload page to start a new upload
                Future.successful(Redirect(routes.UploadOtherIncomeScheduleController.onPageLoad))

              case Some(fileUploadReference) =>
                claimsValidationConnector
                  .getUploadResult(claimId, fileUploadReference)
                  .map {
                    case GetUploadResultValidationFailedOtherIncome(reference, otherIncomeData, errors) =>
                      val currentPage      = request.getQueryString("page").flatMap(_.toIntOption).getOrElse(1)
                      val paginationResult = PaginationService.paginateValidationErrors(
                        allErrors = errors,
                        currentPage = currentPage,
                        baseUrl = routes.ProblemWithOtherIncomeScheduleController.onPageLoad.url
                      )
                      Ok(
                        view(
                          claimId = claimId,
                          errors = paginationResult.paginatedData,
                          paginationViewModel = paginationResult.paginationViewModel,
                          paginationStatus = paginationResult,
                          otherIncomeScheduleSpreadsheetGuidanceUrl =
                            appConfig.otherIncomeScheduleSpreadsheetGuidanceUrl
                        )
                      )

                    case _ =>
                      // In case of any other upload result, we need to redirect to the /your-other-income-schedule-upload page,
                      // which in turn will redirect to the right page based on the upload result
                      Redirect(routes.YourOtherIncomeScheduleUploadController.onPageLoad)
                  }
            }
        }
      }

  val onSubmit: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadOtherIncomeSchedule)
      .async { implicit request =>
        claimsValidationService.deleteOtherIncomeSchedule
          .map(_ => Redirect(routes.UploadOtherIncomeScheduleController.onPageLoad))
      }
}

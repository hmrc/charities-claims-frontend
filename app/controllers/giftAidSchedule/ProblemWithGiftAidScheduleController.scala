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

import play.api.mvc.*
import com.google.inject.Inject
import connectors.ClaimsValidationConnector
import controllers.BaseController
import config.FrontendAppConfig
import views.html.ProblemWithGiftAidScheduleView
import controllers.actions.Actions
import models.*
import services.{ClaimsValidationService, PaginationService}
import controllers.giftAidSchedule.routes

import scala.concurrent.{ExecutionContext, Future}

class ProblemWithGiftAidScheduleController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: ProblemWithGiftAidScheduleView,
  actions: Actions,
  claimsValidationConnector: ClaimsValidationConnector,
  claimsValidationService: ClaimsValidationService,
  appConfig: FrontendAppConfig
)(using ec: ExecutionContext)
    extends BaseController {

  val onPageLoad: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadGiftAidSchedule)
      .async { implicit request =>
        request.sessionData.unsubmittedClaimId match {
          case None =>
            // if the claim id is not found, we need to redirect to the repayment claim details page
            Future
              .successful(Redirect(controllers.repaymentClaimDetails.routes.RepaymentClaimDetailsController.onPageLoad))

          case Some(claimId) =>
            request.sessionData.giftAidScheduleFileUploadReference match {
              case None =>
                // if the file upload reference is not found, we need to redirect to the upload page to start a new upload
                Future.successful(Redirect(routes.UploadGiftAidScheduleController.onPageLoad))

              case Some(fileUploadReference) =>
                claimsValidationConnector
                  .getUploadResult(claimId, fileUploadReference)
                  .map {
                    case GetUploadResultValidationFailedGiftAid(reference, giftAidScheduleData, errors) =>
                      val currentPage      = request.getQueryString("page").flatMap(_.toIntOption).getOrElse(1)
                      val paginationResult = PaginationService.paginateValidationErrors(
                        allErrors = errors,
                        currentPage = currentPage,
                        baseUrl = routes.ProblemWithGiftAidScheduleController.onPageLoad.url
                      )
                      Ok(
                        view(
                          claimId = claimId,
                          errors = paginationResult.paginatedData,
                          paginationViewModel = paginationResult.paginationViewModel,
                          giftAidScheduleSpreadsheetGuidanceUrl = appConfig.giftAidScheduleSpreadsheetGuidanceUrl
                        )
                      )

                    case _ =>
                      // In case of any other upload result, we need to redirect to the /your-gift-aid-schedule-upload page,
                      // which in turn will redirect to the right page based on the upload result
                      Redirect(routes.YourGiftAidScheduleUploadController.onPageLoad)
                  }
            }
        }
      }

  val onSubmit: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadGiftAidSchedule)
      .async { implicit request =>
        claimsValidationService.deleteGiftAidSchedule
          .map(_ => Redirect(routes.UploadGiftAidScheduleController.onPageLoad))
      }
}

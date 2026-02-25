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

package controllers.giftAidSchedule

import play.api.mvc.*
import com.google.inject.Inject
import connectors.ClaimsValidationConnector
import controllers.BaseController
import views.html.YourGiftAidScheduleUploadView
import controllers.actions.Actions
import models.*
import services.ClaimsValidationService
import controllers.giftAidSchedule.routes

import scala.concurrent.{ExecutionContext, Future}
import services.SaveService
import config.FrontendAppConfig

class YourGiftAidScheduleUploadController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: YourGiftAidScheduleUploadView,
  actions: Actions,
  claimsValidationConnector: ClaimsValidationConnector,
  claimsValidationService: ClaimsValidationService,
  saveService: SaveService,
  appConfig: FrontendAppConfig
)(using ec: ExecutionContext)
    extends BaseController {

  def onPageLoad: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadGiftAidSchedule)
      .async { implicit request =>
        val claimId = request.sessionData.unsubmittedClaimId.get

        claimsValidationService
          .getFileUploadReference(ValidationType.GiftAid, acceptAwaitingUpload = false)
          .flatMap {
            case None =>
              // if the file upload reference is not found, we need to redirect to the upload page to start a new upload
              Future.successful(Redirect(routes.UploadGiftAidScheduleController.onPageLoad))

            case Some(fileUploadReference) =>
              claimsValidationConnector
                .getUploadResult(claimId, fileUploadReference)
                .map {
                  case uploadResult: GetUploadResultVeryfying =>
                    Ok(
                      view(
                        claimId = claimId,
                        uploadResult = uploadResult,
                        failureDetails = None,
                        screenLocked = true,
                        uploadStatusCall = routes.GiftAidScheduleUploadStatusController.status,
                        refreshIntervalSeconds = appConfig.uploadStatusRefreshIntervalSeconds,
                        reloadPageCall = routes.YourGiftAidScheduleUploadController.onPageLoad
                      )
                    )

                  case uploadResult: GetUploadResultValidating =>
                    Ok(
                      view(
                        claimId = claimId,
                        uploadResult = uploadResult,
                        failureDetails = None,
                        screenLocked = true,
                        uploadStatusCall = routes.GiftAidScheduleUploadStatusController.status,
                        refreshIntervalSeconds = appConfig.uploadStatusRefreshIntervalSeconds,
                        reloadPageCall = routes.YourGiftAidScheduleUploadController.onPageLoad
                      )
                    )

                  case uploadResult @ GetUploadResultVeryficationFailed(
                        reference,
                        validationType,
                        failureDetails
                      ) =>
                    Ok(
                      view(
                        claimId = claimId,
                        uploadResult = uploadResult,
                        failureDetails = Some(failureDetails),
                        screenLocked = false,
                        uploadStatusCall = routes.GiftAidScheduleUploadStatusController.status,
                        refreshIntervalSeconds = appConfig.uploadStatusRefreshIntervalSeconds,
                        reloadPageCall = routes.YourGiftAidScheduleUploadController.onPageLoad
                      )
                    )

                  case uploadResult =>
                    Ok(
                      view(
                        claimId = claimId,
                        uploadResult = uploadResult,
                        failureDetails = None,
                        screenLocked = false,
                        uploadStatusCall = routes.GiftAidScheduleUploadStatusController.status,
                        refreshIntervalSeconds = appConfig.uploadStatusRefreshIntervalSeconds,
                        reloadPageCall = routes.YourGiftAidScheduleUploadController.onPageLoad
                      )
                    )
                }
                .recoverWith {
                  case e: Exception if e.getMessage.contains("CLAIM_REFERENCE_DOES_NOT_EXIST") =>
                    saveService
                      .save(request.sessionData.copy(giftAidScheduleFileUploadReference = None))
                      .map(_ => Redirect(routes.UploadGiftAidScheduleController.onPageLoad))

                }
          }
      }

  def onRemove: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadGiftAidSchedule)
      .async { implicit request =>
        for {
          _ <- claimsValidationService.deleteGiftAidSchedule
        } yield Redirect(routes.UploadGiftAidScheduleController.onPageLoad)
      }

  def onSubmit: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadGiftAidSchedule)
      .async { implicit request =>
        val claimId = request.sessionData.unsubmittedClaimId.get

        request.sessionData.giftAidScheduleFileUploadReference match {
          case None =>
            // if the file upload reference is not found, we need to redirect to the upload page to start a new upload
            Future.successful(Redirect(routes.UploadGiftAidScheduleController.onPageLoad))

          case Some(fileUploadReference) =>
            claimsValidationConnector
              .getUploadResult(claimId, fileUploadReference)
              .flatMap {
                case failure: GetUploadResultVeryficationFailed =>
                  claimsValidationService.deleteGiftAidSchedule
                    .map(_ =>
                      Redirect {
                        failure.failureDetails.failureReason match {
                          case FailureReason.REJECTED   =>
                            routes.ProblemUpdatingGiftAidScheduleRejectedController.onPageLoad
                          case FailureReason.QUARANTINE =>
                            routes.ProblemUpdatingGiftAidScheduleQuarantineController.onPageLoad
                          case _                        =>
                            routes.ProblemUpdatingGiftAidScheduleUnknownErrorController.onPageLoad
                        }
                      }
                    )

                case _: GetUploadResultValidatedGiftAid =>
                  Future.successful(Redirect(routes.CheckYourGiftAidScheduleController.onPageLoad))

                case _: GetUploadResultValidationFailedGiftAid =>
                  Future.successful(Redirect(routes.ProblemWithGiftAidScheduleController.onPageLoad))

                case _ =>
                  // strange case, but we need to handle it
                  Future.successful(Redirect(routes.YourGiftAidScheduleUploadController.onPageLoad))
              }
        }
      }
}

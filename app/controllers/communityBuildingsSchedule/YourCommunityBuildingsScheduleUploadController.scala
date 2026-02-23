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

package controllers.communityBuildingsSchedule

import play.api.mvc.*
import com.google.inject.Inject
import connectors.ClaimsValidationConnector
import controllers.BaseController
import views.html.YourCommunityBuildingsScheduleUploadView
import controllers.actions.Actions
import models.*
import services.ClaimsValidationService
import controllers.communityBuildingsSchedule.routes

import scala.concurrent.{ExecutionContext, Future}
import services.SaveService

class YourCommunityBuildingsScheduleUploadController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: YourCommunityBuildingsScheduleUploadView,
  actions: Actions,
  claimsValidationConnector: ClaimsValidationConnector,
  claimsValidationService: ClaimsValidationService,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  def onPageLoad: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadCommunityBuildingsSchedule)
      .async { implicit request =>
        val claimId = request.sessionData.unsubmittedClaimId.get

        claimsValidationService
          .getFileUploadReference(ValidationType.CommunityBuildings, acceptAwaitingUpload = false)
          .flatMap {
            case None =>
              // if the file upload reference is not found, we need to redirect to the upload page to start a new upload
              Future.successful(Redirect(routes.UploadCommunityBuildingsScheduleController.onPageLoad))

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
                        screenLocked = true
                      )
                    )

                  case uploadResult: GetUploadResultValidating =>
                    Ok(
                      view(
                        claimId = claimId,
                        uploadResult = uploadResult,
                        failureDetails = None,
                        screenLocked = true
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
                        screenLocked = false
                      )
                    )

                  case uploadResult =>
                    Ok(
                      view(
                        claimId = claimId,
                        uploadResult = uploadResult,
                        failureDetails = None,
                        screenLocked = false
                      )
                    )
                }
                .recoverWith {
                  case e: Exception if e.getMessage.contains("CLAIM_REFERENCE_DOES_NOT_EXIST") =>
                    saveService
                      .save(request.sessionData.copy(communityBuildingsScheduleFileUploadReference = None))
                      .map(_ => Redirect(routes.UploadCommunityBuildingsScheduleController.onPageLoad))

                }
          }
      }

  def onRemove: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadCommunityBuildingsSchedule)
      .async { implicit request =>
        for {
          _ <- claimsValidationService.deleteCommunityBuildingsSchedule
        } yield Redirect(routes.UploadCommunityBuildingsScheduleController.onPageLoad)
      }

  def onSubmit: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadCommunityBuildingsSchedule)
      .async { implicit request =>
        val claimId = request.sessionData.unsubmittedClaimId.get

        request.sessionData.communityBuildingsScheduleFileUploadReference match {
          case None =>
            // if the file upload reference is not found, we need to redirect to the upload page to start a new upload
            Future.successful(Redirect(routes.UploadCommunityBuildingsScheduleController.onPageLoad))

          case Some(fileUploadReference) =>
            claimsValidationConnector
              .getUploadResult(claimId, fileUploadReference)
              .flatMap {
                case failure: GetUploadResultVeryficationFailed =>
                  claimsValidationService.deleteCommunityBuildingsSchedule
                    .map(_ =>
                      Redirect {
                        failure.failureDetails.failureReason match {
                          case FailureReason.REJECTED   =>
                            routes.ProblemUpdatingCommunityBuildingsScheduleRejectedController.onPageLoad
                          case FailureReason.QUARANTINE =>
                            routes.ProblemUpdatingCommunityBuildingsScheduleQuarantineController.onPageLoad
                          case _                        =>
                            routes.ProblemUpdatingCommunityBuildingsScheduleUnknownErrorController.onPageLoad
                        }
                      }
                    )

                case _: GetUploadResultValidatedCommunityBuildings =>
                  Future.successful(Redirect(routes.CheckYourCommunityBuildingsScheduleController.onPageLoad))

                case _: GetUploadResultValidationFailedCommunityBuildings =>
                  Future.successful(Redirect(routes.ProblemWithCommunityBuildingsScheduleController.onPageLoad))

                case _ =>
                  // strange case, but we need to handle it
                  Future.successful(Redirect(routes.YourCommunityBuildingsScheduleUploadController.onPageLoad))
              }
        }
      }
}

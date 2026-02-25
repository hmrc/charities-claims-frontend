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
import connectors.ClaimsValidationConnector
import controllers.BaseController
import controllers.actions.Actions
import controllers.connectedCharitiesSchedule.routes
import models.*
import play.api.mvc.*
import services.{ClaimsValidationService, SaveService}
import views.html.YourConnectedCharitiesScheduleUploadView

import scala.concurrent.{ExecutionContext, Future}

class YourConnectedCharitiesScheduleUploadController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: YourConnectedCharitiesScheduleUploadView,
  actions: Actions,
  claimsValidationConnector: ClaimsValidationConnector,
  claimsValidationService: ClaimsValidationService,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  def onPageLoad: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadConnectedCharitiesSchedule)
      .async { implicit request =>
        val claimId = request.sessionData.unsubmittedClaimId.get

        claimsValidationService
          .getFileUploadReference(ValidationType.ConnectedCharities, acceptAwaitingUpload = false)
          .flatMap {
            case None =>
              // if the file upload reference is not found, we need to redirect to the upload page to start a new upload
              Future.successful(Redirect(routes.UploadConnectedCharitiesScheduleController.onPageLoad))

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
                      .save(request.sessionData.copy(connectedCharitiesScheduleFileUploadReference = None))
                      .map(_ => Redirect(routes.UploadConnectedCharitiesScheduleController.onPageLoad))
                }
          }
      }

  def onRemove: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadConnectedCharitiesSchedule)
      .async { implicit request =>
        for {
          _ <- claimsValidationService.deleteConnectedCharitiesSchedule
        } yield Redirect(routes.UploadConnectedCharitiesScheduleController.onPageLoad)
      }

  def onSubmit: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadConnectedCharitiesSchedule)
      .async { implicit request =>
        val claimId = request.sessionData.unsubmittedClaimId.get

        request.sessionData.connectedCharitiesScheduleFileUploadReference match {
          case None =>
            // if the file upload reference is not found, we need to redirect to the upload page to start a new upload
            Future.successful(Redirect(routes.UploadConnectedCharitiesScheduleController.onPageLoad))

          case Some(fileUploadReference) =>
            claimsValidationConnector
              .getUploadResult(claimId, fileUploadReference)
              .flatMap {
                case failure: GetUploadResultVeryficationFailed =>
                  claimsValidationService.deleteConnectedCharitiesSchedule
                    .map(_ =>
                      Redirect {
                        failure.failureDetails.failureReason match {
                          case FailureReason.QUARANTINE =>
                            routes.UploadConnectedCharitiesScheduleController.onPageLoad
                          case FailureReason.REJECTED   =>
                            routes.ProblemUpdatingConnectedCharitiesScheduleRejectedController.onPageLoad
                          case _                        =>
                            routes.UploadConnectedCharitiesScheduleController.onPageLoad
                        }
                      }
                    )

                case _: GetUploadResultValidatedConnectedCharities =>
                  Future.successful(Redirect(routes.CheckYourConnectedCharitiesScheduleController.onPageLoad))

                case _: GetUploadResultValidationFailedConnectedCharities =>
                  Future.successful(Redirect(routes.ProblemWithConnectedCharitiesScheduleController.onPageLoad))

                case _ =>
                  // strange case, but we need to handle it
                  Future.successful(Redirect(routes.YourConnectedCharitiesScheduleUploadController.onPageLoad))
              }
        }
      }

}

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
import connectors.ClaimsValidationConnector
import controllers.BaseController
import controllers.actions.Actions
import controllers.otherIncomeSchedule.routes
import models.*
import play.api.mvc.*
import services.{ClaimsValidationService, SaveService}
import views.html.YourOtherIncomeScheduleUploadView

import scala.concurrent.{ExecutionContext, Future}

class YourOtherIncomeScheduleUploadController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: YourOtherIncomeScheduleUploadView,
  actions: Actions,
  claimsValidationConnector: ClaimsValidationConnector,
  claimsValidationService: ClaimsValidationService,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  def onPageLoad: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadOtherIncomeSchedule)
      .async { implicit request =>
        request.sessionData.unsubmittedClaimId match {
          case None =>
            // if the claim id is not found, we need to redirect to the repayment claim details page
            Future
              .successful(Redirect(controllers.repaymentClaimDetails.routes.RepaymentClaimDetailsController.onPageLoad))

          case Some(claimId) =>
            claimsValidationService
              .getFileUploadReference(ValidationType.OtherIncome, acceptAwaitingUpload = false)
              .flatMap {
                case None =>
                  // if the file upload reference is not found, we need to redirect to the upload page to start a new upload
                  Future.successful(Redirect(routes.UploadOtherIncomeScheduleController.onPageLoad))

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
                          .save(request.sessionData.copy(otherIncomeScheduleFileUploadReference = None))
                          .map(_ => Redirect(routes.UploadOtherIncomeScheduleController.onPageLoad))

                    }
              }
        }

      }

  def onRemove: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadOtherIncomeSchedule)
      .async { implicit request =>
        for {
          _ <- claimsValidationService.deleteOtherIncomeSchedule
        } yield Redirect(routes.UploadOtherIncomeScheduleController.onPageLoad)
      }

  def onSubmit: Action[AnyContent] =
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
                  .flatMap {
                    case failure: GetUploadResultVeryficationFailed =>
                      claimsValidationService.deleteOtherIncomeSchedule
                        .map(_ =>
                          Redirect {
                            failure.failureDetails.failureReason match {
                              case FailureReason.QUARANTINE =>
                                routes.ProblemUpdatingOtherIncomeScheduleQuarantineController.onPageLoad
                              case _                        =>
                                routes.ProblemUpdatingOtherIncomeScheduleUnknownErrorController.onPageLoad
                            }
                          }
                        )

                    case _: GetUploadResultValidatedOtherIncome =>
                      Future.successful(Redirect(routes.CheckYourOtherIncomeScheduleController.onPageLoad))

                    case _: GetUploadResultValidationFailedOtherIncome =>
                      Future.successful(Redirect(routes.ProblemWithOtherIncomeScheduleController.onPageLoad))

                    case _ =>
                      // strange case, but we need to handle it
                      Future.successful(Redirect(routes.YourOtherIncomeScheduleUploadController.onPageLoad))
                  }
            }
        }
      }

}

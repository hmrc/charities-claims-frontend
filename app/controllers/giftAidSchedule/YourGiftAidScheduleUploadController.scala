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

class YourGiftAidScheduleUploadController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: YourGiftAidScheduleUploadView,
  actions: Actions,
  claimsValidationConnector: ClaimsValidationConnector,
  claimsValidationService: ClaimsValidationService
)(using ec: ExecutionContext)
    extends BaseController {

  def onPageLoad: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    request.sessionData.unsubmittedClaimId match {
      case None =>
        // if the claim id is not found, we need to redirect to the repayment claim details page
        Future.successful(Redirect(controllers.repaymentClaimDetails.routes.RepaymentClaimDetailsController.onPageLoad))

      case Some(claimId) =>
        claimsValidationService
          .getFileUploadReference(ValidationType.GiftAid)
          .flatMap {
            case None =>
              // if the file upload reference is not found, we need to redirect to the upload page to start a new upload
              Future.successful(Redirect(routes.UploadGiftAidScheduleController.onPageLoad))

            case Some(fileUploadReference) =>
              claimsValidationConnector
                .getUploadResult(claimId, fileUploadReference)
                .map {
                  case uploadResult: GetUploadResultAwaitingUpload =>
                    Ok(
                      view(
                        claimId = claimId,
                        uploadResult = uploadResult,
                        failureDetails = None,
                        screenLocked = true
                      )
                    )

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

                  case uploadResult @ GetUploadResultVeryficationFailed(reference, validationType, failureDetails) =>
                    Ok(
                      view(
                        claimId = claimId,
                        uploadResult = uploadResult,
                        failureDetails = Some(failureDetails),
                        screenLocked = true
                      )
                    )

                  case uploadResult: GetUploadResultValidatedGiftAid =>
                    Ok(
                      view(
                        claimId = claimId,
                        uploadResult = uploadResult,
                        failureDetails = None,
                        screenLocked = false
                      )
                    )

                  case _ =>
                    // Ok(view(claimId = claimId, uploadResult = uploadResult, failureDetails = None, screenLocked = false))
                    // strange case, but we need to handle it
                    Redirect(routes.YourGiftAidScheduleUploadController.onPageLoad)
                }
          }
    }

  }

  def onRemove: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    for {
      _ <- claimsValidationService.deleteGiftAidSchedule
    } yield Redirect(routes.UploadGiftAidScheduleController.onPageLoad)
  }

  def onSubmit: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    request.sessionData.unsubmittedClaimId match {
      case None =>
        // if the claim id is not found, we need to redirect to the repayment claim details page
        Future.successful(Redirect(controllers.repaymentClaimDetails.routes.RepaymentClaimDetailsController.onPageLoad))

      case Some(claimId) =>
        request.sessionData.giftAidScheduleFileUploadReference match {
          case None =>
            // if the file upload reference is not found, we need to redirect to the upload page to start a new upload
            Future.successful(Redirect(routes.UploadGiftAidScheduleController.onPageLoad))

          case Some(fileUploadReference) =>
            claimsValidationConnector
              .getUploadResult(claimId, fileUploadReference)
              .map {
                case _: GetUploadResultValidatedGiftAid =>
                  Redirect(routes.CheckYourGiftAidScheduleController.onPageLoad)

                case _: GetUploadResultValidationFailedGiftAid =>
                  Redirect(routes.ProblemWithGiftAidScheduleController.onPageLoad)

                case _ =>
                  // strange case, but we need to handle it
                  Redirect(routes.YourGiftAidScheduleUploadController.onPageLoad)
              }
        }
    }
  }

}

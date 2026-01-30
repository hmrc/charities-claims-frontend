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

import utils.ISODateTime
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import com.google.inject.Inject
import connectors.{ClaimsValidationConnector, UpscanInitiateConnector}
import controllers.BaseController
import config.FrontendAppConfig
import views.html.UploadGifAidScheduleView
import controllers.actions.Actions
import models.*
import models.requests.DataRequest
import services.SaveService
import controllers.giftAidSchedule.routes

import scala.concurrent.{ExecutionContext, Future}

class UploadGiftAidScheduleController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: UploadGifAidScheduleView,
  actions: Actions,
  upscanInitiateConnector: UpscanInitiateConnector,
  claimsValidationConnector: ClaimsValidationConnector,
  saveService: SaveService,
  appConfig: FrontendAppConfig
)(using ec: ExecutionContext)
    extends BaseController {

  val onPageLoad: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    request.sessionData.unsubmittedClaimId match {
      case None =>
        // if the claim id is not found, we need to redirect to the repayment claim details page
        Future.successful(Redirect(controllers.repaymentClaimDetails.routes.RepaymentClaimDetailsController.onPageLoad))

      case Some(claimId) =>
        request.sessionData.giftAidScheduleFileUploadReference match {
          case Some(_) =>
            // if the file upload reference is found, we need to redirect to the your gift aid schedule upload page
            Future.successful(Redirect(routes.YourGiftAidScheduleUploadController.onPageLoad))

          case None =>
            for {
              upscanInitiateResponse <- getUpscanInitiateResponse(claimId, appConfig.baseUrl)
            } yield Ok(
              view(
                claimId = claimId,
                upscanInitiateResponse = upscanInitiateResponse,
                allowedFileTypesHint = appConfig.allowedFileTypesHint,
                filePickerAcceptFilter = appConfig.filePickerAcceptFilter,
                hasError = true
              )
            )
        }
    }
  }

  private def getUpscanInitiateResponse(
    claimId: String,
    baseUrl: String
  )(using request: DataRequest[AnyContent]): Future[UpscanInitiateResponse] =
    request.sessionData.giftAidScheduleUpscanInitialization match {
      case Some(upscanInitiateResponse) =>
        // if the upscan initiate response is known, we can return it directly
        Future.successful(upscanInitiateResponse)

      case None =>
        for {
          upscanInitiateResponse <-
            for {
              upscanInitiateResponse <-
                upscanInitiateConnector.initiate(
                  claimId = claimId,
                  request = UpscanInitiateRequest(
                    successRedirect = baseUrl + routes.UploadGiftAidScheduleController.onUploadSuccess(None).url,
                    errorRedirect = baseUrl + routes.UploadGiftAidScheduleController.onUploadError.url,
                    maximumFileSize = Some(appConfig.maxGifAidScheduleUploadSize)
                  )
                )
              _                      <-
                claimsValidationConnector
                  .createUploadTracking(
                    claimId = claimId,
                    request = CreateUploadTrackingRequest(
                      reference = upscanInitiateResponse.reference,
                      validationType = ValidationType.GiftAid,
                      uploadUrl = upscanInitiateResponse.uploadRequest.href,
                      initiateTimestamp = ISODateTime.timestampNow()
                    )
                  )
            } yield upscanInitiateResponse
          _                      <-
            // store upscan initiate response because charities-claims-validation will not accept the same reference twice
            saveService.save(
              request.sessionData
                .copy(giftAidScheduleUpscanInitialization = Some(upscanInitiateResponse))
            )
        } yield upscanInitiateResponse
    }

  def onUploadSuccess(key: Option[String]): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    request.sessionData.unsubmittedClaimId match {
      case None =>
        // if the claim id is not found, we need to redirect to the repayment claim details page
        Future.successful(Redirect(controllers.repaymentClaimDetails.routes.RepaymentClaimDetailsController.onPageLoad))

      case Some(claimId) =>
        request.sessionData.giftAidScheduleFileUploadReference match {
          case Some(_) =>
            // if the file upload reference is found, we need to redirect to the your gift aid schedule upload page
            Future.successful(Redirect(routes.YourGiftAidScheduleUploadController.onPageLoad))

          case None =>
            request.sessionData.giftAidScheduleUpscanInitialization match {
              case Some(upscanInitiateResponse)
                  // check if the received key is the same as the reference from the upscan initiate response
                  if key.map(UpscanReference(_)).contains(upscanInitiateResponse.reference) =>
                claimsValidationConnector
                  .getUploadSummary(claimId)
                  .flatMap(
                    _.uploads
                      .find(_.validationType == ValidationType.GiftAid)
                      .match {
                        case None =>
                          // upload summary not found, so we need to start a new upload
                          // remove existing upscan initiate response because charities-claims-validation will need fresh upscan reference
                          saveService
                            .save(
                              request.sessionData.copy(
                                giftAidScheduleFileUploadReference = None,
                                giftAidScheduleUpscanInitialization = None
                              )
                            )
                            .map(_ => Redirect(routes.UploadGiftAidScheduleController.onPageLoad))

                        case Some(uploadSummary) =>
                          // store file upload reference and remove upscan initiate response because no longer needed
                          saveService
                            .save(
                              request.sessionData.copy(
                                giftAidScheduleFileUploadReference = Some(uploadSummary.reference),
                                giftAidScheduleUpscanInitialization = None
                              )
                            )
                            .map(_ => Redirect(routes.YourGiftAidScheduleUploadController.onPageLoad))
                      }
                  )

              case _ =>
                // if the upload summary is not found, we need to redirect to the upload page to start a new upload
                Future.successful(Redirect(routes.UploadGiftAidScheduleController.onPageLoad))
            }
        }
    }
  }

  def onUploadError: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    request.sessionData.giftAidScheduleFileUploadReference match {
      case Some(_) =>
        // if the file upload reference is found, we need to redirect to the your gift aid schedule upload page
        Future.successful(Redirect(routes.YourGiftAidScheduleUploadController.onPageLoad))

      case None =>
        request.sessionData.giftAidScheduleUpscanInitialization match {
          case Some(upscanInitiateResponse) =>
            Future.successful(
              Ok(
                view(
                  claimId = request.sessionData.unsubmittedClaimId.get,
                  upscanInitiateResponse = upscanInitiateResponse,
                  allowedFileTypesHint = appConfig.allowedFileTypesHint,
                  filePickerAcceptFilter = appConfig.filePickerAcceptFilter,
                  hasError = true // we are showing the error page because the upload failed
                )
              )
            )

          case None =>
            // if the upscan initiate response is not found, we need to redirect to the upload page to start a new upload
            Future.successful(Redirect(routes.UploadGiftAidScheduleController.onPageLoad))
        }
    }

  }

}

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
import connectors.UpscanInitiateConnector
import controllers.BaseController
import controllers.actions.Actions
import controllers.connectedCharitiesSchedule.routes
import models.*
import models.requests.DataRequest
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ClaimsValidationService, SaveService}
import utils.ISODateTime
import views.html.UploadConnectedCharitiesScheduleView

import scala.concurrent.{ExecutionContext, Future}
import play.api.Logging

class UploadConnectedCharitiesScheduleController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: UploadConnectedCharitiesScheduleView,
  actions: Actions,
  upscanInitiateConnector: UpscanInitiateConnector,
  claimsValidationService: ClaimsValidationService,
  saveService: SaveService,
  appConfig: FrontendAppConfig
)(using ec: ExecutionContext)
    extends BaseController
    with Logging {

  val onPageLoad: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadConnectedCharitiesSchedule)
      .async { implicit request =>
        request.sessionData.unsubmittedClaimId match {
          case None =>
            Future
              .successful(Redirect(controllers.repaymentClaimDetails.routes.RepaymentClaimDetailsController.onPageLoad))

          case Some(claimId) =>
            request.sessionData.connectedCharitiesScheduleUpscanInitialization match {
              case Some(upscanInitiateResponse) =>
                Future.successful(
                  Ok(
                    view(
                      appConfig.connectedCharitiesScheduleSpreadsheetGuidanceUrl,
                      claimId = claimId,
                      upscanInitiateResponse = upscanInitiateResponse,
                      allowedFileTypesHint = appConfig.allowedFileTypesHint,
                      filePickerAcceptFilter = appConfig.filePickerAcceptFilter,
                      errorCode = None
                    )
                  )
                )

              case None =>
                claimsValidationService
                  .getFileUploadReference(ValidationType.ConnectedCharities)
                  .flatMap {
                    case Some(_) =>
                      Future.successful(Redirect(routes.YourConnectedCharitiesScheduleUploadController.onPageLoad))

                    case None =>
                      logger.info(s"Initiating ConnectedCharities schedule upload for claimId=$claimId")
                      for {
                        upscanInitiateResponse <- getUpscanInitiateResponse(claimId, appConfig.baseUrl)
                      } yield Ok(
                        view(
                          appConfig.connectedCharitiesScheduleSpreadsheetGuidanceUrl,
                          claimId = claimId,
                          upscanInitiateResponse = upscanInitiateResponse,
                          allowedFileTypesHint = appConfig.allowedFileTypesHint,
                          filePickerAcceptFilter = appConfig.filePickerAcceptFilter,
                          errorCode = None
                        )
                      )
                  }
            }
        }
      }

  private def getUpscanInitiateResponse(
    claimId: String,
    baseUrl: String
  )(using request: DataRequest[AnyContent]): Future[UpscanInitiateResponse] =
    for {
      upscanInitiateResponse <-
        for {
          upscanInitiateResponse <-
            upscanInitiateConnector.initiate(
              claimId = claimId,
              request = UpscanInitiateRequest(
                successRedirect = baseUrl + routes.UploadConnectedCharitiesScheduleController.onUploadSuccess.url,
                errorRedirect = baseUrl + routes.UploadConnectedCharitiesScheduleController.onUploadError.url,
                maximumFileSize = Some(appConfig.maxConnectedCharitiesScheduleUploadSize),
                minimumFileSize = Some(1)
              )
            )
          _                      <-
            claimsValidationService
              .createUploadTracking(
                claimId = claimId,
                request = CreateUploadTrackingRequest(
                  reference = upscanInitiateResponse.reference,
                  validationType = ValidationType.ConnectedCharities,
                  uploadUrl = upscanInitiateResponse.uploadRequest.href,
                  initiateTimestamp = ISODateTime.timestampNow(),
                  fields = upscanInitiateResponse.uploadRequest.fields
                )
              )
        } yield upscanInitiateResponse
      _                      <-
        saveService.save(
          request.sessionData
            .copy(
              connectedCharitiesScheduleUpscanInitialization = Some(upscanInitiateResponse),
              connectedCharitiesScheduleFileUploadReference =
                Some(FileUploadReference(upscanInitiateResponse.reference))
            )
        )
    } yield upscanInitiateResponse

  def onUploadSuccess: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadConnectedCharitiesSchedule)
      .async { implicit request =>
        claimsValidationService
          .getFileUploadReference(ValidationType.ConnectedCharities, acceptAwaitingUpload = true)
          .flatMap {
            case Some(fileUploadReference) =>
              val claimId = request.sessionData.unsubmittedClaimId.get
              logger.info(
                s"ConnectedCharities schedule uploaded successfully for claimId=$claimId reference=$fileUploadReference"
              )
              claimsValidationService
                .updateUploadStatus(
                  claimId = claimId,
                  reference = fileUploadReference,
                  ValidationType.ConnectedCharities
                )
                .map(_ => Redirect(routes.YourConnectedCharitiesScheduleUploadController.onPageLoad))

            case None =>
              Future.successful(Redirect(routes.UploadConnectedCharitiesScheduleController.onPageLoad))

          }
      }

  def onUploadError: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadConnectedCharitiesSchedule)
      .async { implicit request =>
        val errorCode = request.getQueryString("errorCode")
        logger.warn(s"ConnectedCharities schedule upload error for claimId=${request.sessionData.unsubmittedClaimId
            .getOrElse("-")}: errorCode=${errorCode.getOrElse("-")}")
        request.sessionData.connectedCharitiesScheduleUpscanInitialization match {
          case Some(upscanInitiateResponse) =>
            Future.successful(
              BadRequest(
                view(
                  appConfig.connectedCharitiesScheduleSpreadsheetGuidanceUrl,
                  claimId = request.sessionData.unsubmittedClaimId.get,
                  upscanInitiateResponse = upscanInitiateResponse,
                  allowedFileTypesHint = appConfig.allowedFileTypesHint,
                  filePickerAcceptFilter = appConfig.filePickerAcceptFilter,
                  errorCode = errorCode
                )
              )
            )

          case None =>
            Future.successful(Redirect(routes.UploadConnectedCharitiesScheduleController.onPageLoad))
        }

      }

}

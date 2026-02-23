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
import connectors.UpscanInitiateConnector
import controllers.BaseController
import controllers.actions.Actions
import controllers.otherIncomeSchedule.routes
import models.requests.DataRequest
import models.*
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ClaimsValidationService, SaveService}
import utils.ISODateTime
import views.html.UploadOtherIncomeScheduleView

import scala.concurrent.{ExecutionContext, Future}

class UploadOtherIncomeScheduleController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: UploadOtherIncomeScheduleView,
  actions: Actions,
  upscanInitiateConnector: UpscanInitiateConnector,
  claimsValidationService: ClaimsValidationService,
  saveService: SaveService,
  appConfig: FrontendAppConfig
)(using ec: ExecutionContext)
    extends BaseController {

  val onPageLoad: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadOtherIncomeSchedule)
      .async { implicit request =>
        val claimId = request.sessionData.unsubmittedClaimId.get

        request.sessionData.otherIncomeScheduleUpscanInitialization match {
          case Some(upscanInitiateResponse) =>
            Future.successful(
              Ok(
                view(
                  appConfig.otherIncomeScheduleSpreadsheetGuidanceUrl,
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
              .getFileUploadReference(ValidationType.OtherIncome)
              .flatMap {
                case Some(_) =>
                  // if the file upload reference is found, we need to redirect to your other income schedule upload page
                  Future.successful(Redirect(routes.YourOtherIncomeScheduleUploadController.onPageLoad))

                case None =>
                  for {
                    upscanInitiateResponse <- getUpscanInitiateResponse(claimId, appConfig.baseUrl)
                  } yield Ok(
                    view(
                      appConfig.otherIncomeScheduleSpreadsheetGuidanceUrl,
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
                successRedirect = baseUrl + routes.UploadOtherIncomeScheduleController.onUploadSuccess.url,
                errorRedirect = baseUrl + routes.UploadOtherIncomeScheduleController.onUploadError.url,
                maximumFileSize = Some(appConfig.maxOtherIncomeScheduleUploadSize),
                minimumFileSize = Some(1)
              )
            )
          _                      <-
            claimsValidationService
              .createUploadTracking(
                claimId = claimId,
                request = CreateUploadTrackingRequest(
                  reference = upscanInitiateResponse.reference,
                  validationType = ValidationType.OtherIncome,
                  uploadUrl = upscanInitiateResponse.uploadRequest.href,
                  initiateTimestamp = ISODateTime.timestampNow(),
                  fields = upscanInitiateResponse.uploadRequest.fields
                )
              )
        } yield upscanInitiateResponse
      _                      <-
        // store upscan initiate response because charities-claims-validation will not accept the same reference twice
        saveService.save(
          request.sessionData
            .copy(
              otherIncomeScheduleUpscanInitialization = Some(upscanInitiateResponse),
              otherIncomeScheduleFileUploadReference = Some(FileUploadReference(upscanInitiateResponse.reference))
            )
        )
    } yield upscanInitiateResponse

  def onUploadSuccess: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadOtherIncomeSchedule)
      .async { implicit request =>
        claimsValidationService
          .getFileUploadReference(ValidationType.OtherIncome, acceptAwaitingUpload = true)
          .flatMap {
            case Some(fileUploadReference) =>
              claimsValidationService
                .updateUploadStatus(
                  claimId = request.sessionData.unsubmittedClaimId.get,
                  reference = fileUploadReference,
                  ValidationType.OtherIncome
                )
                .map(_ => Redirect(routes.YourOtherIncomeScheduleUploadController.onPageLoad))

            case None =>
              Future.successful(Redirect(routes.UploadOtherIncomeScheduleController.onPageLoad))

          }
      }

  def onUploadError: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadOtherIncomeSchedule)
      .async { implicit request =>
        val errorCode = request.getQueryString("errorCode")
        request.sessionData.otherIncomeScheduleUpscanInitialization match {
          case Some(upscanInitiateResponse) =>
            Future.successful(
              BadRequest(
                view(
                  appConfig.otherIncomeScheduleSpreadsheetGuidanceUrl,
                  claimId = request.sessionData.unsubmittedClaimId.get,
                  upscanInitiateResponse = upscanInitiateResponse,
                  allowedFileTypesHint = appConfig.allowedFileTypesHint,
                  filePickerAcceptFilter = appConfig.filePickerAcceptFilter,
                  errorCode = errorCode
                )
              )
            )

          case None =>
            // if the upscan initiate response is not found, we need to redirect to the upload page to start a new upload
            Future.successful(Redirect(routes.UploadOtherIncomeScheduleController.onPageLoad))
        }

      }

}

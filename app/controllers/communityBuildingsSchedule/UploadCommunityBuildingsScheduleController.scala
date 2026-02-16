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

import utils.ISODateTime
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import com.google.inject.Inject
import connectors.UpscanInitiateConnector
import controllers.BaseController
import config.FrontendAppConfig
import views.html.UploadCommunityBuildingsScheduleView
import controllers.actions.Actions
import models.*
import models.requests.DataRequest
import services.SaveService
import controllers.communityBuildingsSchedule.routes
import models.ValidationType.CommunityBuildings

import _root_.scala.concurrent.{ExecutionContext, Future}
import services.ClaimsValidationService

class UploadCommunityBuildingsScheduleController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: UploadCommunityBuildingsScheduleView,
  actions: Actions,
  upscanInitiateConnector: UpscanInitiateConnector,
  claimsValidationService: ClaimsValidationService,
  saveService: SaveService,
  appConfig: FrontendAppConfig
)(using ec: ExecutionContext)
    extends BaseController {

  val onPageLoad: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    if RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true) && RepaymentClaimDetailsAnswers.getClaimingDonationsCollectedInCommunityBuildings.contains(true) then {
      request.sessionData.unsubmittedClaimId match {
        case None =>
          // if the claim id is not found, we need to redirect to the repayment claim details page
          Future.successful(Redirect(controllers.repaymentClaimDetails.routes.RepaymentClaimDetailsController.onPageLoad))

        case Some(claimId) =>
          request.sessionData.communityBuildingsScheduleUpscanInitialization match {
            case Some(upscanInitiateResponse) =>
              Future.successful(
                Ok(
                  view(
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
                .getFileUploadReference(ValidationType.CommunityBuildings)
                .flatMap {
                  case Some(_) =>
                    // if the file upload reference is found, we need to redirect to your Community Buildings schedule upload page
                    // TODO: redirect when available
                    // Future.successful(Redirect(routes.YourCommunityBuildingsScheduleUploadController.onPageLoad))
                    Future.successful(Redirect(routes.UploadCommunityBuildingsScheduleController.onPageLoad))

                  case None =>
                    for {
                      upscanInitiateResponse <- getUpscanInitiateResponse(claimId, appConfig.baseUrl)
                    } yield Ok(
                      view(
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
    else Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad))
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
                successRedirect = baseUrl + routes.UploadCommunityBuildingsScheduleController.onUploadSuccess.url,
                errorRedirect = baseUrl + routes.UploadCommunityBuildingsScheduleController.onUploadError.url,
                maximumFileSize = Some(appConfig.maxCommunityBuildingsScheduleUploadSize)
              )
            )
          _                      <-
            claimsValidationService
              .createUploadTracking(
                claimId = claimId,
                request = CreateUploadTrackingRequest(
                  reference = upscanInitiateResponse.reference,
                  validationType = ValidationType.CommunityBuildings,
                  uploadUrl = upscanInitiateResponse.uploadRequest.href,
                  initiateTimestamp = ISODateTime.timestampNow()
                )
              )
        } yield upscanInitiateResponse
      _                      <-
        // store upscan initiate response because charities-claims-validation will not accept the same reference twice
        saveService.save(
          request.sessionData
            .copy(
              communityBuildingsScheduleUpscanInitialization = Some(upscanInitiateResponse),
              communityBuildingsScheduleFileUploadReference =
                Some(FileUploadReference(upscanInitiateResponse.reference))
            )
        )
    } yield upscanInitiateResponse

  val onUploadSuccess: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    claimsValidationService
      .getFileUploadReference(ValidationType.CommunityBuildings, acceptAwaitingUpload = true)
      .flatMap {
        case Some(fileUploadReference) =>
          claimsValidationService
            .updateUploadStatus(
              claimId = request.sessionData.unsubmittedClaimId.get,
              reference = fileUploadReference,
              CommunityBuildings
            )
            .map(_ =>
              // TODO: when available
              // Redirect(routes.YourCommunityBuildingsScheduleUploadController.onPageLoad)
              Redirect(routes.UploadCommunityBuildingsScheduleController.onPageLoad)
            )

        case None =>
          Future.successful(Redirect(routes.UploadCommunityBuildingsScheduleController.onPageLoad))

      }
  }

  val onUploadError: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    val errorCode = request.getQueryString("errorCode")
    request.sessionData.communityBuildingsScheduleUpscanInitialization match {
      case Some(upscanInitiateResponse) =>
        Future.successful(
          BadRequest(
            view(
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
        Future.successful(Redirect(routes.UploadCommunityBuildingsScheduleController.onPageLoad))
    }

  }

}

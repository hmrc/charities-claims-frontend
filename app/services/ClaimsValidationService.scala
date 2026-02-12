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

package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import connectors.ClaimsValidationConnector
import uk.gov.hmrc.http.HeaderCarrier
import models.*
import models.requests.DataRequest

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[ClaimsValidationServiceImpl])
trait ClaimsValidationService {

  def getFileUploadReference(
    validationType: ValidationType,
    acceptAwaitingUpload: Boolean = false
  )(using DataRequest[?], HeaderCarrier): Future[Option[FileUploadReference]]

  def createUploadTracking(claimId: String, request: CreateUploadTrackingRequest)(using HeaderCarrier): Future[Boolean]
  def getUploadSummary(claimId: String)(using HeaderCarrier): Future[GetUploadSummaryResponse]
  def updateUploadStatus(claimId: String, reference: FileUploadReference)(using
    request: DataRequest[?],
    hc: HeaderCarrier
  ): Future[Boolean]

  def getGiftAidScheduleData(using DataRequest[?], HeaderCarrier): Future[GiftAidScheduleData]
  def getOtherIncomeScheduleData(using DataRequest[?], HeaderCarrier): Future[OtherIncomeScheduleData]
  def getCommunityBuildingsScheduleData(using DataRequest[?], HeaderCarrier): Future[CommunityBuildingsScheduleData]
  def getConnectedCharitiesScheduleData(using DataRequest[?], HeaderCarrier): Future[ConnectedCharitiesScheduleData]

  def deleteGiftAidSchedule(using DataRequest[?], HeaderCarrier): Future[Unit]
  def deleteOtherIncomeSchedule(using DataRequest[?], HeaderCarrier): Future[Unit]
  def deleteCommunityBuildingsSchedule(using DataRequest[?], HeaderCarrier): Future[Unit]
  def deleteConnectedCharitiesSchedule(using DataRequest[?], HeaderCarrier): Future[Unit]
}

@Singleton
class ClaimsValidationServiceImpl @Inject() (
  saveService: SaveService,
  claimsValidationConnector: ClaimsValidationConnector
)(using ec: ExecutionContext)
    extends ClaimsValidationService {

  override def createUploadTracking(claimId: String, request: CreateUploadTrackingRequest)(using
    HeaderCarrier
  ): Future[Boolean] =
    claimsValidationConnector.createUploadTracking(claimId, request)

  override def getUploadSummary(claimId: String)(using HeaderCarrier): Future[GetUploadSummaryResponse] =
    claimsValidationConnector.getUploadSummary(claimId)

  override def updateUploadStatus(claimId: String, reference: FileUploadReference)(using
    request: DataRequest[?],
    hc: HeaderCarrier
  ): Future[Boolean] =
    saveService
      .save(request.sessionData.copy(giftAidScheduleUpscanInitialization = None))
      .flatMap { _ =>
        claimsValidationConnector.updateUploadStatus(claimId, reference, FileStatus.VERIFYING)
      }

  private def geUpscanInitializationFromSession(validationType: ValidationType)(using
    request: DataRequest[?]
  ): Future[Option[UpscanInitiateResponse]] =
    validationType match {
      case ValidationType.GiftAid            =>
        Future.successful(request.sessionData.giftAidScheduleUpscanInitialization)
      case ValidationType.OtherIncome        =>
        Future.successful(request.sessionData.otherIncomeScheduleUpscanInitialization)
      case ValidationType.CommunityBuildings =>
        Future.successful(request.sessionData.communityBuildingsScheduleUpscanInitialization)
      case ValidationType.ConnectedCharities =>
        Future.successful(request.sessionData.connectedCharitiesScheduleUpscanInitialization)
    }

  private def getFileUploadReferenceFromSession(validationType: ValidationType)(using
    request: DataRequest[?]
  ): Future[Option[FileUploadReference]] =
    validationType match {
      case ValidationType.GiftAid            =>
        Future.successful(request.sessionData.giftAidScheduleFileUploadReference)
      case ValidationType.OtherIncome        =>
        Future.successful(request.sessionData.otherIncomeScheduleFileUploadReference)
      case ValidationType.CommunityBuildings =>
        Future.successful(request.sessionData.communityBuildingsScheduleFileUploadReference)
      case ValidationType.ConnectedCharities =>
        Future.successful(request.sessionData.connectedCharitiesScheduleFileUploadReference)
    }

  override def getFileUploadReference(
    validationType: ValidationType,
    acceptAwaitingUpload: Boolean = false
  )(using
    request: DataRequest[?],
    hc: HeaderCarrier
  ): Future[Option[FileUploadReference]] =
    getFileUploadReferenceFromSession(validationType)
      .flatMap {
        case Some(reference) => Future.successful(Some(reference))
        case None            =>
          claimsValidationConnector
            .getUploadSummary(request.sessionData.unsubmittedClaimId.get)
            .flatMap { summaryResponse =>
              summaryResponse.uploads.find(_.validationType == validationType) match {
                case None         => Future.successful(None)
                case Some(upload) =>
                  if acceptAwaitingUpload || upload.fileStatus != FileStatus.AWAITING_UPLOAD
                  then
                    saveService
                      .save(validationType match {
                        case ValidationType.GiftAid            =>
                          request.sessionData.copy(giftAidScheduleFileUploadReference = Some(upload.reference))
                        case ValidationType.OtherIncome        =>
                          request.sessionData.copy(otherIncomeScheduleFileUploadReference = Some(upload.reference))
                        case ValidationType.CommunityBuildings =>
                          request.sessionData
                            .copy(communityBuildingsScheduleFileUploadReference = Some(upload.reference))
                        case ValidationType.ConnectedCharities =>
                          request.sessionData
                            .copy(connectedCharitiesScheduleFileUploadReference = Some(upload.reference))
                      })
                      .map(_ => Some(upload.reference))
                  else Future.successful(None)
              }
            }
            .recoverWith {
              case e: Exception if e.getMessage.contains("CLAIM_DOES_NOT_EXIST") =>
                geUpscanInitializationFromSession(validationType)
                  .flatMap {
                    case Some(upscanInitiateResponse) =>
                      Future.successful(Some(FileUploadReference(upscanInitiateResponse.reference)))
                    case None                         =>
                      Future.successful(None)
                  }
            }
      }

  override def getGiftAidScheduleData(using request: DataRequest[?], hc: HeaderCarrier): Future[GiftAidScheduleData] =
    request.sessionData.giftAidScheduleData match {
      case Some(data) => Future.successful(data)
      case None       =>
        request.sessionData.unsubmittedClaimId match {
          case None =>
            Future.failed(new RuntimeException("No claimId found when attempting to get GiftAid schedule data"))

          case Some(claimId) =>
            getFileUploadReference(ValidationType.GiftAid)
              .flatMap {
                case None =>
                  Future.failed(new ScheduleUploadNotFoundException(ValidationType.GiftAid))

                case Some(fileUploadReference) =>
                  claimsValidationConnector
                    .getUploadResult(claimId, fileUploadReference)
                    .flatMap {
                      case GetUploadResultValidatedGiftAid(reference, data) =>
                        saveService
                          .save(request.sessionData.copy(giftAidScheduleData = Some(data)))
                          .map(_ => data)

                      case other =>
                        Future.failed(
                          new RuntimeException(
                            s"No Gift Aid schedule data found, upload file status is ${other.fileStatus}"
                          )
                        )
                    }
              }
        }
    }

  override def getOtherIncomeScheduleData(using
    request: DataRequest[?],
    hc: HeaderCarrier
  ): Future[OtherIncomeScheduleData] =
    request.sessionData.otherIncomeScheduleData match {
      case Some(data) => Future.successful(data)
      case None       =>
        request.sessionData.unsubmittedClaimId match {
          case None =>
            Future.failed(new RuntimeException("No claimId found when attempting to get Other Income schedule data"))

          case Some(claimId) =>
            getFileUploadReference(ValidationType.OtherIncome).flatMap {
              case None =>
                Future.failed(new ScheduleUploadNotFoundException(ValidationType.OtherIncome))

              case Some(fileUploadReference) =>
                claimsValidationConnector
                  .getUploadResult(claimId, fileUploadReference)
                  .flatMap {
                    case GetUploadResultValidatedOtherIncome(reference, data) =>
                      saveService
                        .save(request.sessionData.copy(otherIncomeScheduleData = Some(data)))
                        .map(_ => data)

                    case other =>
                      Future.failed(
                        new RuntimeException(
                          s"No Other Income schedule data found, upload file status is ${other.fileStatus}"
                        )
                      )
                  }
            }
        }
    }

  override def getCommunityBuildingsScheduleData(using
    request: DataRequest[?],
    hc: HeaderCarrier
  ): Future[CommunityBuildingsScheduleData] =
    request.sessionData.communityBuildingsScheduleData match {
      case Some(data) => Future.successful(data)
      case None       =>
        request.sessionData.unsubmittedClaimId match {
          case None =>
            Future.failed(
              new RuntimeException("No claimId found when attempting to get Community Buildings schedule data")
            )

          case Some(claimId) =>
            getFileUploadReference(ValidationType.CommunityBuildings)
              .flatMap {
                case None =>
                  Future.failed(new ScheduleUploadNotFoundException(ValidationType.CommunityBuildings))

                case Some(fileUploadReference) =>
                  claimsValidationConnector
                    .getUploadResult(claimId, fileUploadReference)
                    .flatMap {

                      case GetUploadResultValidatedCommunityBuildings(reference, data) =>
                        saveService
                          .save(request.sessionData.copy(communityBuildingsScheduleData = Some(data)))
                          .map(_ => data)

                      case other =>
                        Future.failed(
                          new RuntimeException(
                            s"No Community Buildings schedule data found, upload file status is ${other.fileStatus}"
                          )
                        )
                    }
              }
        }
    }

  override def getConnectedCharitiesScheduleData(using
    request: DataRequest[?],
    hc: HeaderCarrier
  ): Future[ConnectedCharitiesScheduleData] =
    request.sessionData.connectedCharitiesScheduleData match {
      case Some(data) => Future.successful(data)
      case None       =>
        request.sessionData.unsubmittedClaimId match {
          case None =>
            Future.failed(
              new RuntimeException("No claimId found when attempting to get Connected Charities schedule data")
            )

          case Some(claimId) =>
            getFileUploadReference(ValidationType.ConnectedCharities)
              .flatMap {
                case None =>
                  Future.failed(new ScheduleUploadNotFoundException(ValidationType.ConnectedCharities))

                case Some(fileUploadReference) =>
                  claimsValidationConnector
                    .getUploadResult(claimId, fileUploadReference)
                    .flatMap {
                      case GetUploadResultValidatedConnectedCharities(reference, data) =>
                        saveService
                          .save(request.sessionData.copy(connectedCharitiesScheduleData = Some(data)))
                          .map(_ => data)

                      case other =>
                        Future.failed(
                          new RuntimeException(
                            s"No Connected Charities schedule data found, upload file status is ${other.fileStatus}"
                          )
                        )
                    }
              }
        }
    }

  override def deleteGiftAidSchedule(using request: DataRequest[?], hc: HeaderCarrier): Future[Unit] =
    deleteSchedule(ValidationType.GiftAid, request.sessionData.unsubmittedClaimId)

  override def deleteOtherIncomeSchedule(using request: DataRequest[?], hc: HeaderCarrier): Future[Unit] =
    deleteSchedule(ValidationType.OtherIncome, request.sessionData.unsubmittedClaimId)

  override def deleteCommunityBuildingsSchedule(using request: DataRequest[?], hc: HeaderCarrier): Future[Unit] =
    deleteSchedule(ValidationType.CommunityBuildings, request.sessionData.unsubmittedClaimId)

  override def deleteConnectedCharitiesSchedule(using request: DataRequest[?], hc: HeaderCarrier): Future[Unit] =
    deleteSchedule(ValidationType.ConnectedCharities, request.sessionData.unsubmittedClaimId)

  private def deleteSchedule(validationType: ValidationType, claimIdOpt: Option[String])(using
    request: DataRequest[?],
    hc: HeaderCarrier
  ): Future[Unit] =
    claimIdOpt match {
      case Some(claimId) =>
        claimsValidationConnector
          .getUploadSummary(claimId)
          .flatMap { summaryResponse =>
            summaryResponse.uploads.find(_.validationType == validationType) match {
              case Some(upload) =>
                claimsValidationConnector
                  .deleteSchedule(claimId, upload.reference)
                  .map(_ => ())

              case None =>
                Future.failed(
                  new RuntimeException(s"No $validationType schedule upload found for claimId: $claimId")
                )
            }
          }
          .flatMap { case _ =>
            val modifiedSessionData = validationType match {
              case ValidationType.GiftAid            =>
                request.sessionData.copy(
                  giftAidScheduleFileUploadReference = None,
                  giftAidScheduleData = None
                )
              case ValidationType.OtherIncome        =>
                request.sessionData.copy(
                  otherIncomeScheduleFileUploadReference = None,
                  otherIncomeScheduleData = None
                )
              case ValidationType.CommunityBuildings =>
                request.sessionData.copy(
                  communityBuildingsScheduleFileUploadReference = None,
                  communityBuildingsScheduleData = None
                )
              case ValidationType.ConnectedCharities =>
                request.sessionData.copy(
                  connectedCharitiesScheduleFileUploadReference = None,
                  connectedCharitiesScheduleData = None
                )
            }
            saveService.save(modifiedSessionData)
          }

      case None =>
        Future.failed(
          new RuntimeException(s"No claimId found when attempting to delete $validationType schedule")
        )
    }
}

case class ScheduleUploadNotFoundException(validationType: ValidationType)
    extends RuntimeException(s"No $validationType schedule found")

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
  def getUploadResult(claimId: String, reference: FileUploadReference)(using
    hc: HeaderCarrier
  ): Future[GetUploadResultResponse]
  def updateUploadStatus(claimId: String, reference: FileUploadReference, validationType: ValidationType)(using
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

  override def getUploadResult(claimId: String, reference: FileUploadReference)(using
    hc: HeaderCarrier
  ): Future[GetUploadResultResponse] =
    claimsValidationConnector.getUploadResult(claimId, reference)

  override def updateUploadStatus(claimId: String, reference: FileUploadReference, validationType: ValidationType)(using
    request: DataRequest[?],
    hc: HeaderCarrier
  ): Future[Boolean] =
    saveService
      .save(validationType match {
        case ValidationType.GiftAid            =>
          request.sessionData.copy(giftAidScheduleUpscanInitialization = None)
        case ValidationType.OtherIncome        =>
          request.sessionData.copy(otherIncomeScheduleUpscanInitialization = None)
        case ValidationType.CommunityBuildings =>
          request.sessionData
            .copy(communityBuildingsScheduleUpscanInitialization = None)
        case ValidationType.ConnectedCharities =>
          request.sessionData
            .copy(connectedCharitiesScheduleUpscanInitialization = None)
      })
      .flatMap { _ =>
        claimsValidationConnector.updateUploadStatus(claimId, reference, FileStatus.VERIFYING)
      }

  private def getUpscanInitializationFromSession(validationType: ValidationType)(using
    request: DataRequest[?]
  ): Option[UpscanInitiateResponse] =
    validationType match {
      case ValidationType.GiftAid            =>
        request.sessionData.giftAidScheduleUpscanInitialization
      case ValidationType.OtherIncome        =>
        request.sessionData.otherIncomeScheduleUpscanInitialization
      case ValidationType.CommunityBuildings =>
        request.sessionData.communityBuildingsScheduleUpscanInitialization
      case ValidationType.ConnectedCharities =>
        request.sessionData.connectedCharitiesScheduleUpscanInitialization
    }

  private def getFileUploadReferenceFromSession(validationType: ValidationType)(using
    request: DataRequest[?]
  ): Option[FileUploadReference] =
    validationType match {
      case ValidationType.GiftAid            =>
        request.sessionData.giftAidScheduleFileUploadReference
      case ValidationType.OtherIncome        =>
        request.sessionData.otherIncomeScheduleFileUploadReference
      case ValidationType.CommunityBuildings =>
        request.sessionData.communityBuildingsScheduleFileUploadReference
      case ValidationType.ConnectedCharities =>
        request.sessionData.connectedCharitiesScheduleFileUploadReference
    }

  override def getFileUploadReference(
    validationType: ValidationType,
    acceptAwaitingUpload: Boolean = false
  )(using
    request: DataRequest[?],
    hc: HeaderCarrier
  ): Future[Option[FileUploadReference]] =
    getFileUploadReferenceFromSession(validationType).match {
      case Some(reference) =>
        if acceptAwaitingUpload || getUpscanInitializationFromSession(validationType).isEmpty
        then Future.successful(Some(reference))
        else Future.successful(None)

      case None =>
        claimsValidationConnector
          .getUploadSummary(request.sessionData.unsubmittedClaimId.get)
          .flatMap { summaryResponse =>
            summaryResponse.findUpload(validationType) match {
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
                else
                  upload.asUpscanInitiateResponse match {
                    // restore upscan initiate response from upload summary
                    case Some(upscanInitiateResponse) =>
                      saveService
                        .save(validationType match {
                          case ValidationType.GiftAid            =>
                            request.sessionData.copy(
                              giftAidScheduleFileUploadReference = Some(upload.reference),
                              giftAidScheduleUpscanInitialization = Some(upscanInitiateResponse)
                            )
                          case ValidationType.OtherIncome        =>
                            request.sessionData.copy(
                              otherIncomeScheduleFileUploadReference = Some(upload.reference),
                              otherIncomeScheduleUpscanInitialization = Some(upscanInitiateResponse)
                            )
                          case ValidationType.CommunityBuildings =>
                            request.sessionData.copy(
                              communityBuildingsScheduleFileUploadReference = Some(upload.reference),
                              giftAidScheduleUpscanInitialization = Some(upscanInitiateResponse)
                            )
                          case ValidationType.ConnectedCharities =>
                            request.sessionData
                              .copy(
                                connectedCharitiesScheduleFileUploadReference = Some(upload.reference),
                                connectedCharitiesScheduleUpscanInitialization = Some(upscanInitiateResponse)
                              )
                        })
                        .map(_ => Some(upload.reference))

                    case None =>
                      Future.successful(None)
                  }
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

                      case _ =>
                        Future.failed(
                          new ScheduleUploadNotFoundException(ValidationType.GiftAid)
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

                    case _ =>
                      Future.failed(new ScheduleUploadNotFoundException(ValidationType.OtherIncome))
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

                      case _ =>
                        Future.failed(new ScheduleUploadNotFoundException(ValidationType.OtherIncome))
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

                      case _ =>
                        Future.failed(new ScheduleUploadNotFoundException(ValidationType.OtherIncome))
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
                  giftAidScheduleUpscanInitialization = None,
                  giftAidScheduleFileUploadReference = None,
                  giftAidScheduleData = None,
                  giftAidScheduleCompleted = false
                )
              case ValidationType.OtherIncome        =>
                request.sessionData.copy(
                  otherIncomeScheduleUpscanInitialization = None,
                  otherIncomeScheduleFileUploadReference = None,
                  otherIncomeScheduleData = None,
                  otherIncomeScheduleCompleted = false
                )
              case ValidationType.CommunityBuildings =>
                request.sessionData.copy(
                  communityBuildingsScheduleUpscanInitialization = None,
                  communityBuildingsScheduleFileUploadReference = None,
                  communityBuildingsScheduleData = None,
                  communityBuildingsScheduleCompleted = false
                )
              case ValidationType.ConnectedCharities =>
                request.sessionData.copy(
                  connectedCharitiesScheduleUpscanInitialization = None,
                  connectedCharitiesScheduleFileUploadReference = None,
                  connectedCharitiesScheduleData = None,
                  connectedCharitiesScheduleCompleted = false
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

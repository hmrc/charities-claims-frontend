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

package models

import play.api.libs.json.*

sealed trait GetUploadResultResponse {
  def reference: FileUploadReference
  def validationType: ValidationType
  def fileStatus: FileStatus
}

final case class GetUploadResultAwaitingUpload(
  reference: FileUploadReference,
  validationType: ValidationType,
  uploadUrl: String
) extends GetUploadResultResponse {
  def fileStatus: FileStatus = FileStatus.AWAITING_UPLOAD
}

object GetUploadResultAwaitingUpload {
  given Format[GetUploadResultAwaitingUpload] = Json.format[GetUploadResultAwaitingUpload]
}

final case class GetUploadResultVeryfying(
  reference: FileUploadReference,
  validationType: ValidationType
) extends GetUploadResultResponse {
  def fileStatus: FileStatus = FileStatus.VERIFYING
}

object GetUploadResultVeryfying {
  given Format[GetUploadResultVeryfying] = Json.format[GetUploadResultVeryfying]
}

case class GetUploadResultFailureDetails(
  failureReason: String,
  message: String
)

object GetUploadResultFailureDetails {
  given Format[GetUploadResultFailureDetails] = Json.format[GetUploadResultFailureDetails]
}

final case class GetUploadResultVeryficationFailed(
  reference: FileUploadReference,
  validationType: ValidationType,
  failureDetails: GetUploadResultFailureDetails
) extends GetUploadResultResponse {
  def fileStatus: FileStatus = FileStatus.VERIFICATION_FAILED
}

object GetUploadResultVeryficationFailed {
  given Format[GetUploadResultVeryficationFailed] = Json.format[GetUploadResultVeryficationFailed]
}

final case class GetUploadResultValidating(
  reference: FileUploadReference,
  validationType: ValidationType
) extends GetUploadResultResponse {
  def fileStatus: FileStatus = FileStatus.VALIDATING
}

object GetUploadResultValidating {
  given Format[GetUploadResultValidating] = Json.format[GetUploadResultValidating]
}

final case class GetUploadResultValidatedGiftAid(
  reference: FileUploadReference,
  giftAidScheduleData: GiftAidScheduleData
) extends GetUploadResultResponse {
  def fileStatus: FileStatus         = FileStatus.VALIDATED
  def validationType: ValidationType = ValidationType.GiftAid
}

object GetUploadResultValidatedGiftAid {
  given Format[GetUploadResultValidatedGiftAid] = Json.format[GetUploadResultValidatedGiftAid]
}

final case class GetUploadResultValidatedOtherIncome(
  reference: FileUploadReference,
  otherIncomeData: OtherIncomeScheduleData
) extends GetUploadResultResponse {
  def fileStatus: FileStatus         = FileStatus.VALIDATED
  def validationType: ValidationType = ValidationType.OtherIncome
}

object GetUploadResultValidatedOtherIncome {
  given Format[GetUploadResultValidatedOtherIncome] =
    Json.format[GetUploadResultValidatedOtherIncome]
}

final case class GetUploadResultValidatedCommunityBuildings(
  reference: FileUploadReference,
  communityBuildingsData: CommunityBuildingsScheduleData
) extends GetUploadResultResponse {
  def fileStatus: FileStatus         = FileStatus.VALIDATED
  def validationType: ValidationType = ValidationType.CommunityBuildings
}

object GetUploadResultValidatedCommunityBuildings {
  given Format[GetUploadResultValidatedCommunityBuildings] =
    Json.format[GetUploadResultValidatedCommunityBuildings]
}

final case class GetUploadResultValidatedConnectedCharities(
  reference: FileUploadReference,
  connectedCharitiesData: ConnectedCharitiesScheduleData
) extends GetUploadResultResponse {
  def fileStatus: FileStatus         = FileStatus.VALIDATED
  def validationType: ValidationType = ValidationType.ConnectedCharities
}

object GetUploadResultValidatedConnectedCharities {
  given Format[GetUploadResultValidatedConnectedCharities] =
    Json.format[GetUploadResultValidatedConnectedCharities]
}

case class ValidationError(
  field: String,
  error: String
)

object ValidationError {
  given Format[ValidationError] = Json.format[ValidationError]
}

final case class GetUploadResultValidationFailedGiftAid(
  reference: FileUploadReference,
  giftAidScheduleData: GiftAidScheduleData,
  errors: Seq[ValidationError]
) extends GetUploadResultResponse {
  def fileStatus: FileStatus         = FileStatus.VALIDATION_FAILED
  def validationType: ValidationType = ValidationType.GiftAid
}

object GetUploadResultValidationFailedGiftAid {
  given Format[GetUploadResultValidationFailedGiftAid] =
    Json.format[GetUploadResultValidationFailedGiftAid]
}

final case class GetUploadResultValidationFailedOtherIncome(
  reference: FileUploadReference,
  otherIncomeData: OtherIncomeScheduleData,
  errors: Seq[ValidationError]
) extends GetUploadResultResponse {
  def fileStatus: FileStatus         = FileStatus.VALIDATION_FAILED
  def validationType: ValidationType = ValidationType.OtherIncome
}

object GetUploadResultValidationFailedOtherIncome {
  given Format[GetUploadResultValidationFailedOtherIncome] =
    Json.format[GetUploadResultValidationFailedOtherIncome]
}

final case class GetUploadResultValidationFailedCommunityBuildings(
  reference: FileUploadReference,
  communityBuildingsData: CommunityBuildingsScheduleData,
  errors: Seq[ValidationError]
) extends GetUploadResultResponse {
  def fileStatus: FileStatus         = FileStatus.VALIDATION_FAILED
  def validationType: ValidationType = ValidationType.CommunityBuildings
}

object GetUploadResultValidationFailedCommunityBuildings {
  given Format[GetUploadResultValidationFailedCommunityBuildings] =
    Json.format[GetUploadResultValidationFailedCommunityBuildings]
}

final case class GetUploadResultValidationFailedConnectedCharities(
  reference: FileUploadReference,
  connectedCharitiesData: ConnectedCharitiesScheduleData,
  errors: Seq[ValidationError]
) extends GetUploadResultResponse {
  def fileStatus: FileStatus         = FileStatus.VALIDATION_FAILED
  def validationType: ValidationType = ValidationType.ConnectedCharities
}

object GetUploadResultValidationFailedConnectedCharities {
  given Format[GetUploadResultValidationFailedConnectedCharities] =
    Json.format[GetUploadResultValidationFailedConnectedCharities]
}

object GetUploadResultResponse {
  given Format[GetUploadResultResponse] =
    Format(getUploadResultResponseReads, getUploadResultResponseWrites)

  val getUploadResultResponseReads: Reads[GetUploadResultResponse] =
    Reads {
      case obj @ JsObject(fields) =>
        fields.get("fileStatus").flatMap(_.asOpt[FileStatus]) match {
          case None =>
            JsError(
              s"Cannot parse GetUploadResultResponse because valid fileStatus is required, got ${fields.get("fileStatus").map(_.toString).getOrElse("none")}"
            )

          case Some(FileStatus.AWAITING_UPLOAD)     => Json.reads[GetUploadResultAwaitingUpload].reads(obj)
          case Some(FileStatus.VERIFYING)           => Json.reads[GetUploadResultVeryfying].reads(obj)
          case Some(FileStatus.VERIFICATION_FAILED) => Json.reads[GetUploadResultVeryficationFailed].reads(obj)
          case Some(FileStatus.VALIDATING)          => Json.reads[GetUploadResultValidating].reads(obj)

          case Some(FileStatus.VALIDATED) =>
            fields.get("validationType").flatMap(_.asOpt[ValidationType]) match {
              case None =>
                JsError(
                  s"Cannot parse GetUploadResultResponse because valid validationType is required, got ${fields.get("validationType").map(_.toString).getOrElse("none")}"
                )

              case Some(ValidationType.GiftAid)            =>
                Json.reads[GetUploadResultValidatedGiftAid].reads(obj)
              case Some(ValidationType.OtherIncome)        =>
                Json.reads[GetUploadResultValidatedOtherIncome].reads(obj)
              case Some(ValidationType.CommunityBuildings) =>
                Json.reads[GetUploadResultValidatedCommunityBuildings].reads(obj)
              case Some(ValidationType.ConnectedCharities) =>
                Json.reads[GetUploadResultValidatedConnectedCharities].reads(obj)
            }

          case Some(FileStatus.VALIDATION_FAILED) =>
            fields.get("validationType").flatMap(_.asOpt[ValidationType]) match {
              case None =>
                JsError(
                  s"Cannot parse GetUploadResultResponse because valid validationType is required, got ${fields.get("validationType").map(_.toString).getOrElse("none")}"
                )

              case Some(ValidationType.GiftAid)            =>
                Json.reads[GetUploadResultValidationFailedGiftAid].reads(obj)
              case Some(ValidationType.OtherIncome)        =>
                Json.reads[GetUploadResultValidationFailedOtherIncome].reads(obj)
              case Some(ValidationType.CommunityBuildings) =>
                Json.reads[GetUploadResultValidationFailedCommunityBuildings].reads(obj)
              case Some(ValidationType.ConnectedCharities) =>
                Json.reads[GetUploadResultValidationFailedConnectedCharities].reads(obj)
            }
        }

      case other =>
        JsError(
          s"Cannot parse GetUploadResultResponse because of invalid json value, expected json object, got $other"
        )
    }

  extension (jsValue: JsValue) {
    def addFileStatus(result: GetUploadResultResponse): JsValue =
      jsValue match {
        case JsObject(fields) => JsObject(fields ++ Seq("fileStatus" -> Json.toJson(result.fileStatus)))
        case other            => other
      }

    def addFileStatusAndValidationType(result: GetUploadResultResponse): JsValue =
      jsValue match {
        case JsObject(fields) =>
          JsObject(
            fields ++ Seq(
              "fileStatus"     -> Json.toJson(result.fileStatus),
              "validationType" -> Json.toJson(result.validationType)
            )
          )
        case other            => other
      }
  }

  val getUploadResultResponseWrites: Writes[GetUploadResultResponse] =
    Writes {
      case result: GetUploadResultAwaitingUpload                     =>
        Json.toJson(result).addFileStatus(result)
      case result: GetUploadResultVeryfying                          =>
        Json.toJson(result).addFileStatus(result)
      case result: GetUploadResultVeryficationFailed                 =>
        Json.toJson(result).addFileStatus(result)
      case result: GetUploadResultValidating                         =>
        Json.toJson(result).addFileStatus(result)
      case result: GetUploadResultValidatedGiftAid                   =>
        Json.toJson(result).addFileStatusAndValidationType(result)
      case result: GetUploadResultValidatedOtherIncome               =>
        Json.toJson(result).addFileStatusAndValidationType(result)
      case result: GetUploadResultValidatedCommunityBuildings        =>
        Json.toJson(result).addFileStatusAndValidationType(result)
      case result: GetUploadResultValidatedConnectedCharities        =>
        Json.toJson(result).addFileStatusAndValidationType(result)
      case result: GetUploadResultValidationFailedGiftAid            =>
        Json.toJson(result).addFileStatusAndValidationType(result)
      case result: GetUploadResultValidationFailedOtherIncome        =>
        Json.toJson(result).addFileStatusAndValidationType(result)
      case result: GetUploadResultValidationFailedCommunityBuildings =>
        Json.toJson(result).addFileStatusAndValidationType(result)
      case result: GetUploadResultValidationFailedConnectedCharities =>
        Json.toJson(result).addFileStatusAndValidationType(result)
    }
}

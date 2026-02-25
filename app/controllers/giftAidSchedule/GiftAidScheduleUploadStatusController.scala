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

import services.ClaimsValidationService
import play.api.mvc.*
import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.Actions
import play.api.libs.json.Json
import models.*
import play.api.i18n.MessagesApi

import scala.concurrent.{ExecutionContext, Future}

class GiftAidScheduleUploadStatusController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  claimsValidationService: ClaimsValidationService,
  messagesApi: MessagesApi
)(using ec: ExecutionContext)
    extends BaseController {

  val status: Action[AnyContent] =
    actions
      .authAndGetData()
      .async { implicit request =>
        {
          for {
            claimId             <- request.sessionData.unsubmittedClaimId
            _                   <- request.sessionData.repaymentClaimDetailsAnswers
                                     .flatMap(_.claimingGiftAid)
                                     .flatMap(checkbox => if (checkbox) Some(()) else None)
            fileUploadReference <- request.sessionData.giftAidScheduleFileUploadReference
          } yield claimsValidationService
            .getUploadResult(claimId, fileUploadReference)
            .map { uploadResult =>
              Ok(
                Json.obj(
                  "uploadStatus" -> Json
                    .toJson(messagesApi.preferred(request)(s"fileUpload.status.${uploadResult.fileStatus}")),
                  "isFinal"      -> Json.toJson(uploadResult.fileStatus.isFinal)
                )
              )
            }
            .recover {
              case e: Exception if e.getMessage.contains("CLAIM_REFERENCE_DOES_NOT_EXIST") =>
                BadRequest

              case _ =>
                InternalServerError
            }
        }.getOrElse(Future.successful(BadRequest))

      }
}

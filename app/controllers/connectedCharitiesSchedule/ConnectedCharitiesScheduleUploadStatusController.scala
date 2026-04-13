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

package controllers.connectedCharitiesSchedule

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.Actions
import models.*
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.*
import services.ClaimsValidationService

import scala.concurrent.{ExecutionContext, Future}

class ConnectedCharitiesScheduleUploadStatusController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  claimsValidationService: ClaimsValidationService,
  messagesApi: MessagesApi
)(using ec: ExecutionContext)
    extends BaseController {

  val status: Action[AnyContent] =
    actions
      .authAndGetData()
      // .authAndGetDataWithGuard(SessionData.shouldUploadConnectedCharitiesSchedule)
      .async { implicit request =>
        {
          for {
            claimId             <- request.sessionData.unsubmittedClaimId
            _                   <- request.sessionData.repaymentClaimDetailsAnswers
                                     .flatMap(_.connectedToAnyOtherCharities)
                                     .flatMap(Option.when(_)(()))
            fileUploadReference <- request.sessionData.connectedCharitiesScheduleFileUploadReference
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

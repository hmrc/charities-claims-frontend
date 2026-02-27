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

import play.api.mvc.*
import com.google.inject.Inject
import controllers.BaseController
import views.html.CheckYourConnectedCharitiesScheduleView
import controllers.actions.Actions
import forms.YesNoFormProvider
import play.api.data.Form
import services.{ClaimsService, ClaimsValidationService, PaginationService}
import controllers.connectedCharitiesSchedule.routes

import scala.concurrent.{ExecutionContext, Future}
import services.SaveService
import models.SessionData

class CheckYourConnectedCharitiesScheduleController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourConnectedCharitiesScheduleView,
  actions: Actions,
  claimsValidationService: ClaimsValidationService,
  formProvider: YesNoFormProvider,
  claimsService: ClaimsService,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean] = formProvider("checkYourConnectedCharitiesSchedule.error.required")

  val onPageLoad: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadConnectedCharitiesSchedule)
      .async { implicit request =>
        claimsValidationService.getConnectedCharitiesScheduleData
          .map { connectedCharitiesScheduleData =>

            val currentPage      = request.getQueryString("page").flatMap(_.toIntOption).getOrElse(1)
            val paginationResult = PaginationService.paginateConnectedCharities(
              allConnectedCharities = connectedCharitiesScheduleData.charities,
              currentPage = currentPage,
              baseUrl = routes.CheckYourConnectedCharitiesScheduleController.onPageLoad.url
            )

            Ok(
              view(
                form = form,
                connectedCharities = paginationResult.paginatedData,
                paginationViewModel = paginationResult.paginationViewModel,
                paginationStatus = paginationResult
              )
            )
          }
      }

  val onSubmit: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadConnectedCharitiesSchedule)
      .async { implicit request =>
        claimsValidationService.getConnectedCharitiesScheduleData
          .flatMap { connectedCharitiesScheduleData =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => {
                  val paginationResult = PaginationService.paginateConnectedCharities(
                    allConnectedCharities = connectedCharitiesScheduleData.charities,
                    currentPage = 1,
                    baseUrl = routes.CheckYourConnectedCharitiesScheduleController.onPageLoad.url
                  )

                  Future.successful(
                    BadRequest(
                      view(
                        form = formWithErrors,
                        connectedCharities = paginationResult.paginatedData,
                        paginationViewModel = paginationResult.paginationViewModel,
                        paginationStatus = paginationResult
                      )
                    )
                  )
                },
                answer =>
                  answer match {
                    case true =>
                      Future.successful(Redirect(routes.UpdateConnectedCharitiesScheduleController.onPageLoad))

                    case false =>
                      if request.sessionData.connectedCharitiesScheduleCompleted
                      then {
                        Future.successful(Redirect(controllers.routes.ClaimsTaskListController.onPageLoad))
                      } else {
                        for {
                          _ <- saveService.save(
                                 request.sessionData.copy(
                                   connectedCharitiesScheduleCompleted = true,
                                   connectedCharitiesScheduleData = None
                                 )
                               )
                          _ <- claimsService.save
                        } yield Redirect(routes.ConnectedCharitiesScheduleUploadSuccessfulController.onPageLoad)
                      }
                  }
              )

          }
      }

}

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

package controllers.communityBuildingsSchedule

import play.api.mvc.*
import com.google.inject.Inject
import controllers.BaseController
import views.html.CheckYourCommunityBuildingsScheduleView
import controllers.actions.Actions
import forms.YesNoFormProvider
import play.api.data.Form
import services.{ClaimsService, ClaimsValidationService, PaginationService}
import controllers.communityBuildingsSchedule.routes

import scala.concurrent.{ExecutionContext, Future}
import services.SaveService
import models.SessionData

class CheckYourCommunityBuildingsScheduleController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourCommunityBuildingsScheduleView,
  actions: Actions,
  claimsValidationService: ClaimsValidationService,
  formProvider: YesNoFormProvider,
  claimsService: ClaimsService,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean] = formProvider("checkYourCommunityBuildingsSchedule.error.required")

  val onPageLoad: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadCommunityBuildingsSchedule)
      .async { implicit request =>
        claimsValidationService.getCommunityBuildingsScheduleData
          .map { communityBuildingsScheduleData =>

            val currentPage      = request.getQueryString("page").flatMap(_.toIntOption).getOrElse(1)
            val paginationResult = PaginationService.paginateCommunityBuildings(
              allCommunityBuildings = communityBuildingsScheduleData.communityBuildings,
              currentPage = currentPage,
              baseUrl = routes.CheckYourCommunityBuildingsScheduleController.onPageLoad.url
            )

            Ok(
              view(
                form = form,
                communityBuildingsScheduleData = communityBuildingsScheduleData,
                communityBuildings = paginationResult.paginatedData,
                paginationViewModel = paginationResult.paginationViewModel,
                paginationStatus = paginationResult
              )
            )
          }
      }

  val onSubmit: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadCommunityBuildingsSchedule)
      .async { implicit request =>
        claimsValidationService.getCommunityBuildingsScheduleData
          .flatMap { communityBuildingsScheduleData =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => {
                  val paginationResult = PaginationService.paginateCommunityBuildings(
                    allCommunityBuildings = communityBuildingsScheduleData.communityBuildings,
                    currentPage = 1,
                    baseUrl = routes.CheckYourCommunityBuildingsScheduleController.onPageLoad.url
                  )

                  Future.successful(
                    BadRequest(
                      view(
                        form = formWithErrors,
                        communityBuildingsScheduleData = communityBuildingsScheduleData,
                        communityBuildings = paginationResult.paginatedData,
                        paginationViewModel = paginationResult.paginationViewModel,
                        paginationStatus = paginationResult
                      )
                    )
                  )
                },
                answer =>
                  answer match {
                    case true =>
                      // TODO: UpdateCommunityBuildingsScheduleController to be added here
                      // Gift Aid equivalent: routes.UpdateGiftAidScheduleController.onPageLoad
                      Future.successful(Redirect(routes.UploadCommunityBuildingsScheduleController.onPageLoad))

                    case false =>
                      if request.sessionData.communityBuildingsScheduleCompleted
                      then {
                        Future.successful(Redirect(controllers.routes.ClaimsTaskListController.onPageLoad))
                      } else {
                        for {
                          _ <- saveService.save(
                                 request.sessionData.copy(
                                   communityBuildingsScheduleCompleted = true,
                                   communityBuildingsScheduleData = None
                                 )
                               )
                          _ <- claimsService.save
                        } yield Redirect(controllers.routes.ClaimsTaskListController.onPageLoad)
                        // TODO: CommunityBuildingsScheduleUploadSuccessfulController to be added here
                        // Gift Aid equivalent: routes.GiftAidScheduleUploadSuccessfulController.onPageLoad
                      }
                  }
              )

          }
      }

}

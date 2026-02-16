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

import play.api.mvc.*
import com.google.inject.Inject
import controllers.BaseController
import views.html.CheckYourGiftAidScheduleView
import controllers.actions.Actions
import forms.YesNoFormProvider
import play.api.data.Form
import services.{ClaimsService, ClaimsValidationService, PaginationService}
import controllers.giftAidSchedule.routes

import scala.concurrent.{ExecutionContext, Future}
import services.SaveService
import models.SessionData

class CheckYourGiftAidScheduleController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourGiftAidScheduleView,
  actions: Actions,
  claimsValidationService: ClaimsValidationService,
  formProvider: YesNoFormProvider,
  claimsService: ClaimsService,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean] = formProvider("checkYourGiftAidSchedule.error.required")

  val onPageLoad: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadGiftAidSchedule)
      .async { implicit request =>
        claimsValidationService.getGiftAidScheduleData
          .map { giftAidScheduleData =>

            val currentPage      = request.getQueryString("page").flatMap(_.toIntOption).getOrElse(1)
            val paginationResult = PaginationService.paginateDonations(
              allDonations = giftAidScheduleData.donations,
              currentPage = currentPage,
              baseUrl = routes.CheckYourGiftAidScheduleController.onPageLoad.url
            )

            Ok(
              view(
                form = form,
                giftAidScheduleData = giftAidScheduleData,
                donations = paginationResult.paginatedData,
                paginationViewModel = paginationResult.paginationViewModel
              )
            )
          }
      }

  val onSubmit: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadGiftAidSchedule)
      .async { implicit request =>
        claimsValidationService.getGiftAidScheduleData
          .flatMap { giftAidScheduleData =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => {
                  val paginationResult = PaginationService.paginateDonations(
                    allDonations = giftAidScheduleData.donations,
                    currentPage = 1,
                    baseUrl = routes.CheckYourGiftAidScheduleController.onPageLoad.url
                  )

                  Future.successful(
                    BadRequest(
                      view(
                        form = formWithErrors,
                        giftAidScheduleData = giftAidScheduleData,
                        donations = paginationResult.paginatedData,
                        paginationViewModel = paginationResult.paginationViewModel
                      )
                    )
                  )
                },
                answer =>
                  answer match {
                    case true =>
                      Future.successful(Redirect(routes.UpdateGiftAidScheduleController.onPageLoad))

                    case false =>
                      if request.sessionData.giftAidScheduleCompleted
                      then {
                        Future.successful(Redirect(controllers.routes.ClaimsTaskListController.onPageLoad))
                      } else {
                        for {
                          _ <- saveService.save(
                                 request.sessionData.copy(
                                   giftAidScheduleCompleted = true,
                                   giftAidScheduleData = None
                                 )
                               )
                          _ <- claimsService.save
                        } yield Redirect(routes.GiftAidScheduleUploadSuccessfulController.onPageLoad)
                      }
                  }
              )

          }
      }

}

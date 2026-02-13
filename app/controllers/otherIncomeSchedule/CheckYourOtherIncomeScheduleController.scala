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

import play.api.mvc.*
import com.google.inject.Inject
import controllers.BaseController
import views.html.CheckYourOtherIncomeScheduleView
import controllers.actions.Actions
import forms.YesNoFormProvider
import play.api.data.Form
import models.SessionData
import services.{ClaimsService, ClaimsValidationService, PaginationService}
import controllers.otherIncomeSchedule.routes

import scala.concurrent.{ExecutionContext, Future}

class CheckYourOtherIncomeScheduleController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourOtherIncomeScheduleView,
  actions: Actions,
  claimsValidationService: ClaimsValidationService,
  formProvider: YesNoFormProvider,
  claimsService: ClaimsService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean] = formProvider("checkYourOtherIncomeSchedule.error.required")

  val onPageLoad: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadOtherIncomeSchedule)
      .async { implicit request =>
        claimsValidationService.getOtherIncomeScheduleData
          .map { otherIncomeScheduleData =>

            val currentPage      = request.getQueryString("page").flatMap(_.toIntOption).getOrElse(1)
            val paginationResult = PaginationService.paginateOtherIncomes(
              allOtherIncomes = otherIncomeScheduleData.otherIncomes,
              currentPage = currentPage,
              baseUrl = routes.CheckYourOtherIncomeScheduleController.onPageLoad.url
            )

            Ok(
              view(
                form = form,
                otherIncomeScheduleData = otherIncomeScheduleData,
                otherIncomes = paginationResult.paginatedData,
                paginationViewModel = paginationResult.paginationViewModel
              )
            )
          }
      }

  val onSubmit: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.shouldUploadOtherIncomeSchedule)
      .async { implicit request =>
        claimsValidationService.getOtherIncomeScheduleData
          .flatMap { otherIncomeScheduleData =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => {
                  val paginationResult = PaginationService.paginateOtherIncomes(
                    allOtherIncomes = otherIncomeScheduleData.otherIncomes,
                    currentPage = 1,
                    baseUrl = routes.CheckYourOtherIncomeScheduleController.onPageLoad.url
                  )

                  Future.successful(
                    BadRequest(
                      view(
                        form = formWithErrors,
                        otherIncomeScheduleData = otherIncomeScheduleData,
                        otherIncomes = paginationResult.paginatedData,
                        paginationViewModel = paginationResult.paginationViewModel
                      )
                    )
                  )
                },
                {
                  case true =>
                    Future.successful(Redirect(routes.UpdateOtherIncomeScheduleController.onPageLoad))

                  case false =>
                    claimsService.save
                      .map(_ => Redirect(routes.OtherIncomeScheduleUploadSuccessfulController.onPageLoad))
                }
              )

          }
      }

}

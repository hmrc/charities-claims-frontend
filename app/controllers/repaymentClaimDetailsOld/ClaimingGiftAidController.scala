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

package controllers.repaymentclaimdetailsold

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.Actions
import forms.YesNoFormProvider
import models.{Mode, RepaymentClaimDetailsAnswersOld}
import models.Mode.*
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ClaimsValidationService, SaveService}
import views.html.ClaimingGiftAidView

import cats.implicits.*

import scala.concurrent.{ExecutionContext, Future}

class ClaimingGiftAidController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  view: ClaimingGiftAidView,
  formProvider: YesNoFormProvider,
  saveService: SaveService,
  claimsValidationService: ClaimsValidationService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean] = formProvider("claimingGiftAid.error.required")

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData() { implicit request =>
    val previousAnswer = RepaymentClaimDetailsAnswersOld.getClaimingGiftAid
    Ok(view(form.withDefault(warningAnswerBoolean.orElse(previousAnswer)), mode, isWarning))
  }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] =
    actions.authAndGetData().async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          claimingGiftAid =>
            if hadNoWarningShown && RepaymentClaimDetailsAnswersOld
                .shouldWarnAboutChangingClaimingGiftAid(claimingGiftAid)
            then
              Future.successful(
                Redirect(routes.ClaimingGiftAidController.onPageLoad(mode))
                  .withWarning(claimingGiftAid.toString)
              )
            else
              claimsValidationService.deleteGiftAidSchedule
                .whenA(warningWasShown && !claimingGiftAid)
                .flatMap { _ =>
                  saveService
                    .save(RepaymentClaimDetailsAnswersOld.setClaimingGiftAid(claimingGiftAid))
                    .map { _ =>
                      if (mode == CheckMode) {
                        Redirect(routes.CheckYourAnswersController.onPageLoad)
                      } else {
                        Redirect(routes.ClaimingOtherIncomeController.onPageLoad(NormalMode))
                      }
                    }
                }
        )
    }
}

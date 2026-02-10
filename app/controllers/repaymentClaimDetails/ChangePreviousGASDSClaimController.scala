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

package controllers.repaymentClaimDetails

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.{Actions, GuardAction}
import controllers.repaymentClaimDetails.routes
import forms.YesNoFormProvider
import models.{Mode, RepaymentClaimDetailsAnswers}
import models.requests.DataRequest
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.SaveService
import views.html.{ChangePreviousGASDSClaimView, UpdateRepaymentClaimView}
import models.Mode.*
import scala.concurrent.{ExecutionContext, Future}

class ChangePreviousGASDSClaimController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: ChangePreviousGASDSClaimView,
  updateRepaymentClaimView: UpdateRepaymentClaimView,
  actions: Actions,
  guard: GuardAction,
  formProvider: YesNoFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean]              = formProvider("changePreviousGASDSClaim.error.required")
  val confirmUpdateForm: Form[Boolean] = formProvider("updateRepaymentClaim.error.required")

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] =
    actions.authAndGetData().andThen(guard(RepaymentClaimDetailsAnswers.isClaimingGASDSWithDonations)).async {
      implicit request =>
        val previousAnswer = RepaymentClaimDetailsAnswers.getMakingAdjustmentToPreviousClaim
        Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))
    }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] =
    actions.authAndGetData().andThen(guard(RepaymentClaimDetailsAnswers.isClaimingGASDSWithDonations)).async {
      implicit request =>
        if (isConfirmingUpdate) {
          handleUpdateConfirmationSubmit(mode)
        } else {
          handleQuestionSubmit(mode)
        }
    }

  def handleQuestionSubmit(mode: Mode)(implicit request: DataRequest[AnyContent]) =
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        newAnswer => {
          val previousAnswer = RepaymentClaimDetailsAnswers.getMakingAdjustmentToPreviousClaim

          if (needsUpdateConfirmation(mode, previousAnswer, newAnswer)) {
            Future.successful(
              Ok(
                updateRepaymentClaimView(
                  confirmUpdateForm,
                  routes.ChangePreviousGASDSClaimController.onSubmit(mode)
                )
              )
            )
          } else {
            saveService
              .save(
                RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(newAnswer)
              )
              .map(_ =>
                Redirect(
                  ChangePreviousGASDSClaimController.nextPage(newAnswer, mode, previousAnswer)
                )
              )
          }
        }
      )

  def handleUpdateConfirmationSubmit(mode: Mode)(implicit request: DataRequest[AnyContent]) =
    confirmUpdateForm
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(
            BadRequest(
              updateRepaymentClaimView(
                formWithErrors,
                routes.ChangePreviousGASDSClaimController.onSubmit(mode)
              )
            )
          ),
        {
          case true =>
            val previousAnswer = RepaymentClaimDetailsAnswers.getMakingAdjustmentToPreviousClaim

            saveService
              .save(
                RepaymentClaimDetailsAnswers.setMakingAdjustmentToPreviousClaim(false)
              )
              .map(_ =>
                Redirect(
                  ChangePreviousGASDSClaimController.nextPage(false, mode, previousAnswer)
                )
              )

          case false =>
            Future.successful(Redirect(routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad))
        }
      )
}

object ChangePreviousGASDSClaimController {

  def nextPage(value: Boolean, mode: Mode, previousAnswer: Option[Boolean]): Call =
    (value, mode, previousAnswer) match {
      // NormalMode
      case (_, NormalMode, _)                                =>
        routes.ConnectedToAnyOtherCharitiesController.onPageLoad(NormalMode)

      // CheckMode : no change
      case (newVal, CheckMode, Some(prev)) if newVal == prev =>
        routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad

      case (_, CheckMode, _) =>
        routes.ConnectedToAnyOtherCharitiesController.onPageLoad(CheckMode)
    }
}

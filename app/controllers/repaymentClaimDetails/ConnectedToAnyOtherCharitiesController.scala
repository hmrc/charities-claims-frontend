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
import controllers.actions.Actions
import forms.YesNoFormProvider
import models.{Mode, RepaymentClaimDetailsAnswers}
import models.Mode.*
import controllers.repaymentClaimDetails.routes
import models.requests.DataRequest
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import play.api.data.Form
import services.SaveService
import views.html.{ConnectedToAnyOtherCharitiesView, UpdateRepaymentClaimView}
import scala.concurrent.{ExecutionContext, Future}

class ConnectedToAnyOtherCharitiesController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: ConnectedToAnyOtherCharitiesView,
  updateRepaymentClaimView: UpdateRepaymentClaimView,
  actions: Actions,
  formProvider: YesNoFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean]              = formProvider("connectedToAnyOtherCharities.error.required")
  val confirmUpdateForm: Form[Boolean] = formProvider("updateRepaymentClaim.error.required")

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    if RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true) then {
      val previousAnswer = RepaymentClaimDetailsAnswers.getConnectedToAnyOtherCharities
      Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))
    } else {
      Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad))
    }
  }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
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
          val previousAnswer = RepaymentClaimDetailsAnswers.getConnectedToAnyOtherCharities

          if (needsUpdateConfirmation(mode, previousAnswer, newAnswer)) {
            Future.successful(
              Ok(
                updateRepaymentClaimView(
                  confirmUpdateForm,
                  routes.ConnectedToAnyOtherCharitiesController.onSubmit(mode)
                )
              )
            )
          } else {
            saveService
              .save(
                RepaymentClaimDetailsAnswers.setConnectedToAnyOtherCharities(newAnswer)
              )
              .map(_ =>
                Redirect(
                  ConnectedToAnyOtherCharitiesController.nextPage(newAnswer, mode, previousAnswer)
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
                routes.ConnectedToAnyOtherCharitiesController.onSubmit(mode)
              )
            )
          ),
        {
          case true =>
            val previousAnswer = RepaymentClaimDetailsAnswers.getConnectedToAnyOtherCharities

            saveService
              .save(
                RepaymentClaimDetailsAnswers.setConnectedToAnyOtherCharities(false)
              )
              .map(_ =>
                Redirect(
                  ConnectedToAnyOtherCharitiesController.nextPage(false, mode, previousAnswer)
                )
              )

          case false =>
            Future.successful(Redirect(routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad))
        }
      )
}

object ConnectedToAnyOtherCharitiesController {

  def nextPage(value: Boolean, mode: Mode, previousAnswer: Option[Boolean]): Call =
    (value, mode, previousAnswer) match {
      // NormalMode
      case (_, NormalMode, _) =>
        routes.ClaimingReferenceNumberController.onPageLoad(NormalMode)

      // CheckMode
      case (_, CheckMode, _)  =>
        routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad
    }
}

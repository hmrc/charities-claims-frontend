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

package controllers.repaymentClaimDetails

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.Actions
import controllers.repaymentClaimDetails.routes
import forms.YesNoFormProvider
import models.{Mode, RepaymentClaimDetailsAnswers}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.SaveService
import views.html.ClaimingReferenceNumberView
import models.Mode.*

import scala.concurrent.{ExecutionContext, Future}

class ClaimingReferenceNumberController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: ClaimingReferenceNumberView,
  actions: Actions,
  formProvider: YesNoFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean] = formProvider("claimingReferenceNumber.error.required")

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    val previousAnswer = RepaymentClaimDetailsAnswers.getClaimingReferenceNumber
    Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))
  }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    val previousAnswer = RepaymentClaimDetailsAnswers.getClaimingReferenceNumber
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          saveService
            .save(RepaymentClaimDetailsAnswers.setClaimingReferenceNumber(value))
            .map(_ => Redirect(ClaimingReferenceNumberController.nextPage(value, mode, previousAnswer)))
      )
  }
}

object ClaimingReferenceNumberController {

  def nextPage(value: Boolean, mode: Mode, previousAnswer: Option[Boolean]): Call =
    (value, mode, previousAnswer) match {
      // NormalMode
      case (true, NormalMode, _)                              =>
        routes.ClaimReferenceNumberInputController.onPageLoad(NormalMode)
      case (_, NormalMode, _)                                 =>
        routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad

      // CheckMode: new data
      case (true, CheckMode, None)                            =>
        routes.ClaimReferenceNumberInputController.onPageLoad(CheckMode)
      case (_, CheckMode, None)                               =>
        routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad

      // CheckMode:
      case (newVal, CheckMode, Some(prev)) if newVal && !prev =>
        routes.ClaimReferenceNumberInputController.onPageLoad(CheckMode)

      // unchanged
      case (_, CheckMode, _)                                  =>
        routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad
    }

}

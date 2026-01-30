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

import models.Mode.*
import services.SaveService
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import com.google.inject.Inject
import controllers.BaseController
import views.html.ClaimReferenceNumberInputView
import controllers.actions.Actions
import forms.TextInputFormProvider
import models.{Mode, RepaymentClaimDetailsAnswers}
import play.api.data.Form
import controllers.repaymentClaimDetails.routes

import scala.concurrent.{ExecutionContext, Future}

class ClaimReferenceNumberInputController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: ClaimReferenceNumberInputView,
  actions: Actions,
  formProvider: TextInputFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[String] = formProvider(
    "claimReferenceNumber.error.required",
    (20, "claimReferenceNumber.error.length"),
    "claimReferenceNumber.error.regex"
  )

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    if RepaymentClaimDetailsAnswers.getClaimingReferenceNumber.contains(true)
    then {
      val previousAnswer = RepaymentClaimDetailsAnswers.getClaimReferenceNumber
      Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))
    } else {
      Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad))
    }
  }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          saveService
            .save(RepaymentClaimDetailsAnswers.setClaimReferenceNumber(value))
            .map { _ =>
              // Change to: R1.8 - Check Your Answers (CYA) when created
              if (mode == CheckMode) {
                Redirect(routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad)
              } else {
                Redirect(routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad)
              }
            }
      )
  }
}

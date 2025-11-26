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

package controllers.repaymentclaimdetails

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.Actions
import forms.YesNoFormProvider
import models.RepaymentClaimDetailsAnswers
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SaveService
import views.html.ClaimingTaxReliefView

import scala.concurrent.{ExecutionContext, Future}

class ClaimingTaxReliefController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: ClaimingTaxReliefView,
  actions: Actions,
  formProvider: YesNoFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean] = formProvider("claimingOtherIncome.error.required")

  def onPageLoad: Action[AnyContent] = actions.authAndGetData() { implicit request =>
    val previousAnswer = RepaymentClaimDetailsAnswers.getClaimingTaxDeducted
    Ok(view(form.withDefault(previousAnswer)))
  }

  def onSubmit: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value =>
          saveService
            .save(RepaymentClaimDetailsAnswers.setClaimingTaxDeducted(value))
            .map(_ =>
              Redirect(controllers.repaymentclaimdetails.routes.ClaimingGiftAidSmallDonationsController.onPageLoad)
            )
      )
  }
}

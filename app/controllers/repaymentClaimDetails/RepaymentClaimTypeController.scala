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
import forms.CheckBoxListFormProvider
import models.{Mode, RepaymentClaimDetailsAnswers, RepaymentClaimType}
import controllers.repaymentClaimDetails.routes
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.data.Form
import services.SaveService
import views.html.RepaymentClaimTypeView
import models.Mode.*

import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.Call

class RepaymentClaimTypeController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: RepaymentClaimTypeView,
  actions: Actions,
  formProvider: CheckBoxListFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {
  val form: Form[RepaymentClaimType] = formProvider()

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    val previousAnswer = RepaymentClaimDetailsAnswers.getRepaymentClaimType
    Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))
  }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    val previousAnswer: Option[RepaymentClaimType] = RepaymentClaimDetailsAnswers.getRepaymentClaimType
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        repaymentClaimType =>
          saveService
            .save(RepaymentClaimDetailsAnswers.setRepaymentClaimType(repaymentClaimType, previousAnswer))
            .map(_ => Redirect(RepaymentClaimTypeController.nextPage(repaymentClaimType, mode, previousAnswer)))
      )
  }

}

object RepaymentClaimTypeController {

  def nextPage(value: RepaymentClaimType, mode: Mode, previousAnswer: Option[RepaymentClaimType]): Call =
    (value, mode, previousAnswer) match {
      // NormalMode
      case (value, NormalMode, _)   =>
        if value.claimingUnderGiftAidSmallDonationsScheme then
          routes.ClaimGiftAidSmallDonationsSchemeController.onPageLoad(NormalMode)
        else routes.ClaimingReferenceNumberController.onPageLoad(NormalMode)

      // CheckMode: new data
      case (value, CheckMode, None) =>
        if value.claimingUnderGiftAidSmallDonationsScheme then
          routes.ClaimGiftAidSmallDonationsSchemeController.onPageLoad(CheckMode)
        else routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad

      // CheckMode:
      case (newVal, CheckMode, Some(prev))
          if newVal.claimingUnderGiftAidSmallDonationsScheme && prev.claimingUnderGiftAidSmallDonationsScheme.eq(
            false
          ) =>
        routes.ClaimGiftAidSmallDonationsSchemeController.onPageLoad(CheckMode)

      // unchanged
      case (_, CheckMode, _)        =>
        routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad
    }

}

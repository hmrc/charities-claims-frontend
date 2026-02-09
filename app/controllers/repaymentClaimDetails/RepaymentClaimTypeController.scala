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
import forms.{CheckBoxListFormProvider, YesNoFormProvider}
import models.{Mode, RepaymentClaimDetailsAnswers, RepaymentClaimType}
import models.requests.DataRequest
import controllers.repaymentClaimDetails.routes
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.data.Form
import services.SaveService
import views.html.{RepaymentClaimTypeView, UpdateRepaymentClaimView}
import models.Mode.*
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.Call

class RepaymentClaimTypeController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: RepaymentClaimTypeView,
  updateRepaymentClaimView: UpdateRepaymentClaimView,
  actions: Actions,
  formProvider: CheckBoxListFormProvider,
  yesNoFormProvider: YesNoFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[RepaymentClaimType]   = formProvider()
  val confirmUpdateForm: Form[Boolean] = yesNoFormProvider("updateRepaymentClaim.error.required")

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    val previousAnswer = RepaymentClaimDetailsAnswers.getRepaymentClaimType
    Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))
  }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    if (isConfirmingUpdate) {
      handleUpdateConfirmationSubmit(mode)
    } else {
      handleQuestionSubmit(mode)
    }
  }

  def handleQuestionSubmit(mode: Mode)(implicit request: DataRequest[AnyContent]) = {
    val previousAnswer: Option[RepaymentClaimType] = RepaymentClaimDetailsAnswers.getRepaymentClaimType
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        repaymentClaimType =>
          if (needsUpdateConfirmationForRepaymentClaimType(mode, previousAnswer, repaymentClaimType)) {
            val checkboxValues = formProvider.toSet(repaymentClaimType).toSeq
            Future.successful(
              Ok(
                updateRepaymentClaimView(
                  confirmUpdateForm,
                  routes.RepaymentClaimTypeController.onSubmit(mode),
                  checkboxValues
                )
              )
            )
          } else {
            saveService
              .save(RepaymentClaimDetailsAnswers.setRepaymentClaimType(repaymentClaimType, previousAnswer))
              .map(_ => Redirect(RepaymentClaimTypeController.nextPage(repaymentClaimType, mode, previousAnswer)))
          }
      )
  }

  def handleUpdateConfirmationSubmit(mode: Mode)(implicit request: DataRequest[AnyContent]) = {
    val checkboxValues = getCheckboxValuesFromRequest
    confirmUpdateForm
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(
            BadRequest(
              updateRepaymentClaimView(
                formWithErrors,
                routes.RepaymentClaimTypeController.onSubmit(mode),
                checkboxValues
              )
            )
          ),
        {
          case true =>
            form
              .bindFromRequest()
              .fold(
                _ => Future.successful(Redirect(routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad)),
                repaymentClaimType => {
                  val previousAnswer = RepaymentClaimDetailsAnswers.getRepaymentClaimType
                  saveService
                    .save(RepaymentClaimDetailsAnswers.setRepaymentClaimType(repaymentClaimType, previousAnswer))
                    .map(_ => Redirect(RepaymentClaimTypeController.nextPage(repaymentClaimType, mode, previousAnswer)))
                }
              )

          case false =>
            Future.successful(Redirect(routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad))
        }
      )
  }

  private def getCheckboxValuesFromRequest(implicit request: DataRequest[AnyContent]): Seq[String] =
    request.body.asFormUrlEncoded
      .flatMap(_.get("value[]"))
      .getOrElse(Seq.empty)

  private def needsUpdateConfirmationForRepaymentClaimType(
    mode: Mode,
    previousAnswer: Option[RepaymentClaimType],
    newAnswer: RepaymentClaimType
  ): Boolean =
    mode match {
      case CheckMode =>
        previousAnswer.exists { prev =>
          (prev.claimingGiftAid && !newAnswer.claimingGiftAid) ||
          (prev.claimingTaxDeducted && !newAnswer.claimingTaxDeducted) ||
          (prev.claimingUnderGiftAidSmallDonationsScheme && !newAnswer.claimingUnderGiftAidSmallDonationsScheme)
        }
      case _         => false
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

      // CheckMode: New data
      case (value, CheckMode, None) =>
        if value.claimingUnderGiftAidSmallDonationsScheme then
          routes.ClaimGiftAidSmallDonationsSchemeController.onPageLoad(CheckMode)
        else routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad

      // CheckMode: Changed GASDS from No to Yes
      case (newVal, CheckMode, Some(prev))
          if newVal.claimingUnderGiftAidSmallDonationsScheme && prev.claimingUnderGiftAidSmallDonationsScheme.eq(
            false
          ) =>
        routes.ClaimGiftAidSmallDonationsSchemeController.onPageLoad(CheckMode)

      // CheckMode: Answer unchanged
      case (_, CheckMode, _)        =>
        routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad
    }
}

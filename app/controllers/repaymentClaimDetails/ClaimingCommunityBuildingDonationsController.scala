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
import controllers.repaymentClaimDetails.routes
import forms.YesNoFormProvider
import models.{Mode, RepaymentClaimDetailsAnswers}
import models.requests.DataRequest
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.SaveService
import views.html.{ClaimingCommunityBuildingDonationsView, UpdateRepaymentClaimView}
import scala.concurrent.{ExecutionContext, Future}
import models.Mode.*

class ClaimingCommunityBuildingDonationsController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: ClaimingCommunityBuildingDonationsView,
  confirmationView: UpdateRepaymentClaimView,
  actions: Actions,
  formProvider: YesNoFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean] = formProvider("claimingCommunityBuildingDonations.error.required")

  val confirmUpdateForm: Form[Boolean] = formProvider("updateRepaymentClaim.error.required")

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    if RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true) then {
      val previousAnswer = RepaymentClaimDetailsAnswers.getClaimingDonationsCollectedInCommunityBuildings
      Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))
    } else {
      Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad))
    }
  }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    if (isConfirmingUpdate) {
      handleUpdateConfirmationSubmission(mode)
    } else {
      handleQuestionSubmission(mode)
    }
  }

  def handleQuestionSubmission(mode: Mode)(implicit request: DataRequest[AnyContent]) =
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        newAnswer => {
          val previousAnswer   = RepaymentClaimDetailsAnswers.getClaimingDonationsCollectedInCommunityBuildings
          val prevScreenAnswer = RepaymentClaimDetailsAnswers.getClaimingDonationsNotFromCommunityBuilding
          val nextScreenAnswer = RepaymentClaimDetailsAnswers.getMakingAdjustmentToPreviousClaim

          if (needsUpdateConfirmation(mode, previousAnswer, newAnswer)) {
            Future.successful(
              Ok(
                confirmationView(
                  confirmUpdateForm,
                  routes.ClaimingCommunityBuildingDonationsController.onSubmit(mode)
                )
              )
            )
          } else {
            saveService
              .save(
                RepaymentClaimDetailsAnswers
                  .setClaimingDonationsCollectedInCommunityBuildings(newAnswer, prevScreenAnswer)
              )
              .map(_ =>
                Redirect(
                  ClaimingCommunityBuildingDonationsController.nextPage(
                    newAnswer,
                    mode,
                    prevScreenAnswer,
                    previousAnswer,
                    nextScreenAnswer
                  )
                )
              )
          }
        }
      )

  def handleUpdateConfirmationSubmission(mode: Mode)(implicit request: DataRequest[AnyContent]) =
    confirmUpdateForm
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(
            BadRequest(
              confirmationView(
                formWithErrors,
                routes.ClaimingCommunityBuildingDonationsController.onSubmit(mode)
              )
            )
          ),
        {
          case true =>
            val previousAnswer   = RepaymentClaimDetailsAnswers.getClaimingDonationsCollectedInCommunityBuildings
            val prevScreenAnswer = RepaymentClaimDetailsAnswers.getClaimingDonationsNotFromCommunityBuilding
            val nextScreenAnswer = RepaymentClaimDetailsAnswers.getMakingAdjustmentToPreviousClaim

            saveService
              .save(
                RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(false, prevScreenAnswer)
              )
              .map(_ =>
                Redirect(
                  ClaimingCommunityBuildingDonationsController.nextPage(
                    false,
                    mode,
                    prevScreenAnswer,
                    previousAnswer,
                    nextScreenAnswer
                  )
                )
              )

          case false =>
            Future.successful(Redirect(routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad))
        }
      )
}

object ClaimingCommunityBuildingDonationsController {

  def nextPage(
    value: Boolean,
    mode: Mode,
    prevScreenAnswer: Option[Boolean],
    previousAnswer: Option[Boolean],
    nextScreenAnswer: Option[Boolean]
  ): Call =
    (value, mode, previousAnswer) match {

      // NormalMode: User answered Yes
      case (true, NormalMode, _)         =>
        routes.ChangePreviousGASDSClaimController.onPageLoad(NormalMode)

      // NormalMode: User answered No
      case (false, NormalMode, _)        =>
        if prevScreenAnswer.contains(false) then routes.ConnectedToAnyOtherCharitiesController.onPageLoad(NormalMode)
        else routes.ChangePreviousGASDSClaimController.onPageLoad(NormalMode)

      // CheckMode: New answer is Yes
      case (true, CheckMode, None)       =>
        routes.ChangePreviousGASDSClaimController.onPageLoad(CheckMode)

      // CheckMode: New answer is No
      case (false, CheckMode, None)      =>
        if prevScreenAnswer.contains(false) then routes.ConnectedToAnyOtherCharitiesController.onPageLoad(CheckMode)
        else routes.ChangePreviousGASDSClaimController.onPageLoad(CheckMode)

      // CheckMode: Answer unchanged yes
      case (true, CheckMode, Some(true)) =>
        if nextScreenAnswer.isDefined then routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad
        else routes.ChangePreviousGASDSClaimController.onPageLoad(CheckMode)

      case (false, CheckMode, Some(false)) =>
        if prevScreenAnswer.contains(true) && nextScreenAnswer.isEmpty
        then routes.ChangePreviousGASDSClaimController.onPageLoad(CheckMode)
        else routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad

      // CheckMode: Other scenarios
      case (newVal, CheckMode, _)          =>
        // checking if answer has changed from yes -> No or no->yes, and if prev screen (R1.2) is true
        if newVal || prevScreenAnswer.contains(true) then
          routes.ChangePreviousGASDSClaimController.onPageLoad(CheckMode)
        else routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad
    }
}

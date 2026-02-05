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

  // this form is for the original question (field name: "value")
  val form: Form[Boolean] = formProvider("claimingCommunityBuildingDonations.error.required")

  // this for is for WRN3 confirmation (field name: "value" - same as above, will have hidden field confirmingUpdate = true)
  val confirmUpdateForm: Form[Boolean] = formProvider("updateRepaymentClaim.error.required")

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    if RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true) then {
      val previousAnswer = RepaymentClaimDetailsAnswers.getClaimingDonationsCollectedInCommunityBuildings
      Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))
    } else { Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad)) }
  }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    // CONFIRMATION CHECK FLOW: User is answering the WRN3 confirmation question
    if (isConfirmingUpdate) {
      handleUpdateConfirmation(mode)
    } else {
      // ORIGINAL QUESTION FLOW: User is answering normal question
      handleQuestionSubmission(mode)
    }
  }

  def handleQuestionSubmission(mode: Mode)(implicit request: models.requests.DataRequest[AnyContent]) =
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        newAnswer => {
          val previousAnswer = RepaymentClaimDetailsAnswers.getClaimingDonationsCollectedInCommunityBuildings

          if (needsUpdateConfirmation(mode, previousAnswer, newAnswer)) {
            // then we show WRN3 confirmation
            Future.successful(
              Ok(
                confirmationView(
                  confirmUpdateForm,
                  routes.ClaimingCommunityBuildingDonationsController.onSubmit(mode)
                )
              )
            )
          } else {
            // normal question flow - WRN3 not needed - save and redirect
            saveService
              .save(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(newAnswer))
              .map(_ =>
                Redirect(
                  ClaimingCommunityBuildingDonationsController.nextPage(
                    newAnswer,
                    mode,
                    previousAnswer,
                    RepaymentClaimDetailsAnswers.getClaimingDonationsNotFromCommunityBuilding
                  )
                )
              )
          }
        }
      )

  // only used when user is answering the WRN3 confirmation question
  def handleUpdateConfirmation(mode: Mode)(implicit request: models.requests.DataRequest[AnyContent]) =
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
            // if user confirmed yes - save the change to false and proceed
            // previousAnswer = Some(true) because they're confirming change from true to false
            saveService
              .save(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(false))
              .map(_ =>
                Redirect(
                  ClaimingCommunityBuildingDonationsController.nextPage(
                    false,
                    mode,
                    Some(true),
                    RepaymentClaimDetailsAnswers.getClaimingDonationsNotFromCommunityBuilding
                  )
                )
              )

          case false =>
            // user canceled - go back to CYA without any change
            Future.successful(Redirect(routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad))
        }
      )
  }
}

object ClaimingCommunityBuildingDonationsController {

  def nextPage(
    value: Boolean,
    mode: Mode,
    previousAnswer: Option[Boolean],
    claimingDonationsNotFromCommunityBuildingAnswer: Option[Boolean],
    connectedToAnyOtherCharities: Option[Boolean]
  ): Call =
    (value, mode, previousAnswer) match {
      // NormalMode
      case (true, NormalMode, _)         =>
        routes.ChangePreviousGASDSClaimController.onPageLoad(NormalMode)
      case (false, NormalMode, _)        =>
        if claimingDonationsNotFromCommunityBuildingAnswer.contains(false) then
          routes.ConnectedToAnyOtherCharitiesController.onPageLoad(NormalMode)
        else routes.ChangePreviousGASDSClaimController.onPageLoad(NormalMode)

      // CheckMode: new data
      case (true, CheckMode, None)       =>
        routes.ChangePreviousGASDSClaimController.onPageLoad(CheckMode)

      // CheckMode: new data
      case (false, CheckMode, None)      =>
        if claimingDonationsNotFromCommunityBuildingAnswer.contains(false) then
          routes.ConnectedToAnyOtherCharitiesController.onPageLoad(CheckMode)
        else routes.ChangePreviousGASDSClaimController.onPageLoad(CheckMode)
      // Checkmode : both new and prev are true
      case (true, CheckMode, Some(true)) =>
        routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad

      case (newVal, CheckMode, prev) =>
        if !newVal && prev.contains(false) && claimingDonationsNotFromCommunityBuildingAnswer.contains(
            false
          )
          && connectedToAnyOtherCharities.isEmpty
        then routes.ConnectedToAnyOtherCharitiesController.onPageLoad(CheckMode)
        else if newVal || claimingDonationsNotFromCommunityBuildingAnswer.contains(true) then
          routes.ChangePreviousGASDSClaimController.onPageLoad(CheckMode)
        else routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad

    }
}

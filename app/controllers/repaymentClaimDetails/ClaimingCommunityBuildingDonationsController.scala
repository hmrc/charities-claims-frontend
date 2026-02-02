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

  // Form for the original question (field name: "value")
  val form: Form[Boolean] = formProvider("claimingCommunityBuildingDonations.error.required")

  // Form for WRN3 confirmation (field name: "value" - same as above, will have hidden field confirmingUpdate = true)
  private val confirmUpdateForm: Form[Boolean] = formProvider("updateRepaymentClaim.error.required")

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    if RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true) then {
      val previousAnswer = RepaymentClaimDetailsAnswers.getClaimingDonationsCollectedInCommunityBuildings
      Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))
    } else { Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad)) }
  }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    // check for the hidden field "confirmingUpdate"
    // - if "confirmingUpdate=true" is present then user is confirming a change with WRN3
    // - if "confirmingUpdate" is absent then user is answering the original question as normal
    val isConfirmingUpdate = request.body.asFormUrlEncoded
      .flatMap(_.get("confirmingUpdate"))
      .exists(_.headOption.contains("true"))

    if (isConfirmingUpdate) {
      // UPDATE CONFIRMATION: User is confirming they want to change from Yes to No
      handleUpdateConfirmation(mode)
    } else {
      // ORIGINAL QUESTION: User is answering normal question
      handleQuestionSubmission(mode)
    }
  }

  private def handleQuestionSubmission(mode: Mode)(implicit request: models.requests.DataRequest[AnyContent]) =
    form
      .bindFromRequest() // binds "value" field from original question
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        newAnswer => {
          val previousAnswer = RepaymentClaimDetailsAnswers.getClaimingDonationsCollectedInCommunityBuildings

          // Check if we need to show WRN3 confirmation
          // Show WRN3 when: CheckMode AND changing Yes to No
          val needsConfirmation =
            mode == CheckMode &&
              previousAnswer.contains(true) &&
              !newAnswer // newAnswer is false - user is changing to 'No'

          if (needsConfirmation) {
            // we show WRN3 confirmation screen - does NOT save the change yet
            Future.successful(
              Ok(
                confirmationView(
                  confirmUpdateForm,
                  routes.ClaimingCommunityBuildingDonationsController.onSubmit(mode)
                )
              )
            )
          } else {
            // normal flow - user first time - save answer and redirect to next page
            saveService
              .save(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(newAnswer))
              .map(_ => Redirect(nextPage(newAnswer, mode)))
          }
        }
      )

  // handles submission of WRN3 confirmation form
  private def handleUpdateConfirmation(mode: Mode)(implicit request: models.requests.DataRequest[AnyContent]) =
    confirmUpdateForm
      .bindFromRequest() // binds 'value' field from WRN3 confirmation - yes or no to confirm
      .fold(
        formWithErrors =>
          // validation error - user didn't select Yes/No on
          Future.successful(
            BadRequest(
              confirmationView(
                formWithErrors,
                routes.ClaimingCommunityBuildingDonationsController.onSubmit(mode)
              )
            )
          ),
        userConfirmedChange =>
          // userConfirmedChange = true - 'Yes, I want to update' (proceed with change to No)
          // userConfirmedChange = false - 'No, cancel' (go back to CYA without saving)

          if (userConfirmedChange) {
            // user confirmed - we save the change to false and proceed with user flow
            saveService
              .save(RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(false))
              .map(_ => Redirect(nextPage(false, mode)))
          } else {
            // user cancelled - we go back to CYA without saving anything
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

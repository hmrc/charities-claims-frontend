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
import views.html.ClaimingCommunityBuildingDonationsView

import scala.concurrent.{ExecutionContext, Future}
import models.Mode.*

class ClaimingCommunityBuildingDonationsController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: ClaimingCommunityBuildingDonationsView,
  actions: Actions,
  formProvider: YesNoFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean] = formProvider("claimingCommunityBuildingDonations.error.required")

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    if RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true) then {
      val previousAnswer = RepaymentClaimDetailsAnswers.getClaimingDonationsCollectedInCommunityBuildings
      Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))
    } else { Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad)) }
  }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    val previousAnswer                                  = RepaymentClaimDetailsAnswers.getClaimingDonationsCollectedInCommunityBuildings
    val claimingDonationsNotFromCommunityBuildingAnswer =
      RepaymentClaimDetailsAnswers.getClaimingDonationsNotFromCommunityBuilding
    val connectedToAnyOtherCharities                    =
      RepaymentClaimDetailsAnswers.getConnectedToAnyOtherCharities
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          saveService
            .save(
              RepaymentClaimDetailsAnswers.setClaimingDonationsCollectedInCommunityBuildings(
                value,
                claimingDonationsNotFromCommunityBuildingAnswer
              )
            )
            .map(_ =>
              Redirect(
                ClaimingCommunityBuildingDonationsController
                  .nextPage(
                    value,
                    mode,
                    previousAnswer,
                    claimingDonationsNotFromCommunityBuildingAnswer,
                    connectedToAnyOtherCharities
                  )
              )
            )
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

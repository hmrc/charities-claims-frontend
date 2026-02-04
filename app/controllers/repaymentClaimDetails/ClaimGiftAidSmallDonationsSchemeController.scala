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
import views.html.ClaimGiftAidSmallDonationsSchemeView
import models.Mode.*

import scala.concurrent.{ExecutionContext, Future}

class ClaimGiftAidSmallDonationsSchemeController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: ClaimGiftAidSmallDonationsSchemeView,
  actions: Actions,
  formProvider: YesNoFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean] = formProvider("claimGASDS.error.required")

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    if RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true) then {
      val previousAnswer = RepaymentClaimDetailsAnswers.getClaimingDonationsNotFromCommunityBuilding
      Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))
    } else { Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad)) }

  }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    val previousAnswer = RepaymentClaimDetailsAnswers.getClaimingDonationsNotFromCommunityBuilding
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          saveService
            .save(RepaymentClaimDetailsAnswers.setClaimingDonationsNotFromCommunityBuilding(value))
            .map(_ => Redirect(ClaimGiftAidSmallDonationsSchemeController.nextPage(value, mode, previousAnswer)))
      )
  }
}

object ClaimGiftAidSmallDonationsSchemeController {

  def nextPage(value: Boolean, mode: Mode, previousAnswer: Option[Boolean]): Call =
    (value, mode, previousAnswer) match {
      // NormalMode
      case (_, NormalMode, _)                                                        =>
        routes.ClaimingCommunityBuildingDonationsController.onPageLoad(NormalMode)

      // CheckMode: new data
      case (_, CheckMode, None)                                                      =>
        routes.ClaimingCommunityBuildingDonationsController.onPageLoad(CheckMode)

      // CheckMode: new value diff to old value
      case (newVal, CheckMode, Some(prev)) if (newVal && !prev) || (!newVal && prev) =>
        routes.ClaimingCommunityBuildingDonationsController.onPageLoad(CheckMode)

      // CheckMode
      case (_, CheckMode, _)                                                         =>
        routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad

    }

}

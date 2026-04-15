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

package controllers.giftAidSmallDonationsScheme

import com.google.inject.Inject
import controllers.actions.Actions
import models.{RepaymentClaimDetailsAnswers, SessionData}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.GasdsAdjustmentAmountCheckYourAnswersView

import scala.concurrent.Future

class GasdsAdjustmentAmountCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: GasdsAdjustmentAmountCheckYourAnswersView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    actions
      .authAndRefreshDataWithGuard(
        SessionData.isRepaymentClaimDetailsComplete &&
          RepaymentClaimDetailsAnswers.getMakingAdjustmentToPreviousClaim.contains(true)
      )
      .async { implicit request =>
        val giftAidDonationsAnswers = request.sessionData.giftAidSmallDonationsSchemeDonationDetailsAnswers
        Future.successful(Ok(view(giftAidDonationsAnswers)))
      }

  def onSubmit: Action[AnyContent] =
    actions
      .authAndRefreshDataWithGuard(
        SessionData.isRepaymentClaimDetailsComplete &&
          RepaymentClaimDetailsAnswers.getMakingAdjustmentToPreviousClaim.contains(true)
      )
      .async { implicit request =>
        val claimingUnderGasds =
          request.sessionData.repaymentClaimDetailsAnswers
            .flatMap(_.claimingUnderGiftAidSmallDonationsScheme)
            .contains(true)

        Future.successful(
          if (claimingUnderGasds) {
            Redirect(routes.WhichTaxYearAreYouClaimingForController.onPageLoad(1))
          } else {
            Redirect(
              "/charities-claims/check-your-GASDS-donation-details"
            ) // TODO update URL when next screen is available
          }
        )
      }
}

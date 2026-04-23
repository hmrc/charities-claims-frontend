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
import views.html.ClaimDetailsForTaxYearCheckYourAnswersView

import scala.concurrent.Future

class ClaimDetailsForTaxYearCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: ClaimDetailsForTaxYearCheckYourAnswersView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(index: Int): Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(
        SessionData.isRepaymentClaimDetailsComplete
          && RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true)
      )
      .async { implicit request =>

        val claimOpt =
          request.sessionData.giftAidSmallDonationsSchemeDonationDetailsAnswers
            .flatMap(_.claims)
            .flatMap(_.lift(index - 1))
            .flatten
        Future.successful(Ok(view(claimOpt, index)))
      }

  def onSubmit(index: Int): Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(
        SessionData.isRepaymentClaimDetailsComplete
          && RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true)
      )
      .async { implicit request =>
        Future.successful(
          Redirect("/charities-claims/claim-added-for-tax-year")
        ) // TODO update URL when next screen is available
      }
}

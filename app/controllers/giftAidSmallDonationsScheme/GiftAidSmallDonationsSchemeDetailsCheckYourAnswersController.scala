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

package controllers.giftAidSmallDonationsScheme

import com.google.inject.Inject
import controllers.actions.Actions
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.MessagesControllerComponents
import services.ClaimsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.GiftAidSmallDonationsSchemeDetailsCheckYourAnswersView
import viewmodels.GiftAidSmallDonationsSchemeDonationDetailsAnswersHelper
import models.RepaymentClaimDetailsAnswers
import models.SessionData
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.{ExecutionContext, Future}

class GiftAidSmallDonationsSchemeDetailsCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  claimsService: ClaimsService,
  val controllerComponents: MessagesControllerComponents,
  view: GiftAidSmallDonationsSchemeDetailsCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(
        SessionData.isRepaymentClaimDetailsComplete
          && RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true)
      )
      .async { implicit request =>
        import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
        val previousAnswers = request.sessionData.giftAidSmallDonationsSchemeDonationDetailsAnswers

        val oSummaryListForAdjustmentToGiftAidOverclaimed: Option[SummaryList] =
          if request.sessionData.repaymentClaimDetailsAnswers
              .flatMap(_.makingAdjustmentToPreviousClaim)
              .contains(true)
          then
            Some(
              GiftAidSmallDonationsSchemeDonationDetailsAnswersHelper
                .buildSummaryListForAdjustmentToGiftAidOverclaimed(previousAnswers)
            )
          else None

        val oSummaryListForNumberOfTaxYearsAdded: Option[SummaryList] =
          if request.sessionData.repaymentClaimDetailsAnswers
              .flatMap(_.claimingDonationsNotFromCommunityBuilding)
              .contains(true)
          then
            Some(
              GiftAidSmallDonationsSchemeDonationDetailsAnswersHelper
                .buildSummaryListForNumberOfTaxYearsAdded(previousAnswers)
            )
          else None

        Future.successful(
          Ok(
            view(
              oSummaryListForAdjustmentToGiftAidOverclaimed,
              oSummaryListForNumberOfTaxYearsAdded
            )
          )
        )
      }

  def onSubmit: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(
        SessionData.isRepaymentClaimDetailsComplete
          && RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true)
      )
      .async { implicit request =>
        claimsService.save.map { _ =>
          Redirect(controllers.routes.ClaimsTaskListController.onPageLoad)
        }
      }
}

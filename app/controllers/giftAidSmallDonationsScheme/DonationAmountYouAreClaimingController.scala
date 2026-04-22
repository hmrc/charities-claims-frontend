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
import controllers.BaseController
import controllers.actions.Actions
import forms.AmountFormProvider
import models.{GiftAidSmallDonationsSchemeDonationDetailsAnswers, RepaymentClaimDetailsAnswers, SessionData}
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SaveService
import utils.TaxYearLabels.taxYearLabelKey
import views.html.DonationAmountYouAreClaimingView

import scala.concurrent.{ExecutionContext, Future}

class DonationAmountYouAreClaimingController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  view: DonationAmountYouAreClaimingView,
  formProvider: AmountFormProvider,
  saveService: SaveService
)(using ExecutionContext)
    extends BaseController {

  private def zeroIndex(index: Int): Int = index - 1

  private def form(index: Int)(using messages: Messages) =
    val label = messages(taxYearLabelKey(index))
    formProvider(
      errorRequired = messages("donationAmountYouAreClaiming.error.required", label),
      formatErrorMsg = messages("donationAmountYouAreClaiming.error.invalid", label),
      maxLengthErrorMsg = messages("donationAmountYouAreClaiming.error.maxLength"),
      allowZero = true
    )

  def onPageLoad(index: Int): Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(
        SessionData.isRepaymentClaimDetailsComplete
          && RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true)
          && GiftAidSmallDonationsSchemeDonationDetailsAnswers.isClaimExist(zeroIndex(index))
      )
      .async { implicit request =>
        implicit val messages: Messages = messagesApi.preferred(request)

        val preparedForm = form(index)

        val existingValue =
          GiftAidSmallDonationsSchemeDonationDetailsAnswers
            .getClaim(zeroIndex(index))
            .flatMap(_.amountOfDonationsReceived)

        Future.successful(
          Ok(view(preparedForm.withDefault(existingValue), index))
        )
      }

  def onSubmit(index: Int): Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(
        SessionData.isRepaymentClaimDetailsComplete
          && RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true)
          && GiftAidSmallDonationsSchemeDonationDetailsAnswers.isClaimExist(zeroIndex(index))
      )
      .async { implicit request =>
        implicit val messages: Messages = messagesApi.preferred(request)
        val preparedForm                = form(index)

        preparedForm
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, index))),
            value =>
              GiftAidSmallDonationsSchemeDonationDetailsAnswers
                .getClaim(zeroIndex(index))
                .fold {
                  Future.successful(
                    Redirect(controllers.routes.ClaimsTaskListController.onPageLoad)
                  )
                } { existingClaim =>

                  val updatedClaim =
                    existingClaim.copy(amountOfDonationsReceived = Some(value))

                  saveService
                    .save(
                      GiftAidSmallDonationsSchemeDonationDetailsAnswers
                        .setClaim(zeroIndex(index), updatedClaim)
                    )
                    .map(_ => Redirect(s"/check-claim-details-for-tax-year/$index"))
                }
          )
      }
}

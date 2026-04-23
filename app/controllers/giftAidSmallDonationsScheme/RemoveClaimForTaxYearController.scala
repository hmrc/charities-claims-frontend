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
import forms.YesNoFormProvider
import models.{GiftAidSmallDonationsSchemeDonationDetailsAnswers, RepaymentClaimDetailsAnswers, SessionData}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SaveService
import views.html.RemoveClaimForTaxYearView

import scala.concurrent.{ExecutionContext, Future}

class RemoveClaimForTaxYearController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  view: RemoveClaimForTaxYearView,
  formProvider: YesNoFormProvider,
  saveService: SaveService
)(using ExecutionContext)
    extends BaseController {

  private def zeroIndex(index: Int): Int = index - 1

  private def form(taxYear: Int)(using messages: Messages): Form[Boolean] =
    formProvider(messages("removeClaimForTaxYear.error.required", taxYear))

  def onPageLoad(index: Int): Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(
        SessionData.isRepaymentClaimDetailsComplete &&
          RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true) &&
          GiftAidSmallDonationsSchemeDonationDetailsAnswers.isValidIndex(index) &&
          GiftAidSmallDonationsSchemeDonationDetailsAnswers.isTaxYearEntered(zeroIndex(index))
      )
      .async { implicit request =>
        implicit val messages: Messages = messagesApi.preferred(request)

        val taxYear = GiftAidSmallDonationsSchemeDonationDetailsAnswers
          .getClaim(zeroIndex(index))(using request.sessionData)
          .map(_.taxYear)
          .getOrElse(0)

        Future.successful(Ok(view(form(taxYear), index, taxYear)))
      }

  def onSubmit(index: Int): Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(
        SessionData.isRepaymentClaimDetailsComplete &&
          RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true) &&
          GiftAidSmallDonationsSchemeDonationDetailsAnswers.isValidIndex(index) &&
          GiftAidSmallDonationsSchemeDonationDetailsAnswers.isTaxYearEntered(zeroIndex(index))
      )
      .async { implicit request =>
        implicit val messages: Messages       = messagesApi.preferred(request)
        implicit val sessionData: SessionData = request.sessionData

        val taxYear = GiftAidSmallDonationsSchemeDonationDetailsAnswers
          .getClaim(zeroIndex(index))
          .map(_.taxYear)
          .getOrElse(0)

        form(taxYear)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, index, taxYear))),
            value =>
              if value then {
                saveService
                  .save(GiftAidSmallDonationsSchemeDonationDetailsAnswers.removeClaim(zeroIndex(index)))
                  .map(_ =>
                    Redirect(
                      s"/check-your-donation-details" // TODO redirect to the correct url once it is implemented
                    )
                  )
              } else {
                Future.successful(
                  Redirect(
                    s"/check-your-donation-details" // TODO redirect to the correct url once it is implemented
                  )
                )
              }
          )
      }
}

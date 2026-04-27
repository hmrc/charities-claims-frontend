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
import forms.TaxYearFormProvider
import models.Mode.{CheckMode, NormalMode}
import models.*
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.TaxYearService.TaxYearError
import services.{SaveService, TaxYearService}
import utils.TaxYearLabels.taxYearLabelKey
import views.html.WhichTaxYearAreYouClaimingForView

import scala.concurrent.{ExecutionContext, Future}

class WhichTaxYearAreYouClaimingForController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  view: WhichTaxYearAreYouClaimingForView,
  formProvider: TaxYearFormProvider,
  taxYearService: TaxYearService,
  saveService: SaveService
)(using ExecutionContext)
    extends BaseController {

  private def zeroIndex(index: Int): Int = index - 1

  private def form(index: Int)(using messages: Messages) = {
    val label = messages(taxYearLabelKey(index))

    formProvider(
      requiredKey = messages("whichTaxYearAreYouClaimingFor.error.required", label),
      invalidKey = messages("whichTaxYearAreYouClaimingFor.error.invalid", label)
    )
  }

  def onPageLoad(index: Int, mode: Mode = NormalMode): Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(
        SessionData.isRepaymentClaimDetailsComplete &&
          RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true) &&
          RepaymentClaimDetailsAnswers.getClaimingDonationsNotFromCommunityBuilding.contains(true) &&
          GiftAidSmallDonationsSchemeDonationDetailsAnswers.isValidIndex(index)
      )
      .async { implicit request =>

        implicit val messages: Messages = messagesApi.preferred(request)

        val preparedForm = form(index)

        val existingValue: Option[Int] =
          GiftAidSmallDonationsSchemeDonationDetailsAnswers
            .getClaim(zeroIndex(index))
            .map(_.taxYear)

        Future.successful(
          Ok(view(preparedForm.withDefault(existingValue), index, mode))
        )
      }

  def onSubmit(index: Int, mode: Mode = NormalMode): Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(
        SessionData.isRepaymentClaimDetailsComplete &&
          RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true) &&
          RepaymentClaimDetailsAnswers.getClaimingDonationsNotFromCommunityBuilding.contains(true) &&
          GiftAidSmallDonationsSchemeDonationDetailsAnswers.isValidIndex(index)
      )
      .async { implicit request =>

        implicit val messages: Messages       = messagesApi.preferred(request)
        implicit val sessionData: SessionData = request.sessionData

        val preparedForm = form(index)

        preparedForm
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, index, mode))),
            taxYear => {

              val existingYears =
                (0 until index - 1).flatMap(i =>
                  GiftAidSmallDonationsSchemeDonationDetailsAnswers
                    .getClaim(i)
                    .map(_.taxYear)
                )

              taxYearService.validateTaxYears(taxYear, existingYears) match {

                case Some(error) =>
                  val formWithError =
                    preparedForm
                      .fill(taxYear)
                      .withError("value", mapErrorToMessage(error))

                  Future.successful(BadRequest(view(formWithError, index, mode)))

                case None =>
                  val claim =
                    GiftAidSmallDonationsSchemeDonationDetailsAnswers
                      .getClaim(zeroIndex(index))
                      .map(_.copy(taxYear = taxYear))
                      .getOrElse(
                        GiftAidSmallDonationsSchemeClaimAnswers(
                          taxYear = taxYear,
                          amountOfDonationsReceived = None
                        )
                      )

                  saveService
                    .save(
                      GiftAidSmallDonationsSchemeDonationDetailsAnswers
                        .setClaim(zeroIndex(index), claim)
                    )
                    .map(_ =>
                      Redirect(
                        WhichTaxYearAreYouClaimingForController.nextPage(mode, index)
                      )
                    )
              }
            }
          )
      }

  private def mapErrorToMessage(error: TaxYearError)(using messages: Messages): String =
    error match {
      case TaxYearError.TooOld(min) =>
        messages("whichTaxYearAreYouClaimingFor.error.tooOld", min.toString)
      case TaxYearError.Future      =>
        messages("whichTaxYearAreYouClaimingFor.error.future")
      case TaxYearError.Duplicate   =>
        messages("whichTaxYearAreYouClaimingFor.error.duplicate")
    }
}
object WhichTaxYearAreYouClaimingForController {

  def nextPage(mode: Mode, index: Int): Call =
    mode match {
      case NormalMode =>
        controllers.giftAidSmallDonationsScheme.routes.DonationAmountYouAreClaimingController
          .onPageLoad(index, NormalMode)
      case CheckMode  =>
        controllers.giftAidSmallDonationsScheme.routes.ClaimDetailsForTaxYearCheckYourAnswersController
          .onPageLoad(index)
    }

}

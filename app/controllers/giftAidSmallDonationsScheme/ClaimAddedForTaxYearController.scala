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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import controllers.actions.Actions as ControllerActions
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import forms.YesNoFormProvider
import models.{GiftAidSmallDonationsSchemeDonationDetailsAnswers, RepaymentClaimDetailsAnswers, SessionData}
import play.api.data.Form
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ClaimAddedForTaxYearView
import models.Mode.*

import scala.concurrent.{ExecutionContext, Future}

class ClaimAddedForTaxYearController @Inject() (
  override val messagesApi: MessagesApi,
  actions: ControllerActions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ClaimAddedForTaxYearView
) extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider("claimAddedForTaxYear.error.required")

  def onPageLoad: Action[AnyContent] = actions
    .authAndGetDataWithGuard(
      SessionData.isRepaymentClaimDetailsComplete
        && RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true)
        && GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaimsSize != 0
    )
    .async { implicit request =>
      val gasdsAnswers: Option[GiftAidSmallDonationsSchemeDonationDetailsAnswers] =
        request.sessionData.giftAidSmallDonationsSchemeDonationDetailsAnswers

      val taxYears: Seq[Int] = getTaxYears(gasdsAnswers)
      val countOfTaxYears    = taxYears.size

      Future.successful(
        Ok(
          view(
            form,
            buildCustomSummaryList(taxYears),
            countOfTaxYears,
            getSingularOrPlural(countOfTaxYears)
          )
        )
      )
    }

  def onSubmit: Action[AnyContent] = actions
    .authAndGetDataWithGuard(
      SessionData.isRepaymentClaimDetailsComplete
        && RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true)
        && GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaimsSize != 0
    )
    .async { implicit request =>

      val gasdsAnswers: Option[GiftAidSmallDonationsSchemeDonationDetailsAnswers] =
        request.sessionData.giftAidSmallDonationsSchemeDonationDetailsAnswers

      val taxYears: Seq[Int] = getTaxYears(gasdsAnswers)
      val countOfTaxYears    = taxYears.size

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(
                view(
                  formWithErrors,
                  buildCustomSummaryList(taxYears),
                  countOfTaxYears,
                  getSingularOrPlural(countOfTaxYears)
                )
              )
            ),
          value =>
            if value then {
              Future.successful(
                Redirect(
                  controllers.giftAidSmallDonationsScheme.routes.WhichTaxYearAreYouClaimingForController
                    .onPageLoad(taxYears.size + 1, NormalMode)
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

  private def getTaxYears(gasdsAnswers: Option[GiftAidSmallDonationsSchemeDonationDetailsAnswers]): Seq[Int] =
    gasdsAnswers.toSeq
      .flatMap(_.claims.toSeq)
      .flatMap(_.collect { case Some(c) => c.taxYear })

  private def buildCustomSummaryList(
    taxYears: Seq[Int]
  )(using messages: Messages): Option[Seq[(String, Seq[(String, String, String)])]] = {
    val taxYearLabels: Seq[String] = taxYears.map { taxYear =>
      messages("claimAddedForTaxYear.taxYear.key", taxYear.toString)
    }

    val customSummaryListRows: Seq[(String, Seq[(String, String, String)])] = taxYearLabels.zipWithIndex.map {
      (taxYearLabel, index) =>
        buildCustomSummaryListRows(taxYearLabels.size > 1, taxYearLabel, index + 1)
    }

    Option.when(customSummaryListRows.nonEmpty)(customSummaryListRows)
  }

  private def buildCustomSummaryListRows(
    isMultipleTaxYears: Boolean,
    label: String,
    index: Int
  ): (String, Seq[(String, String, String)]) =
    val baseActions =
      Seq(
        (
          controllers.giftAidSmallDonationsScheme.routes.ClaimDetailsForTaxYearCheckYourAnswersController
            .onPageLoad(index)
            .url,
          "site.change",
          label
        )
      )

    val actions =
      if isMultipleTaxYears then
        baseActions :+ (controllers.giftAidSmallDonationsScheme.routes.RemoveClaimForTaxYearController
          .onPageLoad(index)
          .url, "site.remove", label)
      else baseActions

    label -> actions

  private def getSingularOrPlural(countOfTaxYears: Int)(using messages: Messages) =
    if (countOfTaxYears > 1) messages("claimAddedForTaxYear.singularOrPlural.plural")
    else messages("claimAddedForTaxYear.singularOrPlural.singular")

}

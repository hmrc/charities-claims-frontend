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
import play.api.i18n.{I18nSupport, MessagesApi}
import forms.YesNoFormProvider
import models.requests.DataRequest
import models.{GiftAidSmallDonationsSchemeDonationDetailsAnswers, RepaymentClaimDetailsAnswers, SessionData}
import viewmodels.ClaimAddedForTaxYearHelper._
import play.api.data.Form
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ClaimAddedForTaxYearView
import models.Mode.*

import scala.concurrent.Future

class ClaimAddedForTaxYearController @Inject() (
  override val messagesApi: MessagesApi,
  actions: ControllerActions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ClaimAddedForTaxYearView
) extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider("claimAddedForTaxYear.error.required")

  private def extractTaxYears(using request: DataRequest[?]): (Seq[Int], Int) = {
    val gasdsAnswers =
      request.sessionData.giftAidSmallDonationsSchemeDonationDetailsAnswers

    val taxYears = getTaxYears(gasdsAnswers)
    (taxYears, taxYears.size)
  }

  def onPageLoad: Action[AnyContent] = actions
    .authAndGetDataWithGuard(
      SessionData.isRepaymentClaimDetailsComplete
        && RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true)
        && GiftAidSmallDonationsSchemeDonationDetailsAnswers.getClaimsSize != 0
    )
    .async { implicit request =>

      val (taxYears, countOfTaxYears) = extractTaxYears

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

      val (taxYears, countOfTaxYears) = extractTaxYears

      if (countOfTaxYears == 3) {
        Future.successful(
          Redirect(
            s"/check-your-donation-details" // TODO redirect to the correct url once it is implemented
          )
        )
      } else {
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
    }

}

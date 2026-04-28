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

package viewmodels

import models.GiftAidSmallDonationsSchemeClaimAnswers
import models.Mode.CheckMode
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

import scala.math.BigDecimal.RoundingMode

object ClaimDetailsForTaxYearCheckYourAnswersHelper {

  def buildSummaryList(
    answers: Option[GiftAidSmallDonationsSchemeClaimAnswers],
    index: Int
  )(implicit messages: Messages): SummaryList = {
    val rows = Seq(
      answers.map(_.taxYear) match {
        case Some(year) =>
          summaryRow(
            messages("claimForTaxYearCheckYourAnswers.taxYear.label"),
            year.toString,
            controllers.giftAidSmallDonationsScheme.routes.WhichTaxYearAreYouClaimingForController
              .onPageLoad(index, CheckMode)
              .url,
            messages("claimForTaxYearCheckYourAnswers.taxYear.label")
          )

        case None =>
          missingDataRow(
            messages("claimForTaxYearCheckYourAnswers.taxYear.label"),
            controllers.giftAidSmallDonationsScheme.routes.WhichTaxYearAreYouClaimingForController
              .onPageLoad(index, CheckMode)
              .url,
            messages("claimForTaxYearCheckYourAnswers.taxYear.label")
          )
      },
      answers.flatMap(_.amountOfDonationsReceived) match {
        case Some(amount) =>
          summaryRow(
            messages("claimForTaxYearCheckYourAnswers.donationAmount.label"),
            formatCurrency(amount),
            controllers.giftAidSmallDonationsScheme.routes.DonationAmountYouAreClaimingController
              .onPageLoad(index, CheckMode)
              .url,
            messages("claimForTaxYearCheckYourAnswers.donationAmount.label")
          )

        case None =>
          missingDataRow(
            messages("claimForTaxYearCheckYourAnswers.donationAmount.label"),
            controllers.giftAidSmallDonationsScheme.routes.DonationAmountYouAreClaimingController
              .onPageLoad(index, CheckMode)
              .url,
            messages("claimForTaxYearCheckYourAnswers.donationAmount.label")
          )
      }
    )

    SummaryList(rows)
  }

  private def summaryRow(label: String, value: String, href: String, hiddenText: String)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRow(
      key = Key(content = Text(label)),
      value = Value(content = Text(value)),
      actions = Some(
        Actions(items =
          Seq(
            ActionItem(
              href = href,
              content = Text(messages("site.change")),
              visuallyHiddenText = Some(hiddenText)
            )
          )
        )
      )
    )

  private def missingDataRow(label: String, href: String, hiddenText: String)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRow(
      key = Key(content = Text(label)),
      value = Value(
        content = HtmlContent(
          s"""<a class="govuk-link" href="$href">
             |${messages("site.enter")} <span class="govuk-visually-hidden">$hiddenText</span>
             |</a>""".stripMargin
        )
      ),
      actions = None
    )

  private def formatCurrency(amount: BigDecimal): String =
    s"£${amount.setScale(2, RoundingMode.HALF_UP)}"
}

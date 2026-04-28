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

import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, Key, SummaryListRow, Value}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import models.GiftAidSmallDonationsSchemeDonationDetailsAnswers
import scala.math.BigDecimal.RoundingMode

object GiftAidSmallDonationsSchemeDonationDetailsAnswersHelper {

  def buildSummaryListForAdjustmentToGiftAidOverclaimed(
    answers: Option[GiftAidSmallDonationsSchemeDonationDetailsAnswers]
  )(using messages: Messages): SummaryList =
    val label =
      messages("giftAidSmallDonationsSchemeDetailsCheckYourAnswers.adjustmentToGiftAidOverclaimed.label")

    val changeOrEnterUrl =
      controllers.giftAidSmallDonationsScheme.routes.GasdsAdjustmentAmountCheckYourAnswersController.onPageLoad.url

    val summaryListRow =
      answers
        .flatMap(_.adjustmentForGiftAidOverClaimed)
        .fold(
          missingDataRow(
            label,
            changeOrEnterUrl,
            label
          )
        )(value =>
          summaryRow(
            label,
            formatCurrency(value),
            changeOrEnterUrl,
            label
          )
        )

    SummaryList(Seq(summaryListRow))

  def buildSummaryListForNumberOfTaxYearsAdded(
    answers: Option[GiftAidSmallDonationsSchemeDonationDetailsAnswers]
  )(using messages: Messages): SummaryList =
    val label =
      messages("giftAidSmallDonationsSchemeDetailsCheckYourAnswers.numberOfTaxYearsAdded.label")

    val changeOrEnterUrl =
      controllers.giftAidSmallDonationsScheme.routes.ClaimAddedForTaxYearController.onPageLoad.url

    val taxYearsCount: Int = answers
      .flatMap(_.claims)
      .fold(0)(_.flatten.size)

    val summaryListRow =
      Option
        .when(taxYearsCount > 0)(taxYearsCount)
        .fold(
          missingDataRow(
            label,
            changeOrEnterUrl,
            label
          )
        )(count =>
          summaryRow(
            label,
            count.toString,
            changeOrEnterUrl,
            label
          )
        )

    SummaryList(Seq(summaryListRow))

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

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

package viewmodels

import models.GiftAidSmallDonationsSchemeDonationDetailsAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

object GasdsAdjustmentAmountCheckYourAnswersHelper {

  def buildSummaryList(
    answers: Option[GiftAidSmallDonationsSchemeDonationDetailsAnswers]
  )(implicit messages: Messages): SummaryList = {
    val rows = answers match {
      case Some(giftAidSmallDonationsSchemeDonationDetailsAnswers) =>
        Seq(
          giftAidSmallDonationsSchemeDonationDetailsAnswers.adjustmentForGiftAidOverClaimed match {
            case Some(value) =>
              summaryRow(
                messages(
                  "gasdsAdjustmentAmountCheckYourAnswers.adjustmentToGiftAidOverclaimed.label"
                ),
                formatCurrency(value),
                controllers.giftAidSmallDonationsScheme.routes.AdjustmentToGiftAidOverclaimedController.onPageLoad.url,
                messages("gasdsAdjustmentAmountCheckYourAnswers.adjustmentToGiftAidOverclaimed.label")
              )

            case _ =>
              missingDataRow(
                messages(
                  "gasdsAdjustmentAmountCheckYourAnswers.adjustmentToGiftAidOverclaimed.label"
                ),
                controllers.giftAidSmallDonationsScheme.routes.AdjustmentToGiftAidOverclaimedController.onPageLoad.url,
                messages("gasdsAdjustmentAmountCheckYourAnswers.adjustmentToGiftAidOverclaimed.label")
              )
          }
        )

      case None =>
        Seq(
          missingDataRow(
            messages("gasdsAdjustmentAmountCheckYourAnswers.adjustmentToGiftAidOverclaimed.label"),
            controllers.giftAidSmallDonationsScheme.routes.AdjustmentToGiftAidOverclaimedController.onPageLoad.url,
            messages("gasdsAdjustmentAmountCheckYourAnswers.adjustmentToGiftAidOverclaimed.label")
          )
        )

    }

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
    s"£${amount.setScale(2)}"
}

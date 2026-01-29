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

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import models.Mode.*
import models.RepaymentClaimDetailsAnswersOld
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent

object CheckYourAnswersHelper {

  def buildSummaryList(answers: RepaymentClaimDetailsAnswersOld)(implicit messages: Messages): SummaryList = {

    val rows = Seq(
      answers.claimingGiftAid match {
        case Some(value) =>
          Some(
            summaryRow(
              messages("checkYourAnswers.giftAid.label"),
              if (value) messages("site.yes") else messages("site.no"),
              controllers.repaymentclaimdetailsold.routes.ClaimingGiftAidController
                .onPageLoad(CheckMode)
                .url,
              messages("checkYourAnswers.giftAid.change.hidden")
            )
          )
        case None        =>
          Some(
            missingDataRow(
              messages("checkYourAnswers.giftAid.label"),
              controllers.repaymentclaimdetailsold.routes.ClaimingGiftAidController
                .onPageLoad(CheckMode)
                .url,
              messages("checkYourAnswers.giftAid.change.hidden")
            )
          )
      },
      answers.claimingTaxDeducted match {
        case Some(value) =>
          Some(
            summaryRow(
              messages("checkYourAnswers.taxDeducted.label"),
              if (value) messages("site.yes") else messages("site.no"),
              controllers.repaymentclaimdetailsold.routes.ClaimingOtherIncomeController
                .onPageLoad(CheckMode)
                .url,
              messages("checkYourAnswers.taxDeducted.label")
            )
          )
        case None        =>
          Some(
            missingDataRow(
              messages("checkYourAnswers.taxDeducted.label"),
              controllers.repaymentclaimdetailsold.routes.ClaimingOtherIncomeController
                .onPageLoad(CheckMode)
                .url,
              messages("checkYourAnswers.taxDeducted.label")
            )
          )
      },
      answers.claimingUnderGiftAidSmallDonationsScheme match {
        case Some(value) =>
          Some(
            summaryRow(
              messages("checkYourAnswers.giftAidSmallDonationsScheme.label"),
              if (value) messages("site.yes") else messages("site.no"),
              controllers.repaymentclaimdetailsold.routes.ClaimingGiftAidSmallDonationsController
                .onPageLoad(CheckMode)
                .url,
              messages("checkYourAnswers.giftAidSmallDonationsScheme.label")
            )
          )
        case None        =>
          Some(
            missingDataRow(
              messages("checkYourAnswers.giftAidSmallDonationsScheme.label"),
              controllers.repaymentclaimdetailsold.routes.ClaimingGiftAidSmallDonationsController
                .onPageLoad(CheckMode)
                .url,
              messages("checkYourAnswers.giftAidSmallDonationsScheme.label")
            )
          )
      },
      answers.claimingReferenceNumber match {
        case Some(value) =>
          Some(
            summaryRow(
              messages("checkYourAnswers.hasRef.label"),
              if (value) messages("site.yes") else messages("site.no"),
              controllers.repaymentclaimdetailsold.routes.ClaimingReferenceNumberCheckController
                .onPageLoad(CheckMode)
                .url,
              messages("checkYourAnswers.hasRef.label")
            )
          )
        case None        =>
          Some(
            missingDataRow(
              messages("checkYourAnswers.hasRef.label"),
              controllers.repaymentclaimdetailsold.routes.ClaimingReferenceNumberCheckController
                .onPageLoad(CheckMode)
                .url,
              messages("checkYourAnswers.hasRef.label")
            )
          )
      },
      answers.claimingReferenceNumber match {
        case Some(true) =>
          answers.claimReferenceNumber match {
            case Some(refNum) =>
              Some(
                summaryRow(
                  messages("checkYourAnswers.refNumber.label"),
                  refNum,
                  controllers.repaymentclaimdetailsold.routes.ClaimReferenceNumberInputControllerOLD
                    .onPageLoad(CheckMode)
                    .url,
                  messages("checkYourAnswers.refNumber.label")
                )
              )
            case None         =>
              Some(
                missingDataRow(
                  messages("checkYourAnswers.refNumber.label"),
                  controllers.repaymentclaimdetailsold.routes.ClaimReferenceNumberInputControllerOLD
                    .onPageLoad(CheckMode)
                    .url,
                  messages("checkYourAnswers.refNumber.label")
                )
              )
          }
        case _          => None
      }
    ).flatten

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
}

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

package viewmodels.govuk

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import models.SectionOneAnswers
import controllers.sectionone.routes

class CheckYourAnswersHelper {

  def buildSummaryList(answers: SectionOneAnswers)(implicit messages: Messages): SummaryList = {

    val rows = Seq(
      // Gift Aid Row
      Some(
        SummaryListRow(
          key = Key(content = Text(messages("checkYourAnswers.giftAid.label"))),
          value = Value(content = Text(if (answers.claimingGiftAid.getOrElse(false)) "Yes" else "No")),
          actions = Some(
            Actions(items =
              Seq(
                ActionItem(
                  href = routes.ClaimingGiftAidController.onPageLoad.url,
                  content = Text(messages("site.change")),
                  visuallyHiddenText = Some(messages("checkYourAnswers.giftAid.change.hidden"))
                )
              )
            )
          )
        )
      ),

      // Tax Deducted Row
      Some(
        SummaryListRow(
          key = Key(content = Text(messages("checkYourAnswers.taxDeducted.label"))),
          value = Value(content = Text(if (answers.claimingTaxDeducted.getOrElse(false)) "Yes" else "No")),
          actions = Some(
            Actions(items =
              Seq(
                ActionItem(
                  href = routes.ClaimingOtherIncomeController.onPageLoad.url,
                  content = Text(messages("site.change"))
                )
              )
            )
          )
        )
      ),

      // Small Donations (GASDS) Row
      Some(
        SummaryListRow(
          key = Key(content = Text(messages("checkYourAnswers.gasds.label"))),
          value = Value(content = Text(if (answers.claimingUnderGasds.getOrElse(false)) "Yes" else "No")),
          actions = Some(
            Actions(items =
              Seq(
                ActionItem(
                  href = routes.ClaimingGiftAidSmallDonationsController.onPageLoad.url,
                  content = Text(messages("site.change"))
                )
              )
            )
          )
        )
      )
      // Row 4 (Do you have a reference number?):
      // Row 5 (What is your reference number?):

    ).flatten

    SummaryList(rows)
  }
}

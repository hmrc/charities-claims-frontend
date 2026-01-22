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
import models.RepaymentClaimDetailsAnswers
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent

object RepaymentClaimDetailsCheckYourAnswersHelper {

  def buildSummaryList(answers: Option[RepaymentClaimDetailsAnswers])(implicit messages: Messages): SummaryList = {
    val rows = answers match {
      case Some(buildList) =>
        Seq(
          buildList.claimingDonationsCollectedInCommunityBuildings match {
            case Some(value) =>
              Some(
                summaryRow(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.claimingDonationsCollectedInCommunityBuildings.label"
                  ),
                  if (value) messages("site.yes") else messages("site.no"),
                  controllers.repaymentClaimDetails.routes.ClaimingCommunityBuildingDonationsController.onPageLoad.url,
                  messages("repaymentClaimDetailsCheckYourAnswers.claimingDonationsCollectedInCommunityBuildings.label")
                )
              )
            case _           =>
              Some(
                missingDataRow(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.claimingDonationsCollectedInCommunityBuildings.label"
                  ),
                  controllers.repaymentClaimDetails.routes.ClaimingCommunityBuildingDonationsController.onPageLoad.url,
                  messages("repaymentClaimDetailsCheckYourAnswers.claimingDonationsCollectedInCommunityBuildings.label")
                )
              )
          }
        ).flatten
      case _               =>
        Seq(
          missingDataRow(
            messages("repaymentClaimDetailsCheckYourAnswers.claimingDonationsCollectedInCommunityBuildings.label"),
            controllers.organisationDetails.routes.NameOfCharityRegulatorController
              .onPageLoad(CheckMode)
              .url,
            messages("repaymentClaimDetailsCheckYourAnswers.claimingDonationsCollectedInCommunityBuildings.label")
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

//  private def summaryRowHTML(label: String, value: String, href: String, hiddenText: String)(implicit
//    messages: Messages
//  ): SummaryListRow =
//    SummaryListRow(
//      key = Key(content = Text(label)),
//      value = Value(content = HtmlContent(value)),
//      actions = Some(
//        Actions(items =
//          Seq(
//            ActionItem(
//              href = href,
//              content = Text(messages("site.change")),
//              visuallyHiddenText = Some(hiddenText)
//            )
//          )
//        )
//      )
//    )

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

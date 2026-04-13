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

import models.SubmissionSummaryResponse
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter

object RepaymentClaimSummaryHelper {
  def claimDetails(summary: SubmissionSummaryResponse)(implicit messages: Messages): SummaryList =
    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm:ss").withZone(ZoneId.of("Europe/London"))

    SummaryList(
      rows = Seq(
        row(messages("repaymentClaimSummary.claimDetails.charityName.label"), summary.claimDetails.charityName),
        row(
          messages("repaymentClaimSummary.claimDetails.charityReference.label"),
          summary.claimDetails.hmrcCharityReference
        ),
        row(
          messages("repaymentClaimSummary.adjustments.submissionReceiptRefNumber.label"),
          summary.submissionReferenceNumber
        ),
        row(
          messages("repaymentClaimSummary.claimDetails.submissionDate.label"),
          formatter.format(Instant.parse(summary.claimDetails.submissionTimestamp))
        ),
        row(messages("repaymentClaimSummary.claimDetails.claimSubmittedBy.label"), summary.claimDetails.submittedBy)
      )
    )

  def giftAidDetails(summary: SubmissionSummaryResponse)(implicit messages: Messages): Option[SummaryList] =
    summary.giftAidDetails.map { ga =>
      SummaryList(
        rows = Seq(
          row(
            messages("repaymentClaimSummary.giftAidDetails.noOfGiftAidDonations.label"),
            ga.numberGiftAidDonations.toString
          ),
          row(
            messages("repaymentClaimSummary.giftAidDetails.totalValueOfGiftAidDonations.label"),
            formatCurrency(ga.totalValueGiftAidDonations)
          )
        )
      )
    }

  def otherIncomeDetails(summary: SubmissionSummaryResponse)(implicit messages: Messages): Option[SummaryList] =
    summary.otherIncomeDetails.map { oi =>
      SummaryList(
        rows = Seq(
          row(
            messages("repaymentClaimSummary.otherIncomeDetails.noOfOtherIncomeItems.label"),
            oi.numberOtherIncomeItems.toString
          ),
          row(
            messages("repaymentClaimSummary.otherIncomeDetails.totalValueOfOtherIncomeItems.label"),
            formatCurrency(oi.totalValueOtherIncomeItems)
          )
        )
      )
    }

  def gasdsDetails(summary: SubmissionSummaryResponse)(implicit messages: Messages): Option[SummaryList] =
    summary.gasdsDetails.map { g =>
      SummaryList(
        rows = Seq(
          g.totalValueGasdsNotInCommunityBuilding.map(v =>
            row(messages("repaymentClaimSummary.gasds.totalValueOfGASDS.label"), formatCurrency(v))
          ),
          g.numberCommunityBuildings.map(v =>
            row(messages("repaymentClaimSummary.gasds.noOfCommunityBuildings.label"), v.toString)
          ),
          g.totalValueGasdsInCommunityBuilding.map(v =>
            row(messages("repaymentClaimSummary.gasds.totalValueOfCommunityBuildings.label"), formatCurrency(v))
          ),
          g.numberConnectedCharities.map(v =>
            row(messages("repaymentClaimSummary.gasds.noOfConnectedCharities.label"), v.toString)
          )
        ).flatten
      )
    }

  def adjustmentDetails(summary: SubmissionSummaryResponse)(implicit messages: Messages): Option[SummaryList] =
    summary.adjustmentDetails.map { adj =>
      SummaryList(
        rows = Seq(
          adj.previouslyOverclaimedGiftAidOtherIncome.map(v =>
            row(messages("repaymentClaimSummary.adjustments.totalTaxRelief.label"), formatCurrency(v))
          ),
          adj.previouslyOverclaimedGasds.map(v =>
            row(messages("repaymentClaimSummary.adjustments.amountOfGASDS.label"), formatCurrency(v))
          )
        ).flatten
      )
    }

  private def row(key: String, value: String): SummaryListRow =
    SummaryListRow(
      key = Key(Text(key)),
      value = Value(Text(value))
    )

  private def formatCurrency(amount: BigDecimal): String =
    s"£${amount.setScale(2)}"
}

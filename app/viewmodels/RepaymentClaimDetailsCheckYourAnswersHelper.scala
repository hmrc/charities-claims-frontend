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
          {
            val values = Seq(
              buildList.claimingGiftAid.contains(true)                          ->
                messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.giftAid"),
              buildList.claimingUnderGiftAidSmallDonationsScheme.contains(true) ->
                messages(
                  "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.topUpPaymentForSmallCashDonationsGASDS"
                ),
              buildList.claimingTaxDeducted.contains(true)                      ->
                messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.UKTaxDeductedFromOtherIncome")
            ).collect { case (true, msg) => msg }

            if (values.nonEmpty) {
              Some(
                summaryRowHTML(
                  messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label"),
                  values.mkString("<br><br>"),
                  controllers.repaymentClaimDetails.routes.RepaymentClaimTypeController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label")
                )
              )
            } else {
              Some(
                missingDataRow(
                  messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label"),
                  controllers.repaymentClaimDetails.routes.RepaymentClaimTypeController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label")
                )
              )
            }
          },
          buildList.claimingReferenceNumber match {
            case Some(value) =>
              Some(
                summaryRow(
                  messages("repaymentClaimDetailsCheckYourAnswers.claimingReferenceNumber.label"),
                  if (value) messages("site.yes") else messages("site.no"),
                  controllers.repaymentClaimDetails.routes.ClaimingReferenceNumberController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("repaymentClaimDetailsCheckYourAnswers.claimingReferenceNumber.label")
                )
              )

            case None =>
              Some(
                missingDataRow(
                  messages("repaymentClaimDetailsCheckYourAnswers.claimingReferenceNumber.label"),
                  controllers.repaymentClaimDetails.routes.ClaimingReferenceNumberController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("repaymentClaimDetailsCheckYourAnswers.claimingReferenceNumber.label")
                )
              )
          },
          (buildList.claimingReferenceNumber, buildList.claimReferenceNumber) match {
            case (Some(true), Some(refNumber)) =>
              Some(
                summaryRow(
                  messages("repaymentClaimDetailsCheckYourAnswers.claimReferenceNumber.label"),
                  refNumber,
                  controllers.repaymentClaimDetails.routes.ClaimReferenceNumberInputController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("repaymentClaimDetailsCheckYourAnswers.claimReferenceNumber.label")
                )
              )
            case (Some(true), None)            =>
              Some(
                missingDataRow(
                  messages("repaymentClaimDetailsCheckYourAnswers.claimReferenceNumber.label"),
                  controllers.repaymentClaimDetails.routes.ClaimReferenceNumberInputController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("repaymentClaimDetailsCheckYourAnswers.claimReferenceNumber.label")
                )
              )

            case _ => None
          },
          buildList.claimingUnderGiftAidSmallDonationsScheme match {
            case Some(true) =>
              val values = Seq(
                buildList.claimingDonationsNotFromCommunityBuilding.contains(true)      ->
                  messages("repaymentClaimDetailsCheckYourAnswers.gasdsClaimType.topup"),
                buildList.claimingDonationsCollectedInCommunityBuildings.contains(true) ->
                  messages("repaymentClaimDetailsCheckYourAnswers.gasdsClaimType.communityBuildings"),
                buildList.connectedToAnyOtherCharities.contains(true)                   ->
                  messages("repaymentClaimDetailsCheckYourAnswers.gasdsClaimType.connectedCharity")
              ).collect { case (true, msg) => msg }

              if (values.nonEmpty) {
                Some(
                  summaryRowHTML(
                    messages("repaymentClaimDetailsCheckYourAnswers.gasdsClaimType.label"),
                    values.mkString("<br><br>"),
                    controllers.repaymentClaimDetails.routes.GasdsClaimTypeController
                      .onPageLoad(CheckMode)
                      .url,
                    messages("repaymentClaimDetailsCheckYourAnswers.gasdsClaimType.label")
                  )
                )
              } else {
                Some(
                  missingDataRow(
                    messages("repaymentClaimDetailsCheckYourAnswers.gasdsClaimType.label"),
                    controllers.repaymentClaimDetails.routes.GasdsClaimTypeController
                      .onPageLoad(CheckMode)
                      .url,
                    messages("repaymentClaimDetailsCheckYourAnswers.gasdsClaimType.label")
                  )
                )
              }

            case Some(false) => None
            case None        => None
          },
          (buildList.makingAdjustmentToPreviousClaim, buildList.claimingUnderGiftAidSmallDonationsScheme) match {

            case (Some(value), Some(true))
                if buildList.claimingDonationsCollectedInCommunityBuildings.contains(true) ||
                  buildList.claimingDonationsNotFromCommunityBuilding.contains(true) =>
              Some(
                summaryRow(
                  messages("repaymentClaimDetailsCheckYourAnswers.changeGASDSClaim.label"),
                  if (value) messages("site.yes") else messages("site.no"),
                  controllers.repaymentClaimDetails.routes.ChangePreviousGASDSClaimController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("repaymentClaimDetailsCheckYourAnswers.changeGASDSClaim.label")
                )
              )

            case (None, Some(true))
                if buildList.claimingDonationsCollectedInCommunityBuildings.contains(true) ||
                  buildList.claimingDonationsNotFromCommunityBuilding.contains(true) =>
              Some(
                missingDataRow(
                  messages("repaymentClaimDetailsCheckYourAnswers.changeGASDSClaim.label"),
                  controllers.repaymentClaimDetails.routes.ChangePreviousGASDSClaimController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("repaymentClaimDetailsCheckYourAnswers.changeGASDSClaim.label")
                )
              )

            case _ => None
          }
        ).flatten

      case None =>
        Seq(
          missingDataRow(
            messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label"),
            controllers.repaymentClaimDetails.routes.RepaymentClaimTypeController
              .onPageLoad(CheckMode)
              .url,
            messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label")
          ),
          missingDataRow(
            messages("repaymentClaimDetailsCheckYourAnswers.claimingReferenceNumber.label"),
            controllers.repaymentClaimDetails.routes.ClaimingReferenceNumberController
              .onPageLoad(CheckMode)
              .url,
            messages("repaymentClaimDetailsCheckYourAnswers.claimingReferenceNumber.label")
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

  private def summaryRowHTML(label: String, value: String, href: String, hiddenText: String)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRow(
      key = Key(content = Text(label)),
      value = Value(content = HtmlContent(value)),
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

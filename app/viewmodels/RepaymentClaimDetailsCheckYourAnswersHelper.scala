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
          (
            buildList.claimingGiftAid,
            buildList.claimingUnderGiftAidSmallDonationsScheme,
            buildList.claimingTaxDeducted
          ) match {
            case (Some(true), Some(true), Some(true)) | (Some(true), Some(true), _) | (Some(true), _, Some(true)) |
                (_, Some(true), Some(true)) =>
              Some(
                summaryRowHTML(
                  messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label"),
                  if buildList.claimingGiftAid.contains(true) && buildList.claimingUnderGiftAidSmallDonationsScheme
                      .contains(true) && buildList.claimingTaxDeducted.contains(true)
                  then
                    messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.giftAid")
                      + "<br>" + "<br>" +
                      messages(
                        "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.topUpPaymentForSmallCashDonationsGASDS"
                      )
                      + "<br>" + "<br>" +
                      messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.UKTaxDeductedFromOtherIncome")
                  else if buildList.claimingGiftAid.contains(true) && buildList.claimingTaxDeducted.contains(true) then
                    messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.giftAid")
                      + "<br>" + "<br>" +
                      messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.UKTaxDeductedFromOtherIncome")
                  else if buildList.claimingGiftAid.contains(true) && buildList.claimingUnderGiftAidSmallDonationsScheme
                      .contains(true)
                  then
                    messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.giftAid") + "<br>" + "<br>" +
                      messages(
                        "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.topUpPaymentForSmallCashDonationsGASDS"
                      )
                  else
                    messages(
                      "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.topUpPaymentForSmallCashDonationsGASDS"
                    ) + "<br>" + "<br>" +
                      messages(
                        "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.UKTaxDeductedFromOtherIncome"
                      )
                  ,
                  controllers.repaymentClaimDetails.routes.RepaymentClaimTypeController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("repaymentClaimDetailsCheckYourAnswers.claimReferenceNumber.label")
                )
              )

            case (_, _, _) =>
              if buildList.claimingGiftAid.contains(true) || buildList.claimingUnderGiftAidSmallDonationsScheme
                  .contains(true) || buildList.claimingTaxDeducted.contains(true)
              then
                Some(
                  summaryRow(
                    messages(
                      "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label"
                    ),
                    if buildList.claimingGiftAid.contains(true) then
                      messages(
                        "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.giftAid"
                      )
                    else if buildList.claimingTaxDeducted.contains(true) then
                      messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.UKTaxDeductedFromOtherIncome")
                    else
                      messages(
                        "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.topUpPaymentForSmallCashDonationsGASDS"
                      )
                    ,
                    controllers.repaymentClaimDetails.routes.RepaymentClaimTypeController
                      .onPageLoad(CheckMode)
                      .url,
                    messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label")
                  )
                )
              else
                Some(
                  missingDataRow(
                    messages(
                      "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label"
                    ),
                    controllers.repaymentClaimDetails.routes.RepaymentClaimTypeController
                      .onPageLoad(CheckMode)
                      .url,
                    messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label")
                  )
                )
          },
          buildList.claimingReferenceNumber match {
            case Some(value) =>
              Some(
                summaryRow(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.claimingReferenceNumber.label"
                  ),
                  if (value) messages("site.yes") else messages("site.no"),
                  controllers.repaymentClaimDetails.routes.ClaimingReferenceNumberController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("repaymentClaimDetailsCheckYourAnswers.claimingReferenceNumber.label")
                )
              )
            case _           =>
              Some(
                missingDataRow(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.claimingReferenceNumber.label"
                  ),
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
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.claimReferenceNumber.label"
                  ),
                  refNumber,
                  controllers.repaymentClaimDetails.routes.ClaimReferenceNumberInputController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("repaymentClaimDetailsCheckYourAnswers.claimReferenceNumber.label")
                )
              )
            case (Some(true), _)               =>
              Some(
                missingDataRow(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.claimReferenceNumber.label"
                  ),
                  controllers.repaymentClaimDetails.routes.ClaimReferenceNumberInputController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("repaymentClaimDetailsCheckYourAnswers.claimReferenceNumber.label")
                )
              )
            case (_, _)                        =>
              None
          },
          (
            buildList.claimingDonationsNotFromCommunityBuilding,
            buildList.claimingUnderGiftAidSmallDonationsScheme
          ) match {
            case (Some(value), Some(true)) =>
              Some(
                summaryRow(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.claimingDonationsNotFromCommunityBuilding.label"
                  ),
                  if (value) messages("site.yes") else messages("site.no"),
                  controllers.repaymentClaimDetails.routes.ClaimGiftAidSmallDonationsSchemeController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("repaymentClaimDetailsCheckYourAnswers.claimingDonationsNotFromCommunityBuilding.label")
                )
              )
            case (_, Some(true))           =>
              Some(
                missingDataRow(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.claimingDonationsNotFromCommunityBuilding.label"
                  ),
                  controllers.repaymentClaimDetails.routes.ClaimGiftAidSmallDonationsSchemeController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("repaymentClaimDetailsCheckYourAnswers.claimingDonationsNotFromCommunityBuilding.label")
                )
              )
            case (_, _)                    => None
          },
          (
            buildList.claimingDonationsCollectedInCommunityBuildings,
            buildList.claimingUnderGiftAidSmallDonationsScheme
          ) match {
            case (Some(value), Some(true)) =>
              Some(
                summaryRow(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.claimingDonationsCollectedInCommunityBuildings.label"
                  ),
                  if (value) messages("site.yes") else messages("site.no"),
                  controllers.repaymentClaimDetails.routes.ClaimingCommunityBuildingDonationsController
                    .onPageLoad(CheckMode)
                    .url,
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.claimingDonationsCollectedInCommunityBuildings.label"
                  )
                )
              )
            case (_, Some(true))           =>
              Some(
                missingDataRow(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.claimingDonationsCollectedInCommunityBuildings.label"
                  ),
                  controllers.repaymentClaimDetails.routes.ClaimingCommunityBuildingDonationsController
                    .onPageLoad(CheckMode)
                    .url,
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.claimingDonationsCollectedInCommunityBuildings.label"
                  )
                )
              )
            case (_, _)                    => None
          },
          (buildList.makingAdjustmentToPreviousClaim, buildList.claimingUnderGiftAidSmallDonationsScheme) match {
            case (Some(value), Some(true)) =>
              if (
                buildList.claimingDonationsCollectedInCommunityBuildings
                  .contains(true) || buildList.claimingDonationsNotFromCommunityBuilding.contains(true)
              )
                Some(
                  summaryRow(
                    messages(
                      "repaymentClaimDetailsCheckYourAnswers.changeGASDSClaim.label"
                    ),
                    if (value) messages("site.yes") else messages("site.no"),
                    controllers.repaymentClaimDetails.routes.ChangePreviousGASDSClaimController
                      .onPageLoad(CheckMode)
                      .url,
                    messages("repaymentClaimDetailsCheckYourAnswers.changeGASDSClaim.label")
                  )
                )
              else None
            case (_, Some(true))           =>
              if (
                buildList.claimingDonationsCollectedInCommunityBuildings
                  .contains(true) || buildList.claimingDonationsNotFromCommunityBuilding.contains(true)
              )
                Some(
                  missingDataRow(
                    messages(
                      "repaymentClaimDetailsCheckYourAnswers.changeGASDSClaim.label"
                    ),
                    controllers.repaymentClaimDetails.routes.ChangePreviousGASDSClaimController
                      .onPageLoad(CheckMode)
                      .url,
                    messages("repaymentClaimDetailsCheckYourAnswers.changeGASDSClaim.label")
                  )
                )
              else
                None
            case (_, _)                    =>
              None

          },
          (buildList.connectedToAnyOtherCharities, buildList.claimingUnderGiftAidSmallDonationsScheme) match {
            case (Some(value), Some(true)) =>
              Some(
                summaryRow(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.connectToAnyOtherCharity.label"
                  ),
                  if (value) messages("site.yes") else messages("site.no"),
                  controllers.repaymentClaimDetails.routes.ConnectedToAnyOtherCharitiesController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("repaymentClaimDetailsCheckYourAnswers.connectToAnyOtherCharity.label")
                )
              )
            case (_, Some(true))           =>
              Some(
                missingDataRow(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.connectToAnyOtherCharity.label"
                  ),
                  controllers.repaymentClaimDetails.routes.ConnectedToAnyOtherCharitiesController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("repaymentClaimDetailsCheckYourAnswers.connectToAnyOtherCharity.label")
                )
              )
            case (_, _)                    => None
          }
        ).flatten
      case _               =>
        Seq(
          missingDataRow(
            messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label"),
            controllers.repaymentClaimDetails.routes.RepaymentClaimTypeController
              .onPageLoad(CheckMode)
              .url,
            messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label")
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

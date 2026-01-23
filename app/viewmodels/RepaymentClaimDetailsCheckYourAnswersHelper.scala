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
            case (Some(true), Some(true), Some(true)) =>
              Some(
                summaryRowHTML(
                  messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label"),
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.giftAid.label" + "<br>" +
                      "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.topUpPaymentForSmallCashDonationsGASDS.label" + "<br>" +
                      "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.UKTaxDeductedFromOtherIncome.label"
                  ),
                  controllers.repaymentClaimDetails.routes.ClaimingCommunityBuildingDonationsController.onPageLoad.url, // TODO
                  messages("repaymentClaimDetailsCheckYourAnswers.claimReferenceNumber.label")
                )
              )
            case (Some(true), Some(true), _)          =>
              Some(
                summaryRowHTML(
                  messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label"),
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.giftAid.label" + "<br>" +
                      "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.topUpPaymentForSmallCashDonationsGASDS.label"
                  ),
                  controllers.repaymentClaimDetails.routes.ClaimingCommunityBuildingDonationsController.onPageLoad.url, // TODO
                  messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label")
                )
              )
            case (Some(true), _, Some(true))          =>
              Some(
                summaryRowHTML(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label"
                  ),
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.giftAid.label" + "<br>" +
                      "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.UKTaxDeductedFromOtherIncome.label"
                  ),
                  controllers.repaymentClaimDetails.routes.ClaimingCommunityBuildingDonationsController.onPageLoad.url, // TODO
                  messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label")
                )
              )
            case (_, Some(true), Some(true))          =>
              Some(
                summaryRowHTML(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label"
                  ),
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.topUpPaymentForSmallCashDonationsGASDS.label" + "<br>" +
                      "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.UKTaxDeductedFromOtherIncome.label"
                  ),
                  controllers.repaymentClaimDetails.routes.ClaimingCommunityBuildingDonationsController.onPageLoad.url, // TODO
                  messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label")
                )
              )
            case (Some(true), _, _)                   =>
              Some(
                summaryRowHTML(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label"
                  ),
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.giftAid.label"
                  ),
                  controllers.repaymentClaimDetails.routes.ClaimingCommunityBuildingDonationsController.onPageLoad.url, // TODO
                  messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label")
                )
              )
            case (_, Some(true), _)                   =>
              Some(
                summaryRowHTML(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label"
                  ),
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.topUpPaymentForSmallCashDonationsGASDS.label"
                  ),
                  controllers.repaymentClaimDetails.routes.ClaimingCommunityBuildingDonationsController.onPageLoad.url, // TODO
                  messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label")
                )
              )
            case (_, _, Some(true))                   =>
              Some(
                summaryRowHTML(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label"
                  ),
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.UKTaxDeductedFromOtherIncome.label"
                  ),
                  controllers.repaymentClaimDetails.routes.ClaimingCommunityBuildingDonationsController.onPageLoad.url, // TODO
                  messages("repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label")
                )
              )
            case (_, _, _)                            =>
              Some(
                missingDataRow(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.repaymentClaimType.label"
                  ),
                  controllers.repaymentClaimDetails.routes.ClaimingCommunityBuildingDonationsController.onPageLoad.url, // TODO
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
                  controllers.repaymentClaimDetails.routes.ClaimingCommunityBuildingDonationsController.onPageLoad.url, // TODO
                  messages("repaymentClaimDetailsCheckYourAnswers.claimingReferenceNumber.label")
                )
              )
            case _           =>
              Some(
                missingDataRow(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.claimingReferenceNumber.label"
                  ),
                  controllers.repaymentClaimDetails.routes.ClaimingCommunityBuildingDonationsController.onPageLoad.url, // TODO
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
                  controllers.repaymentClaimDetails.routes.ClaimingCommunityBuildingDonationsController.onPageLoad.url, // TODO
                  messages("repaymentClaimDetailsCheckYourAnswers.claimReferenceNumber.label")
                )
              )
            case (Some(true), _)               =>
              Some(
                missingDataRow(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.claimReferenceNumber.label"
                  ),
                  controllers.repaymentClaimDetails.routes.ClaimingCommunityBuildingDonationsController.onPageLoad.url, // TODO
                  messages("repaymentClaimDetailsCheckYourAnswers.claimReferenceNumber.label")
                )
              )
            case (_, _)                        =>
              None
          },
          buildList.claimingDonationsNotFromCommunityBuilding match {
            case Some(value) =>
              Some(
                summaryRow(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.claimingDonationsNotFromCommunityBuilding.label"
                  ),
                  if (value) messages("site.yes") else messages("site.no"),
                  controllers.repaymentClaimDetails.routes.ClaimingCommunityBuildingDonationsController.onPageLoad.url, // TODO
                  messages("repaymentClaimDetailsCheckYourAnswers.claimingDonationsNotFromCommunityBuilding.label")
                )
              )
            case _           =>
              Some(
                missingDataRow(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.claimingDonationsNotFromCommunityBuilding.label"
                  ),
                  controllers.repaymentClaimDetails.routes.ClaimingCommunityBuildingDonationsController.onPageLoad.url, // TODO
                  messages("repaymentClaimDetailsCheckYourAnswers.claimingDonationsNotFromCommunityBuilding.label")
                )
              )
          },
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
          },
          buildList.makingAdjustmentToPreviousClaim match {
            case Some(value) =>
              Some(
                summaryRow(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.changeGASDSClaim.label"
                  ),
                  if (value) messages("site.yes") else messages("site.no"),
                  controllers.repaymentClaimDetails.routes.ChangePreviousGASDSClaimController.onPageLoad.url,
                  messages("repaymentClaimDetailsCheckYourAnswers.changeGASDSClaim.label")
                )
              )
            case _           =>
              Some(
                missingDataRow(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.changeGASDSClaim.label"
                  ),
                  controllers.repaymentClaimDetails.routes.ChangePreviousGASDSClaimController.onPageLoad.url,
                  messages("repaymentClaimDetailsCheckYourAnswers.changeGASDSClaim.label")
                )
              )
          },
          buildList.connectedToAnyOtherCharities match {
            case Some(value) =>
              Some(
                summaryRow(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.connectToAnyOtherCharity.label"
                  ),
                  if (value) messages("site.yes") else messages("site.no"),
                  controllers.repaymentClaimDetails.routes.ChangePreviousGASDSClaimController.onPageLoad.url, // TODO
                  messages("repaymentClaimDetailsCheckYourAnswers.connectToAnyOtherCharity.label")
                )
              )
            case _           =>
              Some(
                missingDataRow(
                  messages(
                    "repaymentClaimDetailsCheckYourAnswers.connectToAnyOtherCharity.label"
                  ),
                  controllers.repaymentClaimDetails.routes.ChangePreviousGASDSClaimController.onPageLoad.url, // TODO
                  messages("repaymentClaimDetailsCheckYourAnswers.connectToAnyOtherCharity.label")
                )
              )
          }
        ).flatten
      case _               =>
        Seq(
          missingDataRow(
            // TODO
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

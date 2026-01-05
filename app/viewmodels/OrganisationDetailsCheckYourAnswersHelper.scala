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
import models.OrganisationDetailsAnswers
import models.{NameOfCharityRegulator, ReasonNotRegisteredWithRegulator}
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent

object OrganisationDetailsCheckYourAnswersHelper {

  def buildSummaryList(answers: Option[OrganisationDetailsAnswers])(implicit messages: Messages): SummaryList = {

    val rows = Seq(
      answers.nameOfCharityRegulator match {
        case value =>
          Some(
            summaryRow(
              messages("organisationDetailsCheckYourAnswers.nameOfCharityRegulator.label"),
              if {
                value.contains(NameOfCharityRegulator.EnglandAndWales)
                || value.contains(NameOfCharityRegulator.Scottish)
                || value.contains(NameOfCharityRegulator.NorthernIreland)
                || value.contains(NameOfCharityRegulator.None)
              } then messages("site.yes") else messages("site.no"),
              controllers.organisationDetails.routes.NameOfCharityRegulatorController
                .onPageLoad(CheckMode)
                .url,
              messages("organisationDetailsCheckYourAnswers.nameOfCharityRegulator.change.hidden")
            )
          )
        case _     =>
          Some(
            missingDataRow(
              messages("organisationDetailsCheckYourAnswers.giftAid.label"),
              controllers.organisationDetails.routes.NameOfCharityRegulatorController
                .onPageLoad(CheckMode)
                .url,
              messages("organisationDetailsCheckYourAnswers.giftAid.change.hidden")
            )
          )
      },
      answers.reasonNotRegisteredWithRegulator match {
        case value =>
          Some(
            summaryRow(
              messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label"),
              if {
                value.contains(ReasonNotRegisteredWithRegulator.Excepted)
                || value.contains(ReasonNotRegisteredWithRegulator.Exempt)
                || value.contains(ReasonNotRegisteredWithRegulator.LowIncome)
                || value.contains(ReasonNotRegisteredWithRegulator.Waiting)
              } then messages("site.yes") else messages("site.no"),
              controllers.organisationDetails.routes.ReasonNotRegisteredWithRegulatorController
                .onPageLoad(CheckMode)
                .url,
              messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label")
            )
          )
        case None  =>
          Some(
            missingDataRow(
              messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label"),
              controllers.organisationDetails.routes.ReasonNotRegisteredWithRegulatorController
                .onPageLoad(CheckMode)
                .url,
              messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label")
            )
          )
      },
      answers.charityRegistrationNumber match {
        case Some(regNum) =>
          Some(
            summaryRow(
              messages("organisationDetailsCheckYourAnswers.charityRegistrationNumber.label"),
              regNum,
              controllers.organisationDetails.routes.CharityRegulatorNumberController
                .onPageLoad(CheckMode)
                .url,
              messages("organisationDetailsCheckYourAnswers.charityRegistrationNumber.label")
            )
          )
        case None         =>
          Some(
            missingDataRow(
              messages("organisationDetailsCheckYourAnswers.charityRegistrationNumber.label"),
              controllers.organisationDetails.routes.CharityRegulatorNumberController
                .onPageLoad(CheckMode)
                .url,
              messages("organisationDetailsCheckYourAnswers.charityRegistrationNumber.label")
            )
          )
      },
      answers.areYouACorporateTrustee match {
        case Some(value) =>
          Some(
            summaryRow(
              messages("organisationDetailsCheckYourAnswers.areYouACorporateTrustee.label"),
              if (value) messages("site.yes") else messages("site.no"),
              controllers.organisationDetails.routes.CorporateTrusteeClaimController
                .onPageLoad(CheckMode)
                .url,
              messages("organisationDetailsCheckYourAnswers.areYouACorporateTrustee.label")
            )
          )
        case None        =>
          Some(
            missingDataRow(
              messages("organisationDetailsCheckYourAnswers.areYouACorporateTrustee.label"),
              controllers.organisationDetails.routes.CorporateTrusteeClaimController
                .onPageLoad(CheckMode)
                .url,
              messages("organisationDetailsCheckYourAnswers.areYouACorporateTrustee.label")
            )
          )
      },
      answers.doYouHaveUKAddress match {
        case Some(value) =>
          Some(
            summaryRow(
              messages("organisationDetailsCheckYourAnswers.doYouHaveUKAddress.label"),
              if (value) messages("site.yes") else messages("site.no"),
              controllers.organisationDetails.routes.CorporateTrusteeAddressController
                .onPageLoad(CheckMode)
                .url,
              messages("organisationDetailsCheckYourAnswers.doYouHaveUKAddress.label")
            )
          )
        case None        =>
          Some(
            missingDataRow(
              messages("organisationDetailsCheckYourAnswers.doYouHaveUKAddress.label"),
              controllers.organisationDetails.routes.CorporateTrusteeAddressController
                .onPageLoad(CheckMode)
                .url,
              messages("organisationDetailsCheckYourAnswers.doYouHaveUKAddress.label")
            )
          )
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

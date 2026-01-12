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
import models.NameOfCharityRegulator.{EnglandAndWales, NorthernIreland, Scottish}
import models.{NameOfCharityRegulator, OrganisationDetailsAnswers, ReasonNotRegisteredWithRegulator}
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent

object OrganisationDetailsCheckYourAnswersHelper {

  def buildSummaryList(answers: Option[OrganisationDetailsAnswers])(implicit messages: Messages): SummaryList = {
    val rows = answers match {
      case Some(buildList) =>
        Seq(
          buildList.nameOfCharityRegulator match {
            case value =>
              Some(
                summaryRow(
                  messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label"),
                  if value.contains(EnglandAndWales) then "Charity Commission for England and Wales"
                  else if value.contains(NorthernIreland) then "Charity Commission for Northern Ireland"
                  else if value.contains(Scottish) then "Office of the Scottish Charity Regulator"
                  else if value.contains(NameOfCharityRegulator.None) then "Charity is not registered with a regulator"
                  else "missing value",
                  controllers.organisationDetails.routes.NameOfCharityRegulatorController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("organisationDetailsCheckYourAnswers.charityRegulatorName.change.hidden")
                )
              )
            case _     =>
              Some(
                missingDataRow(
                  messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label"),
                  controllers.organisationDetails.routes.NameOfCharityRegulatorController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("organisationDetailsCheckYourAnswers.charityRegulatorName.change.hidden")
                )
              )
          },
          buildList.nameOfCharityRegulator match {
            case Some(NameOfCharityRegulator.None) =>
              buildList.reasonNotRegisteredWithRegulator match {
                case value =>
                  Some(
                    summaryRow(
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label"),
                      if value.contains(ReasonNotRegisteredWithRegulator.Excepted) then "Your charity is excepted"
                      else if value.contains(ReasonNotRegisteredWithRegulator.Exempt) then "Your charity is exempt"
                      else if value.contains(ReasonNotRegisteredWithRegulator.LowIncome) then
                        "Your charity is located in England or Wales and your income is less than £5,000 per year"
                      else "Your charity is based in Northern Ireland and you are awaiting registration with the CCNI",
                      controllers.organisationDetails.routes.ReasonNotRegisteredWithRegulatorController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label")
                    )
                  )
              }
            case _                                 =>
              buildList.charityRegistrationNumber match {
                case Some(regNum) =>
                  Some(
                    summaryRow(
                      messages("organisationDetailsCheckYourAnswers.charityRegulatorNumber.label"),
                      regNum,
                      controllers.organisationDetails.routes.CharityRegulatorNumberController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("organisationDetailsCheckYourAnswers.charityRegulatorNumber.change.hidden")
                    )
                  )
                case None         =>
                  Some(
                    missingDataRow(
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label"),
                      controllers.organisationDetails.routes.ReasonNotRegisteredWithRegulatorController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label")
                    )
                  )
              }
          },
          buildList.areYouACorporateTrustee match {
            case Some(value) =>
              Some(
                summaryRow(
                  messages("organisationDetailsCheckYourAnswers.CorporateTrusteeClaim.label"),
                  if (value) messages("site.yes") else messages("site.no"),
                  controllers.organisationDetails.routes.CorporateTrusteeClaimController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("organisationDetailsCheckYourAnswers.CorporateTrusteeClaim.change.hidden")
                )
              )
            case None        =>
              Some(
                missingDataRow(
                  messages("organisationDetailsCheckYourAnswers.CorporateTrusteeClaim.label"),
                  controllers.organisationDetails.routes.CorporateTrusteeClaimController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("organisationDetailsCheckYourAnswers.CorporateTrusteeClaim.change.hidden")
                )
              )
          },
          buildList.areYouACorporateTrustee match {
            case Some(true)  =>
              buildList.doYouHaveCorporateTrusteeUKAddress match {
                case Some(value) =>
                  Some(
                    summaryRow(
                      messages("organisationDetailsCheckYourAnswers.CorporateTrusteeUKAddress.label"),
                      if value then messages("site.yes") else messages("site.no"),
                      controllers.organisationDetails.routes.CorporateTrusteeAddressController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("organisationDetailsCheckYourAnswers.CorporateTrusteeUKAddress.label")
                    )
                  )
                case None        =>
                  Some(
                    missingDataRow(
                      messages("organisationDetailsCheckYourAnswers.CorporateTrusteeUKAddress.label"),
                      controllers.organisationDetails.routes.CorporateTrusteeAddressController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("organisationDetailsCheckYourAnswers.CorporateTrusteeUKAddress.label")
                    )
                  )
              }
            case Some(false) =>
              buildList.doYouHaveAuthorisedOfficialTrusteeUKAddress match {
                case Some(true) =>
                  Some(
                    summaryRow(
                      messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeUKAddress.label"),
                      if true then messages("site.yes") else messages("site.no"),
                      controllers.organisationDetails.routes.AuthorisedOfficialAddressController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeUKAddress.label")
                    )
                  )
              }
          }
        ).flatten

      case None => Nil
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
}

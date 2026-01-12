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
                  if value.contains(EnglandAndWales) then
                    messages("organisationDetailsCheckYourAnswers.charityRegulatorName.EnglandAndWales.label")
                  else if value.contains(NorthernIreland) then
                    messages("organisationDetailsCheckYourAnswers.charityRegulatorName.NorthernIreland.label")
                  else if value.contains(Scottish) then
                    messages("organisationDetailsCheckYourAnswers.charityRegulatorName.Scottish.label")
                  else if value.contains(NameOfCharityRegulator.None) then
                    messages("organisationDetailsCheckYourAnswers.charityRegulatorName.none.label")
                  else messages("organisationDetailsCheckYourAnswers.charityRegulatorName.missing.label"),
                  controllers.organisationDetails.routes.NameOfCharityRegulatorController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("organisationDetailsCheckYourAnswers.charityRegulatorName.change.hidden")
                )
              )
//            case _     =>
//              Some(
//                missingDataRow(
//                  messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label"),
//                  controllers.organisationDetails.routes.NameOfCharityRegulatorController
//                    .onPageLoad(CheckMode)
//                    .url,
//                  messages("organisationDetailsCheckYourAnswers.charityRegulatorName.change.hidden")
//                )
//              )
          },
          buildList.nameOfCharityRegulator match {
            case Some(NameOfCharityRegulator.None) =>
              buildList.reasonNotRegisteredWithRegulator match {
                case value =>
                  Some(
                    summaryRow(
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label"),
                      if value.contains(ReasonNotRegisteredWithRegulator.Excepted) then
                        messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.excepted.label")
                      else if value.contains(ReasonNotRegisteredWithRegulator.Exempt) then
                        messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.exempt.label")
                      else if value.contains(ReasonNotRegisteredWithRegulator.LowIncome) then
                        messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.lowIncome.label")
                      else if value.contains(ReasonNotRegisteredWithRegulator.Waiting) then
                        messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.waiting.label")
                      else messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.missing.label"),
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
            case _           =>
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
            case Some(true) =>
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
            case _          =>
              buildList.doYouHaveAuthorisedOfficialTrusteeUKAddress match {
                case Some(value) =>
                  Some(
                    summaryRow(
                      messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeUKAddress.label"),
                      if value then messages("site.yes") else messages("site.no"),
                      controllers.organisationDetails.routes.AuthorisedOfficialAddressController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeUKAddress.label")
                    )
                  )
                case _           =>
                  Some(
                    missingDataRow(
                      messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeUKAddress.label"),
                      controllers.organisationDetails.routes.AuthorisedOfficialAddressController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeUKAddress.label")
                    )
                  )
              }

          },
          (buildList.areYouACorporateTrustee, buildList.doYouHaveCorporateTrusteeUKAddress).match {
            case (Some(true), Some(false)) =>
              Some(
                summaryRow(
                  messages("organisationDetailsCheckYourAnswers.CorporateTrusteeDetails.label"),
                  messages(
                    buildList.nameOfCorporateTrustee.toString,
                    "<BR>",
                    buildList.corporateTrusteeDaytimeTelephoneNumber
                  ),
                  controllers.organisationDetails.routes.CorporateTrusteeDetailsController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("organisationDetailsCheckYourAnswers.CorporateTrusteeDetails.label")
                )
              )
            case (Some(true), Some(true))  =>
              Some(
                summaryRow(
                  messages("organisationDetailsCheckYourAnswers.CorporateTrusteeDetails.label"),
                  messages(
                    buildList.nameOfCorporateTrustee.toString,
                    "<BR>",
                    buildList.corporateTrusteeDaytimeTelephoneNumber,
                    "<BR>",
                    buildList.corporateTrusteePostcode
                  ),
                  controllers.organisationDetails.routes.CorporateTrusteeDetailsController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("organisationDetailsCheckYourAnswers.CorporateTrusteeDetails.label")
                )
              )
            case (_, _)                    =>
              Some(
                missingDataRow(
                  messages("organisationDetailsCheckYourAnswers.CorporateTrusteeDetails.label"),
                  controllers.organisationDetails.routes.CorporateTrusteeDetailsController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("organisationDetailsCheckYourAnswers.CorporateTrusteeDetails.change.hidden")
                )
              )
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

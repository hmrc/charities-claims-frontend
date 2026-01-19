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
            case Some(EnglandAndWales)             =>
              Some(
                summaryRow(
                  messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label"),
                  messages("organisationDetailsCheckYourAnswers.charityRegulatorName.EnglandAndWales.label"),
                  controllers.organisationDetails.routes.NameOfCharityRegulatorController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label")
                )
              )
            case Some(NorthernIreland)             =>
              Some(
                summaryRow(
                  messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label"),
                  messages("organisationDetailsCheckYourAnswers.charityRegulatorName.NorthernIreland.label"),
                  controllers.organisationDetails.routes.NameOfCharityRegulatorController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label")
                )
              )
            case Some(Scottish)                    =>
              Some(
                summaryRow(
                  messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label"),
                  messages("organisationDetailsCheckYourAnswers.charityRegulatorName.Scottish.label"),
                  controllers.organisationDetails.routes.NameOfCharityRegulatorController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label")
                )
              )
            case Some(NameOfCharityRegulator.None) =>
              Some(
                summaryRow(
                  messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label"),
                  messages("organisationDetailsCheckYourAnswers.charityRegulatorName.none.label"),
                  controllers.organisationDetails.routes.NameOfCharityRegulatorController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label")
                )
              )
            case _                                 =>
              Some(
                missingDataRow(
                  messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label"),
                  controllers.organisationDetails.routes.NameOfCharityRegulatorController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label")
                )
              )
          },
          buildList.nameOfCharityRegulator match {
            case Some(NameOfCharityRegulator.None)                              =>
              buildList.reasonNotRegisteredWithRegulator match {
                case Some(ReasonNotRegisteredWithRegulator.Excepted)  =>
                  Some(
                    summaryRow(
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label"),
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.excepted.label"),
                      controllers.organisationDetails.routes.ReasonNotRegisteredWithRegulatorController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label")
                    )
                  )
                case Some(ReasonNotRegisteredWithRegulator.Exempt)    =>
                  Some(
                    summaryRow(
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label"),
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.exempt.label"),
                      controllers.organisationDetails.routes.ReasonNotRegisteredWithRegulatorController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label")
                    )
                  )
                case Some(ReasonNotRegisteredWithRegulator.LowIncome) =>
                  Some(
                    summaryRow(
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label"),
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.lowIncome.label"),
                      controllers.organisationDetails.routes.ReasonNotRegisteredWithRegulatorController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label")
                    )
                  )
                case Some(ReasonNotRegisteredWithRegulator.Waiting)   =>
                  Some(
                    summaryRow(
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label"),
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.waiting.label"),
                      controllers.organisationDetails.routes.ReasonNotRegisteredWithRegulatorController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label")
                    )
                  )
                case _                                                =>
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
            case Some(NorthernIreland) | Some(Scottish) | Some(EnglandAndWales) =>
              buildList.charityRegistrationNumber match {
                case Some(regNum) =>
                  Some(
                    summaryRow(
                      messages("organisationDetailsCheckYourAnswers.charityRegulatorNumber.label"),
                      regNum,
                      controllers.organisationDetails.routes.CharityRegulatorNumberController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("organisationDetailsCheckYourAnswers.charityRegulatorNumber.label")
                    )
                  )
                case _            =>
                  Some(
                    missingDataRow(
                      messages("organisationDetailsCheckYourAnswers.charityRegulatorNumber.label"),
                      controllers.organisationDetails.routes.CharityRegulatorNumberController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("organisationDetailsCheckYourAnswers.charityRegulatorNumber.label")
                    )
                  )
              }
            case _                                                              =>
              None
          },
          buildList.areYouACorporateTrustee match {
            case Some(value) =>
              // change screen for are you corporate trustee
              Some(
                summaryRow(
                  messages("organisationDetailsCheckYourAnswers.CorporateTrusteeClaim.label"),
                  if (value) messages("site.yes") else messages("site.no"),
                  controllers.organisationDetails.routes.CorporateTrusteeClaimController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("organisationDetailsCheckYourAnswers.CorporateTrusteeClaim.label")
                )
              )
            case _           =>
              // missing are you a corporate trustee
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
          (buildList.areYouACorporateTrustee, buildList.doYouHaveCorporateTrusteeUKAddress) match {
            case (Some(true), _)  =>
              // is corporate trustee
              buildList.doYouHaveCorporateTrusteeUKAddress match {
                case Some(value) =>
                  // display change - do you have corporate trustee UK address
                  Some(
                    summaryRow(
                      messages("organisationDetailsCheckYourAnswers.CorporateTrusteeUKAddress.label"),
                      if (value) messages("site.yes") else messages("site.no"),
                      controllers.organisationDetails.routes.CorporateTrusteeAddressController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("organisationDetailsCheckYourAnswers.CorporateTrusteeUKAddress.label")
                    )
                  )
                case _           =>
                  // missing -  you have corporate trustee UK address
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
            case (Some(false), _) =>
              buildList.doYouHaveAuthorisedOfficialTrusteeUKAddress match {
                case Some(value) =>
                  // display change - do you have authorised official UK address
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
                  // missing -  you have authorised official UK address
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
            case (_, _)           =>
              None
          },
          (
            buildList.areYouACorporateTrustee,
            buildList.doYouHaveCorporateTrusteeUKAddress,
            buildList.doYouHaveAuthorisedOfficialTrusteeUKAddress
          ) match {
            case (Some(true), Some(false), _)  =>
              if buildList.nameOfCorporateTrustee.isDefined | buildList.corporateTrusteeDaytimeTelephoneNumber.isDefined
              then
                Some(
                  summaryRowHTML(
                    messages("organisationDetailsCheckYourAnswers.CorporateTrusteeDetails.label"),
                    messages(buildList.nameOfCorporateTrustee.getOrElse(" ")) +
                      "<br>" +
                      messages(
                        buildList.corporateTrusteeDaytimeTelephoneNumber
                          .getOrElse(" ")
                      ),
                    controllers.organisationDetails.routes.CorporateTrusteeDetailsController
                      .onPageLoad(CheckMode)
                      .url,
                    messages("organisationDetailsCheckYourAnswers.CorporateTrusteeDetails.label")
                  )
                )
              else
                Some(
                  missingDataRow(
                    messages("organisationDetailsCheckYourAnswers.CorporateTrusteeDetails.label"),
                    controllers.organisationDetails.routes.CorporateTrusteeDetailsController
                      .onPageLoad(CheckMode)
                      .url,
                    messages("organisationDetailsCheckYourAnswers.CorporateTrusteeDetails.change.hidden")
                  )
                )
            case (Some(true), Some(true), _)   =>
              if buildList.nameOfCorporateTrustee.isDefined | buildList.corporateTrusteeDaytimeTelephoneNumber.isDefined
              then
                Some(
                  summaryRowHTML(
                    messages("organisationDetailsCheckYourAnswers.CorporateTrusteeDetails.label"),
                    messages(buildList.nameOfCorporateTrustee.getOrElse(" ")) +
                      "<br>" +
                      messages(
                        buildList.corporateTrusteeDaytimeTelephoneNumber
                          .getOrElse(" ")
                      ) +
                      "<br>" +
                      messages(buildList.corporateTrusteePostcode.getOrElse(" ")),
                    controllers.organisationDetails.routes.CorporateTrusteeDetailsController
                      .onPageLoad(CheckMode)
                      .url,
                    messages("organisationDetailsCheckYourAnswers.CorporateTrusteeDetails.label")
                  )
                )
              else
                Some(
                  missingDataRow(
                    messages("organisationDetailsCheckYourAnswers.CorporateTrusteeDetails.label"),
                    controllers.organisationDetails.routes.CorporateTrusteeDetailsController
                      .onPageLoad(CheckMode)
                      .url,
                    messages("organisationDetailsCheckYourAnswers.CorporateTrusteeDetails.change.hidden")
                  )
                )
            case (Some(true), _, _)            =>
              None
            case (Some(false), _, Some(false)) =>
              if buildList.authorisedOfficialTrusteeFirstName.isDefined | buildList.authorisedOfficialTrusteeLastName.isDefined | buildList.authorisedOfficialTrusteeDaytimeTelephoneNumber.isDefined
              then
                Some(
                  summaryRowHTML(
                    messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeDetails.label"),
                    messages(buildList.authorisedOfficialTrusteeTitle.getOrElse("")) +
                      "<br>" +
                      messages(buildList.authorisedOfficialTrusteeFirstName.getOrElse(""))
                      + "<br>" +
                      messages(buildList.authorisedOfficialTrusteeLastName.getOrElse(""))
                      + "<br>" +
                      messages(buildList.authorisedOfficialTrusteeDaytimeTelephoneNumber.getOrElse("")),
                    controllers.organisationDetails.routes.AuthorisedOfficialDetailsController
                      .onPageLoad(CheckMode)
                      .url,
                    messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeDetails.label")
                  )
                )
              else
                Some(
                  missingDataRow(
                    messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeDetails.label"),
                    controllers.organisationDetails.routes.AuthorisedOfficialDetailsController
                      .onPageLoad(CheckMode)
                      .url,
                    messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeDetails.change.hidden")
                  )
                )
            case (Some(false), _, Some(true))  =>
              if buildList.authorisedOfficialTrusteeFirstName.isDefined | buildList.authorisedOfficialTrusteeLastName.isDefined | buildList.authorisedOfficialTrusteeDaytimeTelephoneNumber.isDefined
              then
                Some(
                  summaryRowHTML(
                    messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeDetails.label"),
                    messages(buildList.authorisedOfficialTrusteeTitle.getOrElse("")) +
                      "<br>" +
                      messages(buildList.authorisedOfficialTrusteeFirstName.getOrElse(""))
                      + "<br>" +
                      messages(buildList.authorisedOfficialTrusteeLastName.getOrElse(""))
                      + "<br>" +
                      messages(buildList.authorisedOfficialTrusteeDaytimeTelephoneNumber.getOrElse(""))
                      + "<br>" +
                      messages(buildList.authorisedOfficialTrusteePostcode.getOrElse("")),
                    controllers.organisationDetails.routes.AuthorisedOfficialDetailsController
                      .onPageLoad(CheckMode)
                      .url,
                    messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeDetails.label")
                  )
                )
              else
                Some(
                  missingDataRow(
                    messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeDetails.label"),
                    controllers.organisationDetails.routes.AuthorisedOfficialDetailsController
                      .onPageLoad(CheckMode)
                      .url,
                    messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeDetails.change.hidden")
                  )
                )
            case (Some(false), _, _)           =>
              None
            case (_, _, _)                     =>
              None
          }
        ).flatten

      case None => // first screen (charity regulator name) and are you corporate trustee are independent screens
        Seq(
          missingDataRow(
            messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label"),
            controllers.organisationDetails.routes.NameOfCharityRegulatorController
              .onPageLoad(CheckMode)
              .url,
            messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label")
          ),
          missingDataRow(
            messages("organisationDetailsCheckYourAnswers.CorporateTrusteeClaim.label"),
            controllers.organisationDetails.routes.CorporateTrusteeClaimController
              .onPageLoad(CheckMode)
              .url,
            messages("organisationDetailsCheckYourAnswers.CorporateTrusteeClaim.change.hidden")
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

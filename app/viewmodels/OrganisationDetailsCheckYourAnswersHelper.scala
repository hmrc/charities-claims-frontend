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

import models.Mode.*
import models.NameOfCharityRegulator.{EnglandAndWales, NorthernIreland, Scottish}
import models.*
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

final case class OrganisationDetailsSummary(
  charityDetails: SummaryList,
  corporateTrusteeDetails: Option[SummaryList],
  authorisedOfficialDetails: Option[SummaryList]
)

object OrganisationDetailsCheckYourAnswersHelper {

  def buildSummaryLists(
    answers: Option[OrganisationDetailsAnswers],
    isCASCCharityRef: Boolean
  )(implicit messages: Messages): OrganisationDetailsSummary = {

    val charityRows = buildCharityRows(answers, isCASCCharityRef)

    val corporateTrusteeRows =
      answers.flatMap { agentOrgUserAnswers =>
        agentOrgUserAnswers.areYouACorporateTrustee match {
          case Some(true) =>
            Some(buildCorporateTrusteeRows(agentOrgUserAnswers))

          case _ =>
            None
        }
      }

    val authorisedOfficialRows =
      answers.flatMap { agentOrgUserAnswers =>
        agentOrgUserAnswers.areYouACorporateTrustee match {
          case Some(false) =>
            Some(buildAuthorisedOfficialRows(agentOrgUserAnswers))

          case _ =>
            None
        }
      }

    OrganisationDetailsSummary(
      charityDetails = SummaryList(charityRows),
      corporateTrusteeDetails = corporateTrusteeRows.map(rows => SummaryList(rows)),
      authorisedOfficialDetails = authorisedOfficialRows.map(rows => SummaryList(rows))
    )
  }

  private def buildCharityRows(
    answers: Option[OrganisationDetailsAnswers],
    isCASCCharityRef: Boolean
  )(implicit messages: Messages): Seq[SummaryListRow] = {

    answers
      .map { buildList =>
        Seq(
          if (isCASCCharityRef) { None }
          else {
            Some(
              buildList.nameOfCharityRegulator match {

                case Some(EnglandAndWales) =>
                  summaryRow(
                    messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label"),
                    Text(
                      messages(
                        "organisationDetailsCheckYourAnswers.charityRegulatorName.EnglandAndWales.label"
                      )
                    ),
                    controllers.organisationDetails.routes.NameOfCharityRegulatorController
                      .onPageLoad(CheckMode)
                      .url,
                    messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label")
                  )

                case Some(NorthernIreland) =>
                  summaryRow(
                    messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label"),
                    Text(
                      messages(
                        "organisationDetailsCheckYourAnswers.charityRegulatorName.NorthernIreland.label"
                      )
                    ),
                    controllers.organisationDetails.routes.NameOfCharityRegulatorController
                      .onPageLoad(CheckMode)
                      .url,
                    messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label")
                  )

                case Some(Scottish) =>
                  summaryRow(
                    messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label"),
                    Text(
                      messages(
                        "organisationDetailsCheckYourAnswers.charityRegulatorName.Scottish.label"
                      )
                    ),
                    controllers.organisationDetails.routes.NameOfCharityRegulatorController
                      .onPageLoad(CheckMode)
                      .url,
                    messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label")
                  )

                case Some(NameOfCharityRegulator.None) =>
                  summaryRow(
                    messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label"),
                    Text(
                      messages(
                        "organisationDetailsCheckYourAnswers.charityRegulatorName.none.label"
                      )
                    ),
                    controllers.organisationDetails.routes.NameOfCharityRegulatorController
                      .onPageLoad(CheckMode)
                      .url,
                    messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label")
                  )

                case _ =>
                  missingDataRow(
                    messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label"),
                    controllers.organisationDetails.routes.NameOfCharityRegulatorController
                      .onPageLoad(CheckMode)
                      .url,
                    messages("organisationDetailsCheckYourAnswers.charityRegulatorName.label")
                  )
              }
            )
          },
          buildList.nameOfCharityRegulator match {

            case Some(NameOfCharityRegulator.None)                              =>
              Some(
                buildList.reasonNotRegisteredWithRegulator match {

                  case Some(ReasonNotRegisteredWithRegulator.Excepted) =>
                    summaryRow(
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label"),
                      exceptedReasonContent,
                      controllers.organisationDetails.routes.ReasonNotRegisteredWithRegulatorController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label")
                    )

                  case Some(ReasonNotRegisteredWithRegulator.Exempt) =>
                    summaryRow(
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label"),
                      exemptReasonContent,
                      controllers.organisationDetails.routes.ReasonNotRegisteredWithRegulatorController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label")
                    )

                  case Some(reason) =>
                    summaryRow(
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label"),
                      Text(
                        messages(
                          s"organisationDetailsCheckYourAnswers.reasonNotRegistered.$reason.label"
                        )
                      ),
                      controllers.organisationDetails.routes.ReasonNotRegisteredWithRegulatorController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label")
                    )

                  case None =>
                    missingDataRow(
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label"),
                      controllers.organisationDetails.routes.ReasonNotRegisteredWithRegulatorController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("organisationDetailsCheckYourAnswers.reasonNotRegistered.label")
                    )
                }
              )
            case Some(NorthernIreland) | Some(Scottish) | Some(EnglandAndWales) =>
              Some(
                buildList.charityRegistrationNumber match {

                  case Some(regNum) =>
                    summaryRow(
                      messages("organisationDetailsCheckYourAnswers.charityRegulatorNumber.label"),
                      Text(regNum),
                      controllers.organisationDetails.routes.CharityRegulatorNumberController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("organisationDetailsCheckYourAnswers.charityRegulatorNumber.label")
                    )

                  case None =>
                    missingDataRow(
                      messages("organisationDetailsCheckYourAnswers.charityRegulatorNumber.label"),
                      controllers.organisationDetails.routes.CharityRegulatorNumberController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("organisationDetailsCheckYourAnswers.charityRegulatorNumber.label")
                    )
                }
              )

            case _ =>
              None
          },
          Some(
            buildList.areYouACorporateTrustee match {

              case Some(value) =>
                summaryRow(
                  messages("organisationDetailsCheckYourAnswers.CorporateTrusteeClaim.label"),
                  Text(if (value) messages("site.yes") else messages("site.no")),
                  controllers.organisationDetails.routes.CorporateTrusteeClaimController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("organisationDetailsCheckYourAnswers.CorporateTrusteeClaim.label")
                )

              case None =>
                missingDataRow(
                  messages("organisationDetailsCheckYourAnswers.CorporateTrusteeClaim.label"),
                  controllers.organisationDetails.routes.CorporateTrusteeClaimController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("organisationDetailsCheckYourAnswers.CorporateTrusteeClaim.label")
                )
            }
          )
        ).flatten
      }
      .getOrElse(
        if (isCASCCharityRef) {
          Seq(
            missingDataRow(
              messages("organisationDetailsCheckYourAnswers.CorporateTrusteeClaim.label"),
              controllers.organisationDetails.routes.CorporateTrusteeClaimController
                .onPageLoad(CheckMode)
                .url,
              messages("organisationDetailsCheckYourAnswers.CorporateTrusteeClaim.label")
            )
          )
        } else {
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
              messages("organisationDetailsCheckYourAnswers.CorporateTrusteeClaim.label")
            )
          )
        }
      )
  }

  private def buildCorporateTrusteeRows(
    answers: OrganisationDetailsAnswers
  )(implicit messages: Messages): Seq[SummaryListRow] =
    Seq(
      Some(
        answers.doYouHaveCorporateTrusteeUKAddress match {

          case Some(value) =>
            summaryRow(
              messages("organisationDetailsCheckYourAnswers.CorporateTrusteeUKAddress.label"),
              Text(if (value) messages("site.yes") else messages("site.no")),
              controllers.organisationDetails.routes.CorporateTrusteeAddressController
                .onPageLoad(CheckMode)
                .url,
              messages("organisationDetailsCheckYourAnswers.CorporateTrusteeUKAddress.label")
            )

          case None =>
            missingDataRow(
              messages("organisationDetailsCheckYourAnswers.CorporateTrusteeUKAddress.label"),
              controllers.organisationDetails.routes.CorporateTrusteeAddressController
                .onPageLoad(CheckMode)
                .url,
              messages("organisationDetailsCheckYourAnswers.CorporateTrusteeUKAddress.label")
            )
        }
      ),
      answers.doYouHaveCorporateTrusteeUKAddress.map { hasUkAddress =>

        val details =
          if (hasUkAddress) {
            Seq(
              answers.nameOfCorporateTrustee,
              answers.corporateTrusteeDaytimeTelephoneNumber,
              answers.corporateTrusteePostcode
            ).flatten
          } else {
            Seq(
              answers.nameOfCorporateTrustee,
              answers.corporateTrusteeDaytimeTelephoneNumber
            ).flatten
          }

        if (details.nonEmpty) {
          summaryRow(
            messages("organisationDetailsCheckYourAnswers.CorporateTrusteeDetails.label"),
            HtmlContent(details.mkString("<br>")),
            controllers.organisationDetails.routes.CorporateTrusteeDetailsController
              .onPageLoad(CheckMode)
              .url,
            messages("organisationDetailsCheckYourAnswers.CorporateTrusteeDetails.label")
          )
        } else {
          missingDataRow(
            messages("organisationDetailsCheckYourAnswers.CorporateTrusteeDetails.label"),
            controllers.organisationDetails.routes.CorporateTrusteeDetailsController
              .onPageLoad(CheckMode)
              .url,
            messages("organisationDetailsCheckYourAnswers.CorporateTrusteeDetails.label")
          )
        }
      }
    ).flatten

  private def buildAuthorisedOfficialRows(
    answers: OrganisationDetailsAnswers
  )(implicit messages: Messages): Seq[SummaryListRow] =
    Seq(
      Some(
        answers.doYouHaveAuthorisedOfficialTrusteeUKAddress match {

          case Some(value) =>
            summaryRow(
              messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeUKAddress.label"),
              Text(if (value) messages("site.yes") else messages("site.no")),
              controllers.organisationDetails.routes.AuthorisedOfficialAddressController
                .onPageLoad(CheckMode)
                .url,
              messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeUKAddress.label")
            )

          case None =>
            missingDataRow(
              messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeUKAddress.label"),
              controllers.organisationDetails.routes.AuthorisedOfficialAddressController
                .onPageLoad(CheckMode)
                .url,
              messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeUKAddress.label")
            )
        }
      ),
      answers.doYouHaveAuthorisedOfficialTrusteeUKAddress.map { hasUkAddress =>

        val details =
          if (hasUkAddress) {
            Seq(
              answers.authorisedOfficialTrusteeTitle,
              answers.authorisedOfficialTrusteeFirstName,
              answers.authorisedOfficialTrusteeLastName,
              answers.authorisedOfficialTrusteeDaytimeTelephoneNumber,
              answers.authorisedOfficialTrusteePostcode
            ).flatten
          } else {
            Seq(
              answers.authorisedOfficialTrusteeTitle,
              answers.authorisedOfficialTrusteeFirstName,
              answers.authorisedOfficialTrusteeLastName,
              answers.authorisedOfficialTrusteeDaytimeTelephoneNumber
            ).flatten
          }

        if (details.nonEmpty) {
          summaryRow(
            messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeDetails.label"),
            HtmlContent(details.mkString("<br>")),
            controllers.organisationDetails.routes.AuthorisedOfficialDetailsController
              .onPageLoad(CheckMode)
              .url,
            messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeDetails.label")
          )
        } else {
          missingDataRow(
            messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeDetails.label"),
            controllers.organisationDetails.routes.AuthorisedOfficialDetailsController
              .onPageLoad(CheckMode)
              .url,
            messages("organisationDetailsCheckYourAnswers.AuthorisedOfficialTrusteeDetails.label")
          )
        }
      }
    ).flatten

  private def exceptedReasonContent(implicit messages: Messages): HtmlContent =
    HtmlContent(
      s"""
         |<div style="max-width: 520px;">
         |  <p class="govuk-!-margin-top-0 govuk-!-margin-bottom-2">
         |    ${messages("reasonNotRegistered.excepted.heading")}
         |  </p>
         |
         |  <p class="govuk-body-s">
         |    ${messages("reasonNotRegistered.excepted.description")}
         |  </p>
         |
         |  <ul class="govuk-list govuk-list--bullet govuk-body-s">
         |    <li>${messages("reasonNotRegistered.excepted.bullet1")}</li>
         |    <li>${messages("reasonNotRegistered.excepted.bullet2")}</li>
         |    <li>${messages("reasonNotRegistered.excepted.bullet3")}</li>
         |    <li>${messages("reasonNotRegistered.excepted.bullet4")}</li>
         |    <li>${messages("reasonNotRegistered.excepted.bullet5")}</li>
         |  </ul>
         |</div>
         |""".stripMargin
    )

  private def exemptReasonContent(implicit messages: Messages): HtmlContent =
    HtmlContent(
      s"""
         |<div style="max-width: 520px;">
         |  <p class="govuk-!-margin-top-0 govuk-!-margin-bottom-2">
         |    ${messages("reasonNotRegistered.exempt.heading")}
         |  </p>
         |
         |  <p class="govuk-body-s">
         |    ${messages("reasonNotRegistered.exempt.description")}
         |  </p>
         |
         |  <ul class="govuk-list govuk-list--bullet govuk-body-s">
         |    <li>${messages("reasonNotRegistered.exempt.bullet1")}</li>
         |    <li>${messages("reasonNotRegistered.exempt.bullet2")}</li>
         |    <li>${messages("reasonNotRegistered.exempt.bullet3")}</li>
         |  </ul>
         |</div>
         |""".stripMargin
    )

  private def summaryRow(
    label: String,
    value: Content,
    href: String,
    hiddenText: String
  )(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = Key(content = Text(label)),
      value = Value(content = value),
      actions = Some(
        Actions(
          items = Seq(
            ActionItem(
              href = href,
              content = Text(messages("site.change")),
              visuallyHiddenText = Some(hiddenText)
            )
          )
        )
      )
    )

  private def missingDataRow(
    label: String,
    href: String,
    hiddenText: String
  )(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = Key(content = Text(label)),
      value = Value(
        content = HtmlContent(
          s"""<a class="govuk-link" href="$href">
             |${messages("site.enter")}
             |<span class="govuk-visually-hidden">$hiddenText</span>
             |</a>""".stripMargin
        )
      ),
      actions = None
    )
}

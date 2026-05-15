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

import models.*
import models.Mode.*
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

final case class AgentOrganisationDetailsSummary(
  charityDetails: SummaryList,
  agentDetails: SummaryList
)

object AgentOrganisationDetailsCheckYourAnswersHelper {

  def buildSummaryLists(
    answers: Option[AgentUserOrganisationDetailsAnswers],
    isCASCCharityRef: Boolean
  )(implicit messages: Messages): AgentOrganisationDetailsSummary = {

    val charityRows = answers
      .map { agentOrgUserAnswers =>
        Seq(
          if (isCASCCharityRef) { None }
          else {
            Some(
              agentOrgUserAnswers.nameOfCharityRegulator match {

                case Some(r) =>
                  summaryRow(
                    messages("agentOrganisationDetailsCheckYourAnswers.charityDetails.charityRegulatorName.label"),
                    Text(
                      messages(
                        s"agentOrganisationDetailsCheckYourAnswers.charityDetails.charityRegulatorName.$r.label"
                      )
                    ),
                    controllers.organisationDetails.routes.NameOfCharityRegulatorController
                      .onPageLoad(CheckMode)
                      .url,
                    messages("agentOrganisationDetailsCheckYourAnswers.charityDetails.charityRegulatorName.label")
                  )

                case None =>
                  missingDataRow(
                    messages("agentOrganisationDetailsCheckYourAnswers.charityDetails.charityRegulatorName.label"),
                    controllers.organisationDetails.routes.NameOfCharityRegulatorController
                      .onPageLoad(CheckMode)
                      .url,
                    messages("agentOrganisationDetailsCheckYourAnswers.charityDetails.charityRegulatorName.label")
                  )
              }
            )
          },
          if (isCASCCharityRef) { None }
          else {
            agentOrgUserAnswers.nameOfCharityRegulator match {

              case Some(
                    NameOfCharityRegulator.EnglandAndWales |
                    NameOfCharityRegulator.Scottish | NameOfCharityRegulator.NorthernIreland
                  ) =>
                Some(
                  agentOrgUserAnswers.charityRegistrationNumber match {

                    case Some(value) =>
                      summaryRow(
                        messages(
                          "agentOrganisationDetailsCheckYourAnswers.charityDetails.charityRegulatorNumber.label"
                        ),
                        Text(value),
                        controllers.organisationDetails.routes.CharityRegulatorNumberController
                          .onPageLoad(CheckMode)
                          .url,
                        messages(
                          "agentOrganisationDetailsCheckYourAnswers.charityDetails.charityRegulatorNumber.label"
                        )
                      )

                    case None =>
                      missingDataRow(
                        messages(
                          "agentOrganisationDetailsCheckYourAnswers.charityDetails.charityRegulatorNumber.label"
                        ),
                        controllers.organisationDetails.routes.CharityRegulatorNumberController
                          .onPageLoad(CheckMode)
                          .url,
                        messages(
                          "agentOrganisationDetailsCheckYourAnswers.charityDetails.charityRegulatorNumber.label"
                        )
                      )
                  }
                )

              case Some(NameOfCharityRegulator.None) =>
                Some(
                  agentOrgUserAnswers.reasonNotRegisteredWithRegulator match {

                    case Some(ReasonNotRegisteredWithRegulator.Excepted) =>
                      summaryRow(
                        messages(
                          "agentOrganisationDetailsCheckYourAnswers.charityDetails.reasonNotRegistered.label"
                        ),
                        exceptedReasonContent,
                        controllers.organisationDetails.routes.ReasonNotRegisteredWithRegulatorController
                          .onPageLoad(CheckMode)
                          .url,
                        messages(
                          "agentOrganisationDetailsCheckYourAnswers.charityDetails.reasonNotRegistered.label"
                        )
                      )

                    case Some(ReasonNotRegisteredWithRegulator.Exempt) =>
                      summaryRow(
                        messages(
                          "agentOrganisationDetailsCheckYourAnswers.charityDetails.reasonNotRegistered.label"
                        ),
                        exemptReasonContent,
                        controllers.organisationDetails.routes.ReasonNotRegisteredWithRegulatorController
                          .onPageLoad(CheckMode)
                          .url,
                        messages(
                          "agentOrganisationDetailsCheckYourAnswers.charityDetails.reasonNotRegistered.label"
                        )
                      )

                    case Some(reason) =>
                      summaryRow(
                        messages(
                          "agentOrganisationDetailsCheckYourAnswers.charityDetails.reasonNotRegistered.label"
                        ),
                        Text(
                          messages(
                            s"agentOrganisationDetailsCheckYourAnswers.charityDetails.reasonNotRegistered.$reason.label"
                          )
                        ),
                        controllers.organisationDetails.routes.ReasonNotRegisteredWithRegulatorController
                          .onPageLoad(CheckMode)
                          .url,
                        messages(
                          "agentOrganisationDetailsCheckYourAnswers.charityDetails.reasonNotRegistered.label"
                        )
                      )

                    case None =>
                      missingDataRow(
                        messages(
                          "agentOrganisationDetailsCheckYourAnswers.charityDetails.reasonNotRegistered.label"
                        ),
                        controllers.organisationDetails.routes.ReasonNotRegisteredWithRegulatorController
                          .onPageLoad(CheckMode)
                          .url,
                        messages(
                          "agentOrganisationDetailsCheckYourAnswers.charityDetails.reasonNotRegistered.label"
                        )
                      )
                  }
                )

              case None =>
                None
            }
          },
          Some(
            agentOrgUserAnswers.whoShouldHmrcSendPaymentTo match {

              case Some(value) =>
                val paymentRecipientMessageKey = value match {
                  case WhoShouldHmrcSendPaymentTo.CharityOrCASC =>
                    "whoShouldWeSendPaymentTo.radio.charityOrCasc"

                  case WhoShouldHmrcSendPaymentTo.AgentOrNominee =>
                    "whoShouldWeSendPaymentTo.radio.agentOrNominee"
                }
                summaryRow(
                  messages("agentOrganisationDetailsCheckYourAnswers.charityDetails.sendPaymentTo.label"),
                  Text(messages(paymentRecipientMessageKey)),
                  controllers.organisationDetails.routes.WhoShouldWeSendPaymentToController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("agentOrganisationDetailsCheckYourAnswers.charityDetails.sendPaymentTo.label")
                )

              case None =>
                missingDataRow(
                  messages("agentOrganisationDetailsCheckYourAnswers.charityDetails.sendPaymentTo.label"),
                  controllers.organisationDetails.routes.WhoShouldWeSendPaymentToController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("agentOrganisationDetailsCheckYourAnswers.charityDetails.sendPaymentTo.label")
                )
            }
          )
        ).flatten
      }
      .getOrElse(
        Seq(
          if (isCASCCharityRef) {
            None
          } else {
            Some(
              missingDataRow(
                messages("agentOrganisationDetailsCheckYourAnswers.charityDetails.charityRegulatorName.label"),
                controllers.organisationDetails.routes.NameOfCharityRegulatorController
                  .onPageLoad(CheckMode)
                  .url,
                messages("agentOrganisationDetailsCheckYourAnswers.charityDetails.charityRegulatorName.label")
              )
            )
          },
          Some(
            missingDataRow(
              messages("agentOrganisationDetailsCheckYourAnswers.charityDetails.sendPaymentTo.label"),
              controllers.organisationDetails.routes.WhoShouldWeSendPaymentToController
                .onPageLoad(CheckMode)
                .url,
              messages("agentOrganisationDetailsCheckYourAnswers.charityDetails.sendPaymentTo.label")
            )
          )
        ).flatten
      )
    val agentRows   = answers
      .map { agentOrgUserAnswers =>
        Seq(
          Some(
            agentOrgUserAnswers.daytimeTelephoneNumber match {

              case Some(value) =>
                summaryRow(
                  messages("agentOrganisationDetailsCheckYourAnswers.agentDetails.telephoneNumber.label"),
                  Text(value),
                  controllers.organisationDetails.routes.EnterTelephoneNumberController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("agentOrganisationDetailsCheckYourAnswers.agentDetails.telephoneNumber.label")
                )

              case None =>
                missingDataRow(
                  messages("agentOrganisationDetailsCheckYourAnswers.agentDetails.telephoneNumber.label"),
                  controllers.organisationDetails.routes.EnterTelephoneNumberController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("agentOrganisationDetailsCheckYourAnswers.agentDetails.telephoneNumber.label")
                )
            }
          ),
          Some(
            agentOrgUserAnswers.doYouHaveAgentUKAddress match {

              case Some(value) =>
                summaryRow(
                  messages("agentOrganisationDetailsCheckYourAnswers.agentDetails.ukAddress.label"),
                  Text(if (value) messages("site.yes") else messages("site.no")),
                  controllers.organisationDetails.routes.AgentHasUKAddressController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("agentOrganisationDetailsCheckYourAnswers.agentDetails.ukAddress.label")
                )

              case None =>
                missingDataRow(
                  messages("agentOrganisationDetailsCheckYourAnswers.agentDetails.ukAddress.label"),
                  controllers.organisationDetails.routes.AgentHasUKAddressController
                    .onPageLoad(CheckMode)
                    .url,
                  messages("agentOrganisationDetailsCheckYourAnswers.agentDetails.ukAddress.label")
                )
            }
          ),
          agentOrgUserAnswers.doYouHaveAgentUKAddress match {

            case Some(true) =>
              Some(
                agentOrgUserAnswers.postcode match {

                  case Some(value) =>
                    summaryRow(
                      messages("agentOrganisationDetailsCheckYourAnswers.agentDetails.postcode.label"),
                      Text(value),
                      controllers.organisationDetails.routes.AgentPostcodeController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("agentOrganisationDetailsCheckYourAnswers.agentDetails.postcode.label")
                    )

                  case None =>
                    missingDataRow(
                      messages("agentOrganisationDetailsCheckYourAnswers.agentDetails.postcode.label"),
                      controllers.organisationDetails.routes.AgentPostcodeController
                        .onPageLoad(CheckMode)
                        .url,
                      messages("agentOrganisationDetailsCheckYourAnswers.agentDetails.postcode.label")
                    )
                }
              )

            case _ =>
              None
          }
        ).flatten
      }
      .getOrElse(
        Seq(
          missingDataRow(
            messages("agentOrganisationDetailsCheckYourAnswers.agentDetails.telephoneNumber.label"),
            controllers.organisationDetails.routes.EnterTelephoneNumberController
              .onPageLoad(CheckMode)
              .url,
            messages("agentOrganisationDetailsCheckYourAnswers.agentDetails.telephoneNumber.label")
          ),
          missingDataRow(
            messages("agentOrganisationDetailsCheckYourAnswers.agentDetails.ukAddress.label"),
            controllers.organisationDetails.routes.AgentHasUKAddressController
              .onPageLoad(CheckMode)
              .url,
            messages("agentOrganisationDetailsCheckYourAnswers.agentDetails.ukAddress.label")
          )
        )
      )

    AgentOrganisationDetailsSummary(
      charityDetails = SummaryList(charityRows),
      agentDetails = SummaryList(agentRows)
    )
  }

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
      key = Key(Text(label)),
      value = Value(value),
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
      key = Key(Text(label)),
      value = Value(
        HtmlContent(
          s"""<a class="govuk-link" href="$href">
             |${messages("site.enter")}
             |<span class="govuk-visually-hidden">$hiddenText</span>
             |</a>""".stripMargin
        )
      ),
      actions = None
    )
}

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

package models

import play.api.libs.json.{Format, Json}

import scala.util.{Failure, Success, Try}

final case class AgentUserOrganisationDetailsAnswers(
  nameOfCharityRegulator: Option[NameOfCharityRegulator] = None,
  reasonNotRegisteredWithRegulator: Option[ReasonNotRegisteredWithRegulator] = None,
  charityRegistrationNumber: Option[String] = None,
  whoShouldHmrcSendPaymentTo: Option[WhoShouldHmrcSendPaymentTo] = None,
  daytimeTelephoneNumber: Option[String] = None,
  doYouHaveAgentUKAddress: Option[Boolean] = None,
  postcode: Option[String] = None
) {

  def missingFields(isCASCCharityRef: Boolean): List[String] =
    List(
      (nameOfCharityRegulator.isEmpty && !isCASCCharityRef)            -> "nameOfCharityRegulator.agent.missingDetails",
      (nameOfCharityRegulator.contains(NameOfCharityRegulator.None) &&
        reasonNotRegisteredWithRegulator.isEmpty && !isCASCCharityRef) -> "reasonNotRegisteredWithRegulator.agent.missingDetails",
      (nameOfCharityRegulator.exists(r =>
        r == NameOfCharityRegulator.EnglandAndWales ||
          r == NameOfCharityRegulator.Scottish ||
          r == NameOfCharityRegulator.NorthernIreland
      ) && charityRegistrationNumber.isEmpty && !isCASCCharityRef)     -> "charityRegulatorNumber.agent.missingDetails",
      whoShouldHmrcSendPaymentTo.isEmpty                               -> "whoShouldWeSendPaymentTo.missingDetails",
      daytimeTelephoneNumber.isEmpty                                   -> "enterTelephoneNumber.missingDetails",
      doYouHaveAgentUKAddress.isEmpty                                  -> "doYouHaveAgentUKAddress.missingDetails",
      (doYouHaveAgentUKAddress.contains(true) && postcode.isEmpty)     -> "agentPostcode.missingDetails"
    ).collect { case (true, key) => key }

  def hasAgentDetailsCompleteAnswers(isCASCCharityRef: Boolean): Boolean =
    missingFields(isCASCCharityRef).isEmpty
}

object AgentUserOrganisationDetailsAnswers {

  given Format[AgentUserOrganisationDetailsAnswers] =
    Json.format[AgentUserOrganisationDetailsAnswers]

  private def get[A](f: AgentUserOrganisationDetailsAnswers => Option[A])(using
    session: SessionData
  ): Option[A] =
    session.agentUserOrganisationDetailsAnswers.flatMap(f)

  private def set[A](value: A)(
    f: (AgentUserOrganisationDetailsAnswers, A) => AgentUserOrganisationDetailsAnswers
  )(using session: SessionData): SessionData = {

    val updated = session.agentUserOrganisationDetailsAnswers match {
      case Some(existing) => f(existing, value)
      case None           => f(AgentUserOrganisationDetailsAnswers(), value)
    }

    session.copy(agentUserOrganisationDetailsAnswers = Some(updated))
  }

  def from(agentUserOrganisationDetails: AgentUserOrganisationDetails): AgentUserOrganisationDetailsAnswers =
    AgentUserOrganisationDetailsAnswers(
      nameOfCharityRegulator = Some(agentUserOrganisationDetails.nameOfCharityRegulator),
      reasonNotRegisteredWithRegulator = agentUserOrganisationDetails.reasonNotRegisteredWithRegulator,
      charityRegistrationNumber = agentUserOrganisationDetails.charityRegistrationNumber,
      whoShouldHmrcSendPaymentTo = Some(agentUserOrganisationDetails.whoShouldHmrcSendPaymentTo),
      daytimeTelephoneNumber = Some(agentUserOrganisationDetails.daytimeTelephoneNumber),
      doYouHaveAgentUKAddress = Some(agentUserOrganisationDetails.doYouHaveAgentUKAddress),
      postcode = agentUserOrganisationDetails.postcode
    )

  def getMissingFields(
    answers: Option[AgentUserOrganisationDetailsAnswers],
    isCASCCharityRef: Boolean
  ): List[String] =
    answers match {
      case Some(a)                   => a.missingFields(isCASCCharityRef)
      case None if !isCASCCharityRef => defaultMissingFields
      case None                      => defaultMissingFieldsForCHCF
    }

  private val defaultMissingFields: List[String] = List(
    "nameOfCharityRegulator.agent.missingDetails",
    "whoShouldWeSendPaymentTo.missingDetails",
    "enterTelephoneNumber.missingDetails",
    "doYouHaveAgentUKAddress.missingDetails"
  )

  private val defaultMissingFieldsForCHCF: List[String] = List(
    "whoShouldWeSendPaymentTo.missingDetails",
    "enterTelephoneNumber.missingDetails",
    "doYouHaveAgentUKAddress.missingDetails"
  )

  def getNameOfCharityRegulator(using session: SessionData): Option[NameOfCharityRegulator] =
    get(_.nameOfCharityRegulator)

  def setNameOfCharityRegulator(value: NameOfCharityRegulator)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(nameOfCharityRegulator = Some(v)))

  def getReasonNotRegisteredWithRegulator(using session: SessionData): Option[ReasonNotRegisteredWithRegulator] =
    get(_.reasonNotRegisteredWithRegulator)

  def setReasonNotRegisteredWithRegulator(value: ReasonNotRegisteredWithRegulator)(using
    session: SessionData
  ): SessionData =
    set(value)((a, v) => a.copy(reasonNotRegisteredWithRegulator = Some(v)))

  def getWhoShouldHmrcSendPaymentTo(using session: SessionData): Option[WhoShouldHmrcSendPaymentTo] =
    get(_.whoShouldHmrcSendPaymentTo)

  def setWhoShouldHmrcSendPaymentTo(value: WhoShouldHmrcSendPaymentTo)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(whoShouldHmrcSendPaymentTo = Some(v)))

  def getCharityRegistrationNumber(using session: SessionData): Option[String] =
    get(_.charityRegistrationNumber)

  def setCharityRegistrationNumber(value: String)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(charityRegistrationNumber = Some(v)))

  def getDoYouHaveAgentUKAddress(using session: SessionData): Option[Boolean] =
    get(_.doYouHaveAgentUKAddress)

  def setDoYouHaveAgentUKAddress(value: Boolean)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(doYouHaveAgentUKAddress = Some(v)))

  def getDaytimeTelephoneNumber(using session: SessionData): Option[String] =
    get(_.daytimeTelephoneNumber)

  def setDaytimeTelephoneNumber(value: String)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(daytimeTelephoneNumber = Some(v)))

  def getPostcode(using session: SessionData): Option[String] =
    get(_.postcode)

  def setPostcode(value: String)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(postcode = Some(v)))

  def toAgentUserOrganisationDetails(
    answers: AgentUserOrganisationDetailsAnswers,
    isCASCCharity: Boolean
  ): Try[AgentUserOrganisationDetails] = {

    def requiredOpt[A](opt: Option[A], field: String): Try[A] =
      opt match {
        case Some(v) => Success(v)
        case None    => Failure(new RuntimeException(s"Missing $field"))
      }

    for {
      nameOfCharityRegulator <-
        if isCASCCharity then Success(NameOfCharityRegulator.None)
        else requiredOpt(answers.nameOfCharityRegulator, "nameOfCharityRegulator")

      whoShouldHmrcSendPaymentTo <-
        requiredOpt(answers.whoShouldHmrcSendPaymentTo, "whoShouldHmrcSendPaymentTo")

      doYouHaveAgentUKAddress <-
        requiredOpt(answers.doYouHaveAgentUKAddress, "doYouHaveAgentUKAddress")

      daytimeTelephoneNumber <-
        requiredOpt(answers.daytimeTelephoneNumber, "daytimeTelephoneNumber")

    } yield AgentUserOrganisationDetails(
      whoShouldHmrcSendPaymentTo = whoShouldHmrcSendPaymentTo,
      daytimeTelephoneNumber = daytimeTelephoneNumber,
      doYouHaveAgentUKAddress = doYouHaveAgentUKAddress,
      postcode = if doYouHaveAgentUKAddress then answers.postcode else None,
      nameOfCharityRegulator = nameOfCharityRegulator,
      reasonNotRegisteredWithRegulator =
        if nameOfCharityRegulator == NameOfCharityRegulator.None then answers.reasonNotRegisteredWithRegulator
        else None,
      charityRegistrationNumber =
        if nameOfCharityRegulator != NameOfCharityRegulator.None then answers.charityRegistrationNumber
        else None
    )
  }
}

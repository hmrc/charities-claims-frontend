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

package models

import play.api.libs.json.Format
import play.api.libs.json.Json
import scala.util.Try
import utils.Required.required

final case class OrganisationDetailsAnswers(
  nameOfCharityRegulator: Option[NameOfCharityRegulator] = None,
  reasonNotRegisteredWithRegulator: Option[ReasonNotRegisteredWithRegulator] = None,
  charityRegistrationNumber: Option[String] = None,
  areYouACorporateTrustee: Option[Boolean] = None,
  doYouHaveCorporateTrusteeUKAddress: Option[Boolean] = None,
  doYouHaveAuthorisedOfficialTrusteeUKAddress: Option[Boolean] = None,
  nameOfCorporateTrustee: Option[String] = None,
  corporateTrusteePostcode: Option[String] = None,
  corporateTrusteeDaytimeTelephoneNumber: Option[String] = None,
  authorisedOfficialTrusteePostcode: Option[String] = None,
  authorisedOfficialTrusteeDaytimeTelephoneNumber: Option[String] = None,
  authorisedOfficialTrusteeTitle: Option[String] = None,
  authorisedOfficialTrusteeFirstName: Option[String] = None,
  authorisedOfficialTrusteeLastName: Option[String] = None,
  corporateTrusteeDetails: Option[CorporateTrusteeDetails] = None,
  authorisedOfficialDetails: Option[AuthorisedOfficialDetails] = None
) {
  def hasOrganisationDetailsCompleteAnswers: Boolean =
    nameOfCharityRegulator.isDefined && reasonNotRegisteredWithRegulator.isDefined
      && charityRegistrationNumber.isDefined
      && (
        areYouACorporateTrustee,
        doYouHaveCorporateTrusteeUKAddress,
        doYouHaveAuthorisedOfficialTrusteeUKAddress
      ).match {
        case (Some(true), Some(false), _)  =>
          nameOfCorporateTrustee.isDefined && corporateTrusteeDaytimeTelephoneNumber.isDefined && corporateTrusteePostcode.isEmpty
        case (Some(true), Some(true), _)   =>
          nameOfCorporateTrustee.isDefined && corporateTrusteeDaytimeTelephoneNumber.isDefined && corporateTrusteePostcode.isDefined
        case (Some(false), _, Some(false)) =>
          authorisedOfficialTrusteeFirstName.isDefined && authorisedOfficialTrusteeLastName.isDefined && authorisedOfficialTrusteeDaytimeTelephoneNumber.isDefined && authorisedOfficialTrusteePostcode.isEmpty
        case (Some(false), _, Some(true))  =>
          authorisedOfficialTrusteeFirstName.isDefined && authorisedOfficialTrusteeLastName.isDefined && authorisedOfficialTrusteeDaytimeTelephoneNumber.isDefined && authorisedOfficialTrusteePostcode.isDefined
        case (_, _, _)                     => false
      }
}

object OrganisationDetailsAnswers {

  given Format[OrganisationDetailsAnswers] = Json.format[OrganisationDetailsAnswers]

  private def get[A](f: OrganisationDetailsAnswers => Option[A])(using session: SessionData): Option[A] =
    session.organisationDetailsAnswers.flatMap(f)

  private def set[A](value: A)(f: (OrganisationDetailsAnswers, A) => OrganisationDetailsAnswers)(using
    session: SessionData
  ): SessionData =
    val updated = session.organisationDetailsAnswers match
      case Some(existing) => f(existing, value)
      case None           => f(OrganisationDetailsAnswers(), value)
    session.copy(organisationDetailsAnswers = Some(updated))

  def from(organisationDetails: OrganisationDetails): OrganisationDetailsAnswers =
    OrganisationDetailsAnswers(
      nameOfCharityRegulator = Some(organisationDetails.nameOfCharityRegulator),
      reasonNotRegisteredWithRegulator = organisationDetails.reasonNotRegisteredWithRegulator,
      charityRegistrationNumber = organisationDetails.charityRegistrationNumber,
      areYouACorporateTrustee = Some(organisationDetails.areYouACorporateTrustee),
      doYouHaveCorporateTrusteeUKAddress = Some(organisationDetails.doYouHaveCorporateTrusteeUKAddress),
      doYouHaveAuthorisedOfficialTrusteeUKAddress =
        Some(organisationDetails.doYouHaveAuthorisedOfficialTrusteeUKAddress),
      nameOfCorporateTrustee = organisationDetails.nameOfCorporateTrustee,
      corporateTrusteePostcode = organisationDetails.corporateTrusteePostcode,
      corporateTrusteeDaytimeTelephoneNumber = organisationDetails.corporateTrusteeDaytimeTelephoneNumber,
      authorisedOfficialTrusteePostcode = organisationDetails.authorisedOfficialTrusteePostcode,
      authorisedOfficialTrusteeDaytimeTelephoneNumber =
        organisationDetails.authorisedOfficialTrusteeDaytimeTelephoneNumber,
      authorisedOfficialTrusteeTitle = organisationDetails.authorisedOfficialTrusteeTitle,
      authorisedOfficialTrusteeFirstName = organisationDetails.authorisedOfficialTrusteeFirstName,
      authorisedOfficialTrusteeLastName = organisationDetails.authorisedOfficialTrusteeLastName
    )

  def getNameOfCharityRegulator(using session: SessionData): Option[NameOfCharityRegulator] = get(
    _.nameOfCharityRegulator
  )

  def setNameOfCharityRegulator(value: NameOfCharityRegulator)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(nameOfCharityRegulator = Some(v)))

  def getReasonNotRegisteredWithRegulator(using session: SessionData): Option[ReasonNotRegisteredWithRegulator] = get(
    _.reasonNotRegisteredWithRegulator
  )

  def setReasonNotRegisteredWithRegulator(value: ReasonNotRegisteredWithRegulator)(using
    session: SessionData
  ): SessionData =
    set(value)((a, v) => a.copy(reasonNotRegisteredWithRegulator = Some(v)))

  def getDoYouHaveCorporateTrusteeUKAddress(using session: SessionData): Option[Boolean] = get(
    _.doYouHaveCorporateTrusteeUKAddress
  )

  def setDoYouHaveCorporateTrusteeUKAddress(value: Boolean)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(doYouHaveCorporateTrusteeUKAddress = Some(v)))

  def getDoYouHaveAuthorisedOfficialTrusteeUKAddress(using session: SessionData): Option[Boolean] = get(
    _.doYouHaveAuthorisedOfficialTrusteeUKAddress
  )

  def setDoYouHaveAuthorisedOfficialTrusteeUKAddress(value: Boolean)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(doYouHaveAuthorisedOfficialTrusteeUKAddress = Some(v)))

  def getAreYouACorporateTrustee(using session: SessionData): Option[Boolean] = get(_.areYouACorporateTrustee)

  def setAreYouACorporateTrustee(value: Boolean)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(areYouACorporateTrustee = Some(v)))

  def getCharityRegistrationNumber(using session: SessionData): Option[String] = get(_.charityRegistrationNumber)

  def setCharityRegistrationNumber(value: String)(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(charityRegistrationNumber = Some(v)))

  def getAuthorisedOfficialDetails(using session: SessionData): Option[AuthorisedOfficialDetails] = get(answers =>
    for
      title       <- answers.authorisedOfficialTrusteeTitle
      firstName   <- answers.authorisedOfficialTrusteeFirstName
      lastName    <- answers.authorisedOfficialTrusteeLastName
      phoneNumber <- answers.authorisedOfficialTrusteeDaytimeTelephoneNumber
      postcode     = answers.authorisedOfficialTrusteePostcode
    yield AuthorisedOfficialDetails(Some(title), firstName, lastName, phoneNumber, postcode)
  )

  def setAuthorisedOfficialDetails(value: AuthorisedOfficialDetails)(using session: SessionData): SessionData =
    set(value)((a, v) =>
      a.copy(
        authorisedOfficialTrusteeTitle = v.title,
        authorisedOfficialTrusteeFirstName = Some(v.firstName),
        authorisedOfficialTrusteeLastName = Some(v.lastName),
        authorisedOfficialTrusteeDaytimeTelephoneNumber = Some(v.phoneNumber),
        authorisedOfficialTrusteePostcode = v.postcode
      )
    )

  def getCorporateTrusteeDetails(using session: SessionData): Option[CorporateTrusteeDetails] = get(answers =>
    for
      nameOfCorporateTrustee <- answers.nameOfCorporateTrustee
      phoneNumber            <- answers.corporateTrusteeDaytimeTelephoneNumber
      postCode                = answers.corporateTrusteePostcode
    yield CorporateTrusteeDetails(nameOfCorporateTrustee, phoneNumber, postCode)
  )

  def setCorporateTrusteeDetails(value: CorporateTrusteeDetails)(using session: SessionData): SessionData =
    set(value)((a, v) =>
      a.copy(
        nameOfCorporateTrustee = Some(v.nameOfCorporateTrustee),
        corporateTrusteeDaytimeTelephoneNumber = Some(v.corporateTrusteeDaytimeTelephoneNumber),
        corporateTrusteePostcode = v.corporateTrusteePostcode
      )
    )

  def toOrganisationDetails(answers: OrganisationDetailsAnswers): Try[OrganisationDetails] =
    for {
      nameOfCharityRegulator                      <- required(answers)(_.nameOfCharityRegulator)
      areYouACorporateTrustee                     <- required(answers)(_.areYouACorporateTrustee)
      doYouHaveAuthorisedOfficialTrusteeUKAddress <- required(answers)(_.doYouHaveAuthorisedOfficialTrusteeUKAddress)
      doYouHaveCorporateTrusteeUKAddress          <- required(answers)(_.doYouHaveCorporateTrusteeUKAddress)
    } yield OrganisationDetails(
      nameOfCharityRegulator = nameOfCharityRegulator,
      reasonNotRegisteredWithRegulator = answers.reasonNotRegisteredWithRegulator,
      charityRegistrationNumber = answers.charityRegistrationNumber,
      areYouACorporateTrustee = areYouACorporateTrustee,
      doYouHaveCorporateTrusteeUKAddress = doYouHaveCorporateTrusteeUKAddress,
      doYouHaveAuthorisedOfficialTrusteeUKAddress = doYouHaveAuthorisedOfficialTrusteeUKAddress,
      nameOfCorporateTrustee = answers.nameOfCorporateTrustee,
      corporateTrusteePostcode = answers.corporateTrusteePostcode,
      corporateTrusteeDaytimeTelephoneNumber = answers.corporateTrusteeDaytimeTelephoneNumber,
      authorisedOfficialTrusteePostcode = answers.authorisedOfficialTrusteePostcode,
      authorisedOfficialTrusteeDaytimeTelephoneNumber = answers.authorisedOfficialTrusteeDaytimeTelephoneNumber,
      authorisedOfficialTrusteeTitle = answers.authorisedOfficialTrusteeTitle,
      authorisedOfficialTrusteeFirstName = answers.authorisedOfficialTrusteeFirstName,
      authorisedOfficialTrusteeLastName = answers.authorisedOfficialTrusteeLastName
    )

}

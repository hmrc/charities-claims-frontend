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

package controllers.organisationDetails

import models.OrganisationDetailsAnswers
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import controllers.ControllerSpec
import models.*
import play.api.inject.bind
import services.ClaimsService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.OrganisationDetailsCheckYourAnswersView

import _root_.scala.concurrent.Future

class OrganisationDetailsCheckYourAnswersControllerSpec extends ControllerSpec {
  "OrganisationDetailsCheckYourAnswersController" - {
    "onPageLoad" - {
      "should render ClaimCompleteController if submissionReference is defined" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          lastUpdatedReference = Some(testCharitiesReference),
          submissionReference = Some(testCharitiesReference)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
      "should render the page correctly when organisation details not defined" in {

        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OrganisationDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.organisationDetailsAnswers).body
        }
      }

      "should render the page correctly when organisation details with  name of charity=EnglandAndWales, Reg Num, both Corporate Trustee & UK Address true" in {
        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.EnglandAndWales),
          charityRegistrationNumber = Some("123"),
          areYouACorporateTrustee = Some(true),
          doYouHaveCorporateTrusteeUKAddress = Some(true),
          nameOfCorporateTrustee = Some("Name of Corporate Trustee"),
          corporateTrusteePostcode = Some("none"),
          corporateTrusteeDaytimeTelephoneNumber = Some("12345678AB"),
          corporateTrusteeDetails =
            Some(CorporateTrusteeDetails("Name of Corporate Trustee", "12345678AB", Some("none")))
        )

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(organisationDetailsAnswers = Some(organisationDetailsAnswers))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OrganisationDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.organisationDetailsAnswers).body
        }
      }
      "should render the page correctly when organisation details with  name of charity not defined, Reg Num, both Corporate Trustee & UK Address true" in {

        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          charityRegistrationNumber = Some("123"),
          areYouACorporateTrustee = Some(true),
          doYouHaveCorporateTrusteeUKAddress = Some(true),
          nameOfCorporateTrustee = Some("Name of Corporate Trustee"),
          corporateTrusteePostcode = Some("none"),
          corporateTrusteeDaytimeTelephoneNumber = Some("12345678AB"),
          corporateTrusteeDetails =
            Some(CorporateTrusteeDetails("Name of Corporate Trustee", "12345678AB", Some("none")))
        )

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(organisationDetailsAnswers = Some(organisationDetailsAnswers))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OrganisationDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.organisationDetailsAnswers).body
        }
      }
      "should render the page correctly when organisation details with  name of charity=NorthernIreland, Reg Num not defined, both Corporate Trustee is true and not UK Address" in {

        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.NorthernIreland),
          areYouACorporateTrustee = Some(true),
          doYouHaveCorporateTrusteeUKAddress = Some(false),
          nameOfCorporateTrustee = Some("Name of Corporate Trustee"),
          corporateTrusteeDaytimeTelephoneNumber = Some("12345678AB"),
          corporateTrusteeDetails =
            Some(CorporateTrusteeDetails("Name of Corporate Trustee", "12345678AB", Some("none")))
        )
        val sessionData                =
          completeRepaymentDetailsAnswersSession.copy(organisationDetailsAnswers = Some(organisationDetailsAnswers))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OrganisationDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.organisationDetailsAnswers).body
        }
      }

      "should render the page correctly when organisation details with  name of charity =None, reason for not registered = Waiting, both Corporate Trustee is true and not UK Address" in {

        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.None),
          reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.Waiting),
          areYouACorporateTrustee = Some(true),
          doYouHaveCorporateTrusteeUKAddress = Some(false),
          nameOfCorporateTrustee = Some("Name of Corporate Trustee"),
          corporateTrusteeDaytimeTelephoneNumber = Some("12345678AB"),
          corporateTrusteeDetails =
            Some(CorporateTrusteeDetails("Name of Corporate Trustee", "12345678AB", Some("none")))
        )

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(organisationDetailsAnswers = Some(organisationDetailsAnswers))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OrganisationDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.organisationDetailsAnswers).body
        }
      }
      "should render the page correctly when organisation details with  name of charity =None, reason for not registered = Exempt, both Corporate Trustee is true and not UK Address" in {

        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.None),
          reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.Exempt),
          areYouACorporateTrustee = Some(true),
          doYouHaveCorporateTrusteeUKAddress = Some(false),
          nameOfCorporateTrustee = Some("Name of Corporate Trustee"),
          corporateTrusteeDaytimeTelephoneNumber = Some("12345678AB"),
          corporateTrusteeDetails =
            Some(CorporateTrusteeDetails("Name of Corporate Trustee", "12345678AB", Some("none")))
        )

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(organisationDetailsAnswers = Some(organisationDetailsAnswers))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OrganisationDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.organisationDetailsAnswers).body
        }
      }
      "should render the page correctly when organisation details with  name of charity =None, reason for not registered = Excepted, both Corporate Trustee is true and not UK Address" in {

        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.None),
          reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.Excepted),
          areYouACorporateTrustee = Some(true),
          doYouHaveCorporateTrusteeUKAddress = Some(false),
          nameOfCorporateTrustee = Some("Name of Corporate Trustee"),
          corporateTrusteeDaytimeTelephoneNumber = Some("12345678AB"),
          corporateTrusteeDetails =
            Some(CorporateTrusteeDetails("Name of Corporate Trustee", "12345678AB", Some("none")))
        )

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(organisationDetailsAnswers = Some(organisationDetailsAnswers))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OrganisationDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.organisationDetailsAnswers).body
        }
      }
      "should render the page correctly when organisation details with  name of charity =None, reason for not registered = LowIncome, both Corporate Trustee is true and not UK Address" in {

        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.None),
          reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.LowIncome),
          areYouACorporateTrustee = Some(true),
          doYouHaveCorporateTrusteeUKAddress = Some(false),
          nameOfCorporateTrustee = Some("Name of Corporate Trustee"),
          corporateTrusteeDaytimeTelephoneNumber = Some("12345678AB"),
          corporateTrusteeDetails =
            Some(CorporateTrusteeDetails("Name of Corporate Trustee", "12345678AB", Some("none")))
        )

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(organisationDetailsAnswers = Some(organisationDetailsAnswers))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OrganisationDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.organisationDetailsAnswers).body
        }
      }
      "should render the page correctly when organisation details with  name of charity =None, reason for not registered = LowIncome, both Corporate Trustee is true and UK Address not defined" in {

        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.None),
          reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.LowIncome),
          areYouACorporateTrustee = Some(true),
          nameOfCorporateTrustee = Some("Name of Corporate Trustee"),
          corporateTrusteeDaytimeTelephoneNumber = Some("12345678AB"),
          corporateTrusteeDetails =
            Some(CorporateTrusteeDetails("Name of Corporate Trustee", "12345678AB", Some("none")))
        )

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(organisationDetailsAnswers = Some(organisationDetailsAnswers))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OrganisationDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.organisationDetailsAnswers).body
        }
      }
      "should render the page correctly when organisation details with  name of charity =None, reason for not registered = LowIncome, both Corporate Trustee is not defined and UK Address not defined" in {

        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.None),
          reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.LowIncome),
          nameOfCorporateTrustee = Some("Name of Corporate Trustee"),
          corporateTrusteeDaytimeTelephoneNumber = Some("12345678AB"),
          corporateTrusteeDetails =
            Some(CorporateTrusteeDetails("Name of Corporate Trustee", "12345678AB", Some("none")))
        )

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(organisationDetailsAnswers = Some(organisationDetailsAnswers))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OrganisationDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.organisationDetailsAnswers).body
        }
      }

      "should render the page correctly when organisation details with  name of charity =None, reason for not registered = not defined, both Corporate Trustee is true and not UK Address" in {

        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.None),
          areYouACorporateTrustee = Some(true),
          doYouHaveCorporateTrusteeUKAddress = Some(false),
          nameOfCorporateTrustee = Some("Name of Corporate Trustee"),
          corporateTrusteeDaytimeTelephoneNumber = Some("12345678AB"),
          corporateTrusteeDetails =
            Some(CorporateTrusteeDetails("Name of Corporate Trustee", "12345678AB", Some("none")))
        )

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(organisationDetailsAnswers = Some(organisationDetailsAnswers))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OrganisationDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.organisationDetailsAnswers).body
        }
      }
      "should render the page correctly when organisation details with  name of charity=Scottish, Reg Num, Corporate Trustee = true and UK address but details not defined" in {

        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.Scottish),
          reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.Waiting),
          charityRegistrationNumber = Some("123"),
          areYouACorporateTrustee = Some(true),
          doYouHaveCorporateTrusteeUKAddress = Some(true)
        )

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(organisationDetailsAnswers = Some(organisationDetailsAnswers))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OrganisationDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.organisationDetailsAnswers).body
        }
      }
      "should render the page correctly when organisation details with  name of charity=Scottish, Reg Num, Corporate Trustee = true and not UK address but details not defined" in {

        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.Scottish),
          reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.Waiting),
          charityRegistrationNumber = Some("123"),
          areYouACorporateTrustee = Some(true),
          doYouHaveCorporateTrusteeUKAddress = Some(false)
        )

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(organisationDetailsAnswers = Some(organisationDetailsAnswers))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OrganisationDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.organisationDetailsAnswers).body
        }
      }
      "should render the page correctly when organisation details with  name of charity=Scottish, Reg Num, Corporate Trustee = false and not UK address" in {

        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.Scottish),
          reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.Waiting),
          charityRegistrationNumber = Some("123"),
          areYouACorporateTrustee = Some(false),
          doYouHaveAuthorisedOfficialTrusteeUKAddress = Some(false),
          authorisedOfficialTrusteePostcode = Some("none"),
          authorisedOfficialTrusteeDaytimeTelephoneNumber = Some("12345678AB"),
          authorisedOfficialTrusteeTitle = Some("MR"),
          authorisedOfficialTrusteeFirstName = Some("Jack"),
          authorisedOfficialTrusteeLastName = Some("Smith"),
          authorisedOfficialDetails =
            Some(AuthorisedOfficialDetails(Some("MR"), "Jack", "Smith", "12345678AB", Some("none")))
        )

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(organisationDetailsAnswers = Some(organisationDetailsAnswers))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OrganisationDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.organisationDetailsAnswers).body
        }
      }
      "should render the page correctly when organisation details with  name of charity=Scottish, Reg Num, Corporate Trustee = false and UK address" in {

        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.Scottish),
          reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.Waiting),
          charityRegistrationNumber = Some("123"),
          areYouACorporateTrustee = Some(false),
          doYouHaveAuthorisedOfficialTrusteeUKAddress = Some(true),
          authorisedOfficialTrusteePostcode = Some("none"),
          authorisedOfficialTrusteeDaytimeTelephoneNumber = Some("12345678AB"),
          authorisedOfficialTrusteeTitle = Some("MR"),
          authorisedOfficialTrusteeFirstName = Some("Jack"),
          authorisedOfficialTrusteeLastName = Some("Smith"),
          authorisedOfficialDetails =
            Some(AuthorisedOfficialDetails(Some("MR"), "Jack", "Smith", "12345678AB", Some("none")))
        )

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(organisationDetailsAnswers = Some(organisationDetailsAnswers))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OrganisationDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.organisationDetailsAnswers).body
        }
      }
      "should render the page correctly when organisation details with  name of charity=Scottish, Reg Num, Corporate Trustee = false and UK address but details not defined" in {

        val organisationDetailsAnswers =
          OrganisationDetailsAnswers(
            nameOfCharityRegulator = Some(NameOfCharityRegulator.Scottish),
            reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.Waiting),
            charityRegistrationNumber = Some("123"),
            areYouACorporateTrustee = Some(false),
            doYouHaveAuthorisedOfficialTrusteeUKAddress = Some(true),
            authorisedOfficialDetails =
              Some(AuthorisedOfficialDetails(Some("MR"), "Jack", "Smith", "12345678AB", Some("none")))
          )

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(organisationDetailsAnswers = Some(organisationDetailsAnswers))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OrganisationDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.organisationDetailsAnswers).body
        }
      }
      "should render the page correctly when organisation details with  name of charity=Scottish, Reg Num, Corporate Trustee = false and not UK address but details not defined" in {

        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.Scottish),
          reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.Waiting),
          charityRegistrationNumber = Some("123"),
          areYouACorporateTrustee = Some(false),
          doYouHaveAuthorisedOfficialTrusteeUKAddress = Some(false),
          authorisedOfficialDetails =
            Some(AuthorisedOfficialDetails(Some("MR"), "Jack", "Smith", "12345678AB", Some("none")))
        )

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(organisationDetailsAnswers = Some(organisationDetailsAnswers))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OrganisationDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.organisationDetailsAnswers).body
        }
      }
      "should render the page correctly when organisation details with  name of charity=Scottish, Reg Num, Corporate Trustee = false and UK address not defined" in {

        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.Scottish),
          reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.Waiting),
          charityRegistrationNumber = Some("123"),
          areYouACorporateTrustee = Some(false),
          authorisedOfficialTrusteePostcode = Some("none"),
          authorisedOfficialTrusteeDaytimeTelephoneNumber = Some("12345678AB"),
          authorisedOfficialTrusteeTitle = Some("MR"),
          authorisedOfficialTrusteeFirstName = Some("Jack"),
          authorisedOfficialTrusteeLastName = Some("Smith"),
          authorisedOfficialDetails =
            Some(AuthorisedOfficialDetails(Some("MR"), "Jack", "Smith", "12345678AB", Some("none")))
        )

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(organisationDetailsAnswers = Some(organisationDetailsAnswers))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OrganisationDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.organisationDetailsAnswers).body
        }
      }
    }

    "onSubmit" - {
      "should render ClaimCompleteController if submissionReference is defined" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          lastUpdatedReference = Some(testCharitiesReference),
          submissionReference = Some(testCharitiesReference)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.OrganisationDetailsCheckYourAnswersController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
      "should save the organisation details answers and redirect to ClaimsTaskListController when isRepaymentClaimDetailsComplete = false" in {
        val sessionData =
          SessionData(
            charitiesReference = testCharitiesReference,
            unsubmittedClaimId = Some("123"),
            lastUpdatedReference = Some("123"),
            organisationDetailsAnswers = Some(
              OrganisationDetailsAnswers(
                nameOfCharityRegulator = Some(NameOfCharityRegulator.EnglandAndWales),
                reasonNotRegisteredWithRegulator = None,
                charityRegistrationNumber = Some("1234"),
                areYouACorporateTrustee = Some(false),
                doYouHaveCorporateTrusteeUKAddress = Some(true),
                nameOfCorporateTrustee = Some("Name of Corporate Trustee"),
                corporateTrusteePostcode = Some("SW1 5TY"),
                corporateTrusteeDaytimeTelephoneNumber = Some("12345678AB"),
                doYouHaveAuthorisedOfficialTrusteeUKAddress = Some(true),
                authorisedOfficialTrusteePostcode = Some("SW1 5TY"),
                authorisedOfficialTrusteeDaytimeTelephoneNumber = Some("12345678AB"),
                authorisedOfficialTrusteeTitle = Some("MR"),
                authorisedOfficialTrusteeFirstName = Some("Jack"),
                authorisedOfficialTrusteeLastName = Some("Smith"),
                authorisedOfficialDetails =
                  Some(AuthorisedOfficialDetails(Some("MR"), "Jack", "Smith", "12345678AB", Some("SW1 5TY")))
              )
            )
          )

        given application: Application =
          applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.OrganisationDetailsCheckYourAnswersController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should save the organisation details answers and redirect to ClaimsTaskListController when isRepaymentClaimDetailsComplete is true" in {
        val organisationDetailsAnswers = OrganisationDetailsAnswers(
          nameOfCharityRegulator = Some(NameOfCharityRegulator.EnglandAndWales),
          reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.Waiting),
          charityRegistrationNumber = Some("123"),
          areYouACorporateTrustee = Some(true),
          doYouHaveCorporateTrusteeUKAddress = Some(true),
          nameOfCorporateTrustee = Some("Name of Corporate Trustee"),
          corporateTrusteePostcode = Some("SW1 5TY"),
          corporateTrusteeDaytimeTelephoneNumber = Some("12345678AB"),
          doYouHaveAuthorisedOfficialTrusteeUKAddress = Some(true),
          authorisedOfficialTrusteePostcode = Some("SW1 5TY"),
          authorisedOfficialTrusteeDaytimeTelephoneNumber = Some("12345678AB"),
          authorisedOfficialTrusteeTitle = Some("MR"),
          authorisedOfficialTrusteeFirstName = Some("Jack"),
          authorisedOfficialTrusteeLastName = Some("Smith"),
          authorisedOfficialDetails =
            Some(AuthorisedOfficialDetails(Some("MR"), "Jack", "Smith", "12345678AB", Some("SW1 5TY")))
        )
        val sessionData                =
          completeRepaymentDetailsAnswersSession.copy(organisationDetailsAnswers = Some(organisationDetailsAnswers))

        val mockClaimsService: ClaimsService = mock[ClaimsService]
        (mockClaimsService
          .save(using _: HeaderCarrier))
          .expects(*)
          .returning(Future.successful(()))

        given application: Application =
          applicationBuilder(sessionData = sessionData)
            .overrides(bind[ClaimsService].toInstance(mockClaimsService))
            .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.OrganisationDetailsCheckYourAnswersController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )

        }
      }

      "should redirect to the incomplete answers page if the answers are not complete" in {

        val sessionData = completeRepaymentDetailsAnswersSession

        given application: Application =
          applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.OrganisationDetailsCheckYourAnswersController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result) shouldEqual Some(
            routes.OrganisationDetailsIncompleteAnswersController.onPageLoad.url
          )

        }
      }
    }
  }
}

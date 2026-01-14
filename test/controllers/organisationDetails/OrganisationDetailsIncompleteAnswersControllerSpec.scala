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

import play.api.test.FakeRequest
import play.api.mvc.AnyContentAsEmpty
import controllers.ControllerSpec
import views.html.OrganisationDetailsIncompleteAnswersView
import models.{
  AuthorisedOfficialDetails,
  NameOfCharityRegulator,
  OrganisationDetailsAnswers,
  ReasonNotRegisteredWithRegulator,
  RepaymentClaimDetailsAnswers,
  SessionData
}
import play.api.Application

class OrganisationDetailsIncompleteAnswersControllerSpec extends ControllerSpec {

  "IncompleteAnswersController" - {
    "onPageLoad" - {
      "should render the page with No missing fields when answers are missing" in {

        given application: Application = applicationBuilder().build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsIncompleteAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OrganisationDetailsIncompleteAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(
            routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url,
            Nil
          ).body
        }
      }

      "should render the page with missing fields when answers are incomplete" in {

        val repaymentClaimDetailsDefaultAnswers = RepaymentClaimDetailsAnswers(
          claimingGiftAid = Some(true),
          claimingTaxDeducted = Some(false),
          claimingUnderGiftAidSmallDonationsScheme = Some(true),
          claimingReferenceNumber = Some(true),
          claimReferenceNumber = Some("12345678AB")
        )

        val sessionData = SessionData(
          unsubmittedClaimId = Some("123"),
          lastUpdatedReference = Some("123"),
          repaymentClaimDetailsAnswers = repaymentClaimDetailsDefaultAnswers,
          organisationDetailsAnswers = Some(OrganisationDetailsAnswers())
        )

        given application: Application = applicationBuilder(sessionData).build()

        val missingFields = OrganisationDetailsAnswers().missingFields

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsIncompleteAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OrganisationDetailsIncompleteAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(
            routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url,
            missingFields
          ).body
        }
      }

      "should render the page with no missing fields when answers are complete" in {
        val repaymentClaimDetailsDefaultAnswers = RepaymentClaimDetailsAnswers(
          claimingGiftAid = Some(true),
          claimingTaxDeducted = Some(false),
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingReferenceNumber = Some(false)
        )
        val sessionData                         = SessionData(
          unsubmittedClaimId = Some("123"),
          lastUpdatedReference = Some("123"),
          repaymentClaimDetailsAnswers = repaymentClaimDetailsDefaultAnswers,
          organisationDetailsAnswers = Some(
            OrganisationDetailsAnswers(
              nameOfCharityRegulator = Some(NameOfCharityRegulator.None),
              reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.Waiting),
              charityRegistrationNumber = None,
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

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.OrganisationDetailsIncompleteAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[OrganisationDetailsIncompleteAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(
            routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url,
            Seq.empty
          ).body
        }
      }
    }
  }

}

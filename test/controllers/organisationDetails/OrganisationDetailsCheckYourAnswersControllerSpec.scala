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
import models.ReasonNotRegisteredWithRegulator.*
import models.NameOfCharityRegulator.*
import models.*
import views.html.OrganisationDetailsCheckYourAnswersView

class OrganisationDetailsCheckYourAnswersControllerSpec extends ControllerSpec {
  "OrganisationDetailsCheckYourAnswersController" - {
    "onPageLoad" - {
      "should render the page correctly when organisation details is true" in {

        val sessionData = SessionData(
          unsubmittedClaimId = Some("123"),
          lastUpdatedReference = Some("123"),
          repaymentClaimDetailsAnswers = RepaymentClaimDetailsAnswers(
            claimingGiftAid = Some(true),
            claimingTaxDeducted = Some(false),
            claimingUnderGiftAidSmallDonationsScheme = Some(true),
            claimingReferenceNumber = Some(true),
            claimReferenceNumber = Some("12345678AB")
          ),
          organisationDetailsAnswers = Some(
            OrganisationDetailsAnswers(
              nameOfCharityRegulator = Some(NameOfCharityRegulator.EnglandAndWales),
              reasonNotRegisteredWithRegulator = Some(ReasonNotRegisteredWithRegulator.LowIncome),
              charityRegistrationNumber = Some("123"),
              areYouACorporateTrustee = Some(true),
              doYouHaveCorporateTrusteeUKAddress = Some(true),
              doYouHaveAuthorisedOfficialTrusteeUKAddress = Some(false),
              nameOfCorporateTrustee = Some("Name of Corporate Trustee"),
              corporateTrusteePostcode = Some("none"),
              corporateTrusteeDaytimeTelephoneNumber = Some("12345678AB"),
              authorisedOfficialTrusteePostcode = Some("none"),
              authorisedOfficialTrusteeDaytimeTelephoneNumber = Some("12345678AB"),
              authorisedOfficialTrusteeTitle = Some("MR"),
              authorisedOfficialTrusteeFirstName = Some("Jack"),
              authorisedOfficialTrusteeLastName = Some("Smith"),
              corporateTrusteeDetails =
                Some(CorporateTrusteeDetails("Name of Corporate Trustee", "12345678AB", Some("none"))),
              authorisedOfficialDetails =
                Some(AuthorisedOfficialDetails(Some("MR"), "Jack", "Smith", "12345678AB", Some("none")))
            )
          )
//          ,
//          giftAidScheduleDataAnswers = Some(GiftAidScheduleDataAnswers()),
//          declarationDetailsAnswers = Some(DeclarationDetailsAnswers()),
//          otherIncomeScheduleDataAnswers = Some(OtherIncomeScheduleDataAnswers()),
//          giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(GiftAidSmallDonationsSchemeDonationDetailsAnswers())
        )

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
  }

  "onSubmit" - {
    "should render the page correctly" in {
      given application: Application = applicationBuilder().build()

      running(application) {
        given request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.OrganisationDetailsCheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        status(result) shouldEqual SEE_OTHER
        redirectLocation(result) shouldEqual Some("next-page-after-organisation-details-check-your-answers")

      }
    }
  }

}

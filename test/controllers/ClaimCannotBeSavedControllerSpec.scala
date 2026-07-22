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

package controllers

import play.api.test.FakeRequest
import play.api.mvc.AnyContentAsEmpty
import controllers.ControllerSpec
import views.html.Warning16UnsubmittedClaimExistsForCharityView
import uk.gov.hmrc.auth.core.AffinityGroup
import play.api.Application
import models.RepaymentClaimDetailsAnswers

class ClaimCannotBeSavedControllerSpec extends ControllerSpec {

  "ClaimCannotBeSavedController" - {
    "onPageLoad" - {
      "should render the WRN16 page for an agent user" in {
        given application: Application = applicationBuilder(
          affinityGroup = AffinityGroup.Agent,
          sessionData = defaultSessionData.copy(repaymentClaimDetailsAnswers =
            Some(
              RepaymentClaimDetailsAnswers(
                nameOfCharity = Some("Test Charity"),
                hmrcCharitiesReference = Some("1234567890")
              )
            )
          )
        ).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimCannotBeSavedController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[Warning16UnsubmittedClaimExistsForCharityView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view(
            Some("Test Charity"),
            Some("1234567890"),
            "http://localhost:8033/charities-management/manage-charity-repayment-claim"
          ).body
        }
      }

      "should reject the request for an organisation user" in {
        given application: Application = applicationBuilder(affinityGroup = AffinityGroup.Organisation).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimCannotBeSavedController.onPageLoad.url)

          val result = route(application, request).value
          status(result) shouldEqual SEE_OTHER
        }
      }

      "should redirect to the Claim complete page when the claim has been submitted" in {
        given application: Application = applicationBuilder(
          sessionData = defaultSessionData.copy(submissionReference = Some("submission-reference")),
          affinityGroup = AffinityGroup.Agent
        ).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ClaimCannotBeSavedController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
    }
  }
}

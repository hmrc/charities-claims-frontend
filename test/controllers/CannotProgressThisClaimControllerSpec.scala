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
import views.html.{Warning15UnsubmittedClaimExistsView, Warning16UnsubmittedClaimExistsForCharityView}
import play.api.Application
import uk.gov.hmrc.auth.core.AffinityGroup

class CannotProgressThisClaimControllerSpec extends ControllerSpec {

  "CannotProgressThisClaimController" - {
    "onPageLoad" - {
      "should render the WRN15 page for an organisation user" in {
        given application: Application = applicationBuilder(affinityGroup = AffinityGroup.Organisation).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CannotProgressThisClaimController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[Warning15UnsubmittedClaimExistsView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view().body
        }
      }

      "should render the WRN16 page for an agent user" in {
        given application: Application = applicationBuilder(affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CannotProgressThisClaimController.onPageLoad.url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[Warning16UnsubmittedClaimExistsForCharityView]

          status(result) shouldEqual OK
          contentAsString(result) shouldEqual view().body
        }
      }

      "should redirect to the Claim complete page when the claim has been submitted" in {
        given application: Application = applicationBuilder(
          sessionData = defaultSessionData.copy(submissionReference = Some("submission-reference")),
          affinityGroup = AffinityGroup.Organisation
        ).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CannotProgressThisClaimController.onPageLoad.url)

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

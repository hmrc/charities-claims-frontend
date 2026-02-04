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

package controllers.repaymentclaimdetailsold

import play.api.test.FakeRequest
import play.api.mvc.AnyContentAsEmpty
import controllers.ControllerSpec
import views.html.IncompleteAnswersView
import play.api.Application
import models.{RepaymentClaimDetailsAnswersOld, SessionData}

class IncompleteAnswersControllerSpec extends ControllerSpec {

  "IncompleteAnswersController" - {
    "onPageLoad" - {
      "should render the page with missing fields when answers are incomplete" in {
        val incompleteAnswers = RepaymentClaimDetailsAnswersOld()
        val sessionData       =
          SessionData(charitiesReference = testCharitiesReference, repaymentClaimDetailsAnswersOld = incompleteAnswers)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.IncompleteAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view                  = application.injector.instanceOf[IncompleteAnswersView]
          val expectedMissingFields = incompleteAnswers.missingFields

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(
            routes.CheckYourAnswersController.onPageLoad.url,
            expectedMissingFields
          ).body
        }
      }

      "should render the page with no missing fields when answers are complete" in {
        val completeAnswers = RepaymentClaimDetailsAnswersOld(
          claimingGiftAid = Some(true),
          claimingTaxDeducted = Some(false),
          claimingUnderGiftAidSmallDonationsScheme = Some(false),
          claimingReferenceNumber = Some(false)
        )
        val sessionData     =
          SessionData(charitiesReference = testCharitiesReference, repaymentClaimDetailsAnswersOld = completeAnswers)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.IncompleteAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[IncompleteAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(
            routes.CheckYourAnswersController.onPageLoad.url,
            Seq.empty
          ).body
        }
      }
    }
  }
}

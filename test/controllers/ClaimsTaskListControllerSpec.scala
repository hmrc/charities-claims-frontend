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

import models.*
import play.api.Application
import play.api.test.FakeRequest

class ClaimsTaskListControllerSpec extends ControllerSpec {

  val url = s"$baseUrl/make-a-charity-repayment-claim"

  def completeRepaymentClaimDetailsAnswersOld(): RepaymentClaimDetailsAnswersOld =
    RepaymentClaimDetailsAnswersOld(
      claimingGiftAid = Some(false),
      claimingTaxDeducted = Some(false),
      claimingUnderGiftAidSmallDonationsScheme = Some(false),
      claimingReferenceNumber = Some(false)
    )

  "ClaimsTaskListController" - {

    "onPageLoad" - {

      "should render the page with correct heading" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include("Make a charity repayment claim")
        }
      }

      "should display About the claim section with all tasks when repaymentClaimDetails complete" in {
        val sessionData = SessionData(repaymentClaimDetailsAnswersOld = completeRepaymentClaimDetailsAnswersOld())

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("About the claim")
          contentAsString(result) should include("Provide repayment claim details")
          contentAsString(result) should include("Provide organisation details")
          contentAsString(result) should include("Gift Aid Small Donations Scheme details")
        }
      }

      "should display Upload documents section with all tasks when repaymentClaimDetails complete" in {
        val sessionData = SessionData(repaymentClaimDetailsAnswersOld = completeRepaymentClaimDetailsAnswersOld())

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Upload documents")
          contentAsString(result) should include("Add Gift Aid schedule")
          contentAsString(result) should include("Add other income schedule")
          contentAsString(result) should include("Add community buildings schedule")
          contentAsString(result) should include("Add connected charities schedule")
        }
      }

      "should display Declaration section when Repayment Claim Details complete" in {
        val sessionData = SessionData(repaymentClaimDetailsAnswersOld = completeRepaymentClaimDetailsAnswersOld())

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Declaration")
          contentAsString(result) should include("Read declaration")
        }
      }

      "should display declaration warning when other sections incomplete" in {
        val sessionData = SessionData(repaymentClaimDetailsAnswersOld = completeRepaymentClaimDetailsAnswersOld())

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("You must complete every section before you can declare.")
        }
      }

      "should display Delete claim link when repaymentClaimDetails complete" in {
        val sessionData = SessionData(repaymentClaimDetailsAnswersOld = completeRepaymentClaimDetailsAnswersOld())

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Delete claim")
        }
      }

      "should display Go to dashboard link" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Go to dashboard")
        }
      }

      "should show status tags for tasks" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Incomplete")
        }
      }

      "should show Completed status for GASDS and upload tasks" in {
        val sessionData = SessionData(repaymentClaimDetailsAnswersOld = completeRepaymentClaimDetailsAnswersOld())

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value
          val content = contentAsString(result)

          content should include("Completed")
        }
      }

      "should use govuk-heading-l class for the page heading" in {
        given application: Application = applicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("""<h1 class="govuk-heading-l">""")
          contentAsString(result) shouldNot include("""<h1 class="govuk-heading-xl">""")
        }
      }

      "should not display Upload documents section when Repayment Claim Details incomplete" in {
        val incompleteAnswers = RepaymentClaimDetailsAnswersOld(
          claimingGiftAid = Some(true),
          claimingTaxDeducted = None
        )
        val sessionData       = SessionData(repaymentClaimDetailsAnswersOld = incompleteAnswers)

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) shouldNot include("Upload documents")
        }
      }

      "should display Declaration section as Cannot start yet when Repayment Claim Details incomplete" in {
        val incompleteAnswers = RepaymentClaimDetailsAnswersOld(
          claimingGiftAid = Some(true),
          claimingTaxDeducted = None
        )
        val sessionData       = SessionData(repaymentClaimDetailsAnswersOld = incompleteAnswers)

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Declaration")
          contentAsString(result) should include("Cannot start yet")
        }
      }

      "should display declaration warning as hint text within task item" in {
        val sessionData = SessionData(repaymentClaimDetailsAnswersOld = completeRepaymentClaimDetailsAnswersOld())

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("govuk-task-list__hint")
          contentAsString(result) should include("You must complete every section before you can declare.")
        }
      }

      "should link GASDS and upload tasks to page not found" in {
        val sessionData = SessionData(repaymentClaimDetailsAnswersOld = completeRepaymentClaimDetailsAnswersOld())

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("/charities-claims/page-not-found")
        }
      }
    }
  }
}

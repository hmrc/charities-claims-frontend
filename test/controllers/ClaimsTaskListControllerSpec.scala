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

  val testClaimId = "claim-123"

  val repaymentClaimDetailsAnswersCompleted: RepaymentClaimDetailsAnswers =
    RepaymentClaimDetailsAnswers(
      claimingGiftAid = Some(false),
      claimingTaxDeducted = Some(false),
      claimingUnderGiftAidSmallDonationsScheme = Some(false),
      claimingReferenceNumber = Some(false)
    )

  def sessionDataWithClaimId(): SessionData =
    SessionData(
      charitiesReference = testCharitiesReference,
      unsubmittedClaimId = Some(testClaimId)
    )

  def sessionDataWithCompleteRcd(): SessionData =
    SessionData(
      charitiesReference = testCharitiesReference,
      repaymentClaimDetailsAnswers = Some(repaymentClaimDetailsAnswersCompleted),
      unsubmittedClaimId = Some(testClaimId)
    )

  "ClaimsTaskListController" - {

    "onPageLoad" - {

      "should render the page with correct heading" in {
        given application: Application = applicationBuilder(sessionDataWithClaimId()).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include("Make a charity repayment claim")
        }
      }

      "should display About the claim section with all tasks when repaymentClaimDetails complete" in {
        given application: Application = applicationBuilder(sessionDataWithCompleteRcd()).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("About the claim")
          contentAsString(result) should include("Provide repayment claim details")
          contentAsString(result) should include("Provide organisation details")
        }
      }

      "should display GASDS task when claimingUnderGiftAidSmallDonationsScheme is true" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(true),
          claimingDonationsNotFromCommunityBuilding = Some(true),
          claimingDonationsCollectedInCommunityBuildings = Some(false),
          connectedToAnyOtherCharities = Some(false),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Gift Aid Small Donations Scheme details")
        }
      }

      "should not display GASDS task when claimingUnderGiftAidSmallDonationsScheme is false" in {
        given application: Application = applicationBuilder(sessionDataWithCompleteRcd()).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) shouldNot include("Gift Aid Small Donations Scheme details")
        }
      }

      "should display Gift Aid schedule task when claimingGiftAid is true" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(claimingGiftAid = Some(true))
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Add Gift Aid schedule")
        }
      }

      "should not display Gift Aid schedule task when claimingGiftAid is false" in {
        given application: Application = applicationBuilder(sessionDataWithCompleteRcd()).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) shouldNot include("Add Gift Aid schedule")
        }
      }

      "should display Other income schedule task when claimingTaxDeducted is true" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(claimingTaxDeducted = Some(true))
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Add other income schedule")
        }
      }

      "should not display Other income schedule task when claimingTaxDeducted is false" in {
        given application: Application = applicationBuilder(sessionDataWithCompleteRcd()).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) shouldNot include("Add other income schedule")
        }
      }

      "should display Community buildings schedule task when claimingDonationsCollectedInCommunityBuildings is true" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(true),
          claimingDonationsCollectedInCommunityBuildings = Some(true),
          claimingDonationsNotFromCommunityBuilding = Some(true),
          connectedToAnyOtherCharities = Some(false),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Add community buildings schedule")
        }
      }

      "should not display Community buildings schedule task when claimingDonationsCollectedInCommunityBuildings is false" in {
        given application: Application = applicationBuilder(sessionDataWithCompleteRcd()).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) shouldNot include("Add community buildings schedule")
        }
      }

      "should display Connected charities schedule task when connectedToAnyOtherCharities is true" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(true),
          connectedToAnyOtherCharities = Some(true),
          claimingDonationsNotFromCommunityBuilding = Some(true),
          claimingDonationsCollectedInCommunityBuildings = Some(false),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Add connected charities schedule")
        }
      }

      "should not display Connected charities schedule task when connectedToAnyOtherCharities is false" in {
        given application: Application = applicationBuilder(sessionDataWithCompleteRcd()).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) shouldNot include("Add connected charities schedule")
        }
      }

      "should not display Upload documents section when no upload tasks are visible" in {
        given application: Application = applicationBuilder(sessionDataWithCompleteRcd()).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) shouldNot include("Upload documents")
        }
      }

      "should display Upload documents section when at least one upload task is visible" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(claimingGiftAid = Some(true))
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Upload documents")
        }
      }

      "should show Gift Aid schedule as Incomplete when not completed" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(claimingGiftAid = Some(true))
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Incomplete")
        }
      }

      "should show Gift Aid schedule as Completed when giftAidScheduleCompleted is true" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(claimingGiftAid = Some(true))
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers),
          giftAidScheduleCompleted = true
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value
          val content = contentAsString(result)

          content should include("Add Gift Aid schedule")
          content should include("Completed")
        }
      }

      "should show Other income schedule as Incomplete when not completed" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(claimingTaxDeducted = Some(true))
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Incomplete")
        }
      }

      "should show Other income schedule as Completed when otherIncomeScheduleCompleted is true" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(claimingTaxDeducted = Some(true))
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers),
          otherIncomeScheduleCompleted = true
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value
          val content = contentAsString(result)

          content should include("Add other income schedule")
          content should include("Completed")
        }
      }

      "should show Declaration as CannotStartYet when upload tasks are incomplete" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(claimingGiftAid = Some(true))
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Cannot start yet")
          contentAsString(result) should include("You must complete every section before you can declare.")
        }
      }

      "should display Declaration section when Repayment Claim Details complete" in {
        given application: Application = applicationBuilder(sessionDataWithCompleteRcd()).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Declaration")
          contentAsString(result) should include("Read declaration")
        }
      }

      "should display declaration warning when other sections incomplete" in {
        given application: Application = applicationBuilder(sessionDataWithCompleteRcd()).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("You must complete every section before you can declare.")
        }
      }

      "should display Delete claim link when repaymentClaimDetails complete" in {
        given application: Application = applicationBuilder(sessionDataWithCompleteRcd()).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Delete claim")
        }
      }

      "should display Go to dashboard link pointing to management frontend" in {
        given application: Application = applicationBuilder(sessionDataWithClaimId()).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value
          val content = contentAsString(result)

          content should include("Go to dashboard")
          content should include("http://localhost:8033/charities-management/charity-repayment-dashboard")
        }
      }

      "should show status tags for tasks" in {
        given application: Application = applicationBuilder(sessionDataWithClaimId()).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Incomplete")
        }
      }

      "should use govuk-heading-l class for the page heading" in {
        given application: Application = applicationBuilder(sessionDataWithClaimId()).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("""<h1 class="govuk-heading-l">""")
          contentAsString(result) shouldNot include("""<h1 class="govuk-heading-xl">""")
        }
      }

      "should not display Upload documents section when Repayment Claim Details incomplete" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId)
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) shouldNot include("Upload documents")
        }
      }

      "should display Declaration section as Cannot start yet when Repayment Claim Details incomplete" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId)
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Declaration")
          contentAsString(result) should include("Cannot start yet")
        }
      }

      "should display declaration warning as hint text within task item" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId)
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("govuk-task-list__hint")
          contentAsString(result) should include("You must complete every section before you can declare.")
        }
      }

      "should display caption with HMRC Charities reference" in {
        given application: Application = applicationBuilder(sessionDataWithClaimId()).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value
          val content = contentAsString(result)

          content should include("""class="govuk-caption-l"""")
          content should include("HMRC Charities reference:")
        }
      }

      "should not display post create claim sections when answers complete but unsubmittedClaimId is None" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          repaymentClaimDetailsAnswers = Some(repaymentClaimDetailsAnswersCompleted),
          unsubmittedClaimId = None
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value
          val content = contentAsString(result)

          content should include(messages("claimsTaskList.task.repaymentClaimDetails"))
          content shouldNot include(messages("claimsTaskList.task.organisationDetails"))
          content shouldNot include(messages("claimsTaskList.link.deleteClaim"))
          content should include(messages("claimsTaskList.status.cannotStartYet"))
        }
      }

      "should have unique IDs for task list status elements" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId)
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value
          val content = contentAsString(result)

          val idPattern    = """id="([^"]*-status)"""".r
          val statusIds    = idPattern.findAllMatchIn(content).map(_.group(1)).toSeq
          val duplicateIds = statusIds.groupBy(identity).filter(_._2.size > 1).keys

          duplicateIds shouldBe empty
        }
      }
    }
  }
}

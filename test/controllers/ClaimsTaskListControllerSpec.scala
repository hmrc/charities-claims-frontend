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
import util.*
import play.api.mvc.{Security as _, *}
import play.api.Application
import models.{SessionData, *}
import play.api.libs.json.Json

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
      val fileUploadReference = FileUploadReference("test-file-upload-reference")
      val uploadUrl           = "http://foo.bar.com/upscan-upload-proxy/bucketName"
      val callbackUrl         = "http://example.com:1235/charities-claims-validation/claim-1234567890/upscan-callback"
      val responseJson        =
        s"""{
              "reference": "11370e18-6e24-453e-b45a-76d3e32ea33d",
              "uploadRequest": {
                  "href": "$uploadUrl",
                  "fields": {
                      "Content-Type": "application/xml",
                      "acl": "private",
                      "key": "xxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
                      "policy": "xxxxxxxx==",
                      "x-amz-algorithm": "AWS4-HMAC-SHA256",
                      "x-amz-credential": "ASIAxxxxxxxxx/20180202/eu-west-2/s3/aws4_request",
                      "x-amz-date": "yyyyMMddThhmmssZ",
                      "x-amz-meta-callback-url": "$callbackUrl",
                      "x-amz-signature": "xxxx",
                      "success_action_redirect": "http://foo.bar.com/success",
                      "error_action_redirect": "http://foo.bar.com/error"
                  }
              }
            }""".stripMargin

      val upscanResponse = Json.parse(responseJson).as[UpscanInitiateResponse]

      "should render the page with correct heading" in {
        given application: Application = applicationBuilder(TestClaims.testClaimUnsubmitted, Seq.empty).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result) should include("Make a charity repayment claim")
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

      "should not display GASDS task when GASDS is true but all sub-fields are NO" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(true),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(false),
          connectedToAnyOtherCharities = Some(false)
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

          contentAsString(result) shouldNot include("Gift Aid Small Donations Scheme details")
        }
      }

      "should display GASDS task when makingAdjustmentToPreviousClaim is true" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(true),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(true),
          connectedToAnyOtherCharities = Some(false),
          makingAdjustmentToPreviousClaim = Some(true)
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

      "should show Community buildings schedule as inProgress when there is file upload" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(true),
          connectedToAnyOtherCharities = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(true),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(
          communityBuildingsScheduleCompleted = false,
          communityBuildingsScheduleFileUploadReference = Some(fileUploadReference)
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value
          val content = contentAsString(result)

          content should include("Add community buildings schedule")
          content should include("In progress")
        }
      }

      "should show Community buildings as inProgress when upscan is initiated" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(true),
          connectedToAnyOtherCharities = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(true),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(
          communityBuildingsScheduleCompleted = false,
          communityBuildingsScheduleUpscanInitialization = Some(upscanResponse)
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value
          val content = contentAsString(result)

          content should include("Add community buildings schedule")
          content should include("In progress")
        }
      }

      "should show Community buildings as Completed when schedule upload completed" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(
          claimingUnderGiftAidSmallDonationsScheme = Some(true),
          connectedToAnyOtherCharities = Some(false),
          claimingDonationsNotFromCommunityBuilding = Some(false),
          claimingDonationsCollectedInCommunityBuildings = Some(true),
          makingAdjustmentToPreviousClaim = Some(false)
        )
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers),
          communityBuildingsScheduleCompleted = true
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value
          val content = contentAsString(result)

          content should include("Add community buildings schedule")
          content should include("Completed")
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

      "should show Connected charities schedule as inProgress when there is file upload" in {
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
        ).copy(
          connectedCharitiesScheduleCompleted = false,
          connectedCharitiesScheduleFileUploadReference = Some(fileUploadReference)
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value
          val content = contentAsString(result)

          content should include("Add connected charities schedule")
          content should include("In progress")
        }
      }

      "should show Connected charities as inProgress when upscan is initiated" in {
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
        ).copy(
          connectedCharitiesScheduleCompleted = false,
          connectedCharitiesScheduleUpscanInitialization = Some(upscanResponse)
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value
          val content = contentAsString(result)

          content should include("Add connected charities schedule")
          content should include("In progress")
        }
      }

      "should show Connected charities as Completed when schedule upload completed" in {
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
          repaymentClaimDetailsAnswers = Some(answers),
          connectedCharitiesScheduleCompleted = true
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value
          val content = contentAsString(result)

          content should include("Add connected charities schedule")
          content should include("Completed")
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

      "should show Gift Aid schedule as inProgress when there is file upload" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(claimingGiftAid = Some(true))
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(giftAidScheduleCompleted = false, giftAidScheduleFileUploadReference = Some(fileUploadReference))

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value
          val content = contentAsString(result)

          content should include("Add Gift Aid schedule")
          content should include("In progress")
        }
      }

      "should show Gift Aid schedule as inProgress when upscan is initiated" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(claimingGiftAid = Some(true))
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(giftAidScheduleCompleted = false, giftAidScheduleUpscanInitialization = Some(upscanResponse))

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value
          val content = contentAsString(result)

          content should include("Add Gift Aid schedule")
          content should include("In progress")
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

      "should show Other income schedule as inProgress when there is file upload" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(claimingTaxDeducted = Some(true))
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(otherIncomeScheduleCompleted = false, otherIncomeScheduleFileUploadReference = Some(fileUploadReference))

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value
          val content = contentAsString(result)

          content should include("Add other income schedule")
          content should include("In progress")
        }
      }

      "should show Other income schedule as inProgress when upscan is initiated" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(claimingTaxDeducted = Some(true))
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers)
        ).copy(otherIncomeScheduleCompleted = false, otherIncomeScheduleUpscanInitialization = Some(upscanResponse))

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value
          val content = contentAsString(result)

          content should include("Add other income schedule")
          content should include("In progress")
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

          content should include("Go to manage charity repayment claims")
          content should include("http://localhost:8033/charities-management/charity-repayment-dashboard")
        }
      }

      "should show status tags for tasks" in {
        given application: Application = applicationBuilder(sessionDataWithClaimId()).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Not yet started")
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

      "should display Declaration section as Not yet started when Repayment & organisation Claim Details complete" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(claimingGiftAid = Some(true))
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers),
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
          ),
          giftAidScheduleCompleted = true
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Declaration")
          contentAsString(result) should include("Not yet started")
        }
      }

      "should display Declaration section as in progress when Repayment & organisation Claim Details complete & some text for adjustments Claim prompt" in {
        val answers     = repaymentClaimDetailsAnswersCompleted.copy(claimingGiftAid = Some(true))
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          unsubmittedClaimId = Some(testClaimId),
          repaymentClaimDetailsAnswers = Some(answers),
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
          ),
          giftAidScheduleCompleted = true,
          includedAnyAdjustmentsInClaimPrompt = Some("some reason for adjustment")
        )

        given application: Application = applicationBuilder(sessionData).build()

        running(application) {
          val request = FakeRequest(GET, url)
          val result  = route(application, request).value

          contentAsString(result) should include("Declaration")
          contentAsString(result) should include("In progress")
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

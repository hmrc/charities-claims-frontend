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

package controllers.giftAidSchedule

import connectors.ClaimsValidationConnector
import controllers.ControllerSpec
import controllers.giftAidSchedule.routes
import models.*
import models.requests.DataRequest
import play.api.Application
import play.api.inject.guice.GuiceableModule
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.inject
import services.ClaimsValidationService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class ProblemWithGiftAidScheduleControllerSpec extends ControllerSpec {

  val mockClaimsValidationConnector: ClaimsValidationConnector = mock[ClaimsValidationConnector]
  val mockClaimsValidationService: ClaimsValidationService     = mock[ClaimsValidationService]

  override protected val additionalBindings: List[GuiceableModule] = List(
    inject.bind[ClaimsValidationConnector].toInstance(mockClaimsValidationConnector),
    inject.bind[ClaimsValidationService].toInstance(mockClaimsValidationService)
  )

  val testClaimId: String                          = "test-claim-id-123"
  val testFileUploadReference: FileUploadReference = FileUploadReference("test-file-upload-ref")

  val testGiftAidScheduleData: GiftAidScheduleData = GiftAidScheduleData(
    earliestDonationDate = Some("2025-01-01"),
    prevOverclaimedGiftAid = Some(100.00),
    totalDonations = Some(500.00),
    donations = Seq.empty
  )

  val testValidationErrors: Seq[ValidationError] = Seq(
    ValidationError("earliestDonationDate", "ERROR: Earliest donation date is missing."),
    ValidationError("donations[0]", "ERROR: Item You have entered data in an invalid area of the form."),
    ValidationError(
      "donations[2]",
      "ERROR: Item 3 You cannot provide both a title and an entry for an aggregated donation box."
    )
  )

  val testValidationFailedResponse: GetUploadResultValidationFailedGiftAid =
    GetUploadResultValidationFailedGiftAid(
      reference = testFileUploadReference,
      giftAidScheduleData = testGiftAidScheduleData,
      errors = testValidationErrors
    )

  "ProblemWithGiftAidScheduleController" - {

    "onPageLoad" - {

      // Redirect Tests

      "should redirect to RepaymentClaimDetailsController when no claimId in session" in {
        val sessionData = defaultSessionData.copy(unsubmittedClaimId = None)

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithGiftAidScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.repaymentClaimDetails.routes.RepaymentClaimDetailsController.onPageLoad.url
          )
        }
      }

      "should redirect to UploadGiftAidScheduleController when no file upload reference in session" in {
        val sessionData = defaultSessionData.copy(
          unsubmittedClaimId = Some(testClaimId),
          giftAidScheduleFileUploadReference = None
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.ProblemWithGiftAidScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadGiftAidScheduleController.onPageLoad.url
          )
        }
      }

      // tests requiring mock connector setup to follow - see TODO below
      // TODO: Add tests for successful page render with validation errors
      // tests require mocking ClaimsValidationConnector.getUploadResult to return
      // GetUploadResultValidationFailedGiftAid with test errors
      //
      // tests to add:
      // - "should render page with validation errors when upload has VALIDATION_FAILED status"
      // - "should display correct error count in the table"
      // - "should include the giftAidScheduleSpreadsheetGuidanceUrl link"
      // - "should paginate errors correctly when more than 10 errors"
      // - "should redirect to UploadGiftAidScheduleController when button is clicked to return to upload page"
      // - "should redirect to DeleteGiftAidScheduleController when delete action is triggered"

    }

    "onSubmit" - {

      "should delete the gift aid schedule and redirect to UploadGiftAidScheduleController" in {
        val sessionData = defaultSessionData.copy(
          unsubmittedClaimId = Some(testClaimId),
          giftAidScheduleFileUploadReference = Some(testFileUploadReference)
        )

        (mockClaimsValidationService
          .deleteGiftAidSchedule(using _: DataRequest[?], _: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(()))

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.ProblemWithGiftAidScheduleController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UploadGiftAidScheduleController.onPageLoad.url
          )
        }
      }

    }
  }
}

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

package controllers.actions

import connectors.{ClaimsConnector, ClaimsValidationConnector}
import models.*
import models.requests.{AuthorisedRequest, DataRequest}
import play.api.mvc.Results.*
import play.api.test.Helpers.*
import play.api.test.Helpers
import repositories.SessionCache
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier
import util.{BaseSpec, TestClaims}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class RefreshDataActionSpec extends BaseSpec {

  val request                       = FakeRequest("GET", "/test")
  val authorisedRequestOrganisation = AuthorisedRequest(request, AffinityGroup.Organisation, testCharitiesReference)
  val authorisedRequestAgent        = AuthorisedRequest(request, AffinityGroup.Agent, testCharitiesReference)

  given SessionData = SessionData.empty(testCharitiesReference)

  "RefreshDataAction" - {
    "refines AuthorisedRequest into a DataRequest when session data exists" in {
      val mockSessionCache              = mock[SessionCache]
      val mockClaimsConnector           = mock[ClaimsConnector]
      val mockClaimsValidationConnector = mock[ClaimsValidationConnector]

      val action = new DefaultRefreshDataAction(
        mockSessionCache,
        mockClaimsConnector,
        mockClaimsValidationConnector,
        new DefaultDataRetrievalAction(
          mockSessionCache,
          mockClaimsConnector,
          mockClaimsValidationConnector,
          testFrontendAppConfig
        ),
        testFrontendAppConfig
      )

      val sessionData =
        RepaymentClaimDetailsAnswers
          .setClaimingTaxDeducted(true)
          .and(SessionData.setUnsubmittedClaimId("claim-123"))

      (mockSessionCache
        .get()(using _: HeaderCarrier))
        .expects(*)
        .returning(Future.successful(Some(sessionData)))

      (mockSessionCache
        .store(_: SessionData)(using _: HeaderCarrier))
        .expects(*, *)
        .returning(Future.successful(()))

      (mockClaimsValidationConnector
        .getUploadSummary(_: String)(using _: HeaderCarrier))
        .expects(*, *)
        .returning(Future.successful(GetUploadSummaryResponse(uploads = Nil)))

      val claim = TestClaims.testClaimWithRepaymentClaimDetailsOnly()

      (mockClaimsConnector
        .getClaim(_: String)(using _: HeaderCarrier))
        .expects(*, *)
        .returning(Future.successful(Some(claim)))

      val result = action.invokeBlock(
        authorisedRequestOrganisation,
        (req: DataRequest[?]) =>
          req.sessionData shouldBe SessionData.from(
            claim,
            testCharitiesReference,
            Some(GetUploadSummaryResponse(uploads = Nil))
          )
          Future.successful(Ok)
      )
      status(result) shouldBe OK
    }

    "refines AuthorisedRequest into a DataRequest when session data object doesn't exist and some claims and uploads summary are retrieved from backend" in {
      val mockSessionCache              = mock[SessionCache]
      val mockClaimsConnector           = mock[ClaimsConnector]
      val mockClaimsValidationConnector = mock[ClaimsValidationConnector]
      val action                        = new DefaultRefreshDataAction(
        mockSessionCache,
        mockClaimsConnector,
        mockClaimsValidationConnector,
        new DefaultDataRetrievalAction(
          mockSessionCache,
          mockClaimsConnector,
          mockClaimsValidationConnector,
          testFrontendAppConfig
        ),
        testFrontendAppConfig
      )

      val sessionData =
        RepaymentClaimDetailsAnswers
          .setClaimingTaxDeducted(true)
          .and(SessionData.setUnsubmittedClaimId("claim-123"))

      (mockSessionCache
        .get()(using _: HeaderCarrier))
        .expects(*)
        .returning(Future.successful(Some(sessionData)))

      (mockSessionCache
        .store(_: SessionData)(using _: HeaderCarrier))
        .expects(*, *)
        .returning(Future.successful(()))

      val getUploadSummaryResponseWithGiftAidAwaitingUpload =
        GetUploadSummaryResponse(uploads =
          Seq(
            UploadSummary(
              reference = FileUploadReference("gift-aid-ref-123"),
              validationType = ValidationType.GiftAid,
              fileStatus = FileStatus.AWAITING_UPLOAD,
              uploadUrl = Some("https://www.foo.bar.com"),
              fields = Some(Map("key" -> "value"))
            )
          )
        )

      (mockClaimsValidationConnector
        .getUploadSummary(_: String)(using _: HeaderCarrier))
        .expects(*, *)
        .returning(
          Future.successful(getUploadSummaryResponseWithGiftAidAwaitingUpload)
        )

      val claim = TestClaims.testClaimWithRepaymentClaimDetailsOnly()

      (mockClaimsConnector
        .getClaim(_: String)(using _: HeaderCarrier))
        .expects(*, *)
        .returning(Future.successful(Some(claim)))

      val result = action.invokeBlock(
        authorisedRequestOrganisation,
        (req: DataRequest[?]) =>
          req.sessionData shouldBe
            SessionData.from(claim, testCharitiesReference, Some(getUploadSummaryResponseWithGiftAidAwaitingUpload))

          req.sessionData.organisationDetailsAnswers                        shouldBe None
          req.sessionData.giftAidScheduleData                               shouldBe None
          req.sessionData.giftAidScheduleFileUploadReference                shouldBe Some(FileUploadReference("gift-aid-ref-123"))
          req.sessionData.giftAidScheduleUpscanInitialization               shouldBe Some(
            UpscanInitiateResponse(
              reference = UpscanReference("gift-aid-ref-123"),
              uploadRequest = UploadRequest(
                href = "https://www.foo.bar.com",
                fields = Map("key" -> "value")
              )
            )
          )
          req.sessionData.includedAnyAdjustmentsInClaimPrompt               shouldBe None
          req.sessionData.understandFalseStatements                         shouldBe None
          req.sessionData.otherIncomeScheduleData                           shouldBe None
          req.sessionData.otherIncomeScheduleFileUploadReference            shouldBe None
          req.sessionData.communityBuildingsScheduleData                    shouldBe None
          req.sessionData.communityBuildingsScheduleFileUploadReference     shouldBe None
          req.sessionData.connectedCharitiesScheduleData                    shouldBe None
          req.sessionData.connectedCharitiesScheduleFileUploadReference     shouldBe None
          req.sessionData.giftAidSmallDonationsSchemeDonationDetailsAnswers shouldBe None
          Future.successful(Ok)
      )
      status(result) shouldBe OK
    }

    "refines AuthorisedRequest into a DataRequest when session data object exists but and unsubmitted claim exists too" in {
      val mockSessionCache              = mock[SessionCache]
      val mockClaimsConnector           = mock[ClaimsConnector]
      val mockClaimsValidationConnector = mock[ClaimsValidationConnector]
      val action                        = new DefaultRefreshDataAction(
        mockSessionCache,
        mockClaimsConnector,
        mockClaimsValidationConnector,
        new DefaultDataRetrievalAction(
          mockSessionCache,
          mockClaimsConnector,
          mockClaimsValidationConnector,
          testFrontendAppConfig
        ),
        testFrontendAppConfig
      )

      val sessionData = RepaymentClaimDetailsAnswers
        .setClaimingTaxDeducted(true)
        .and(SessionData.setUnsubmittedClaimId("claim-123"))

      (mockSessionCache
        .get()(using _: HeaderCarrier))
        .expects(*)
        .anyNumberOfTimes()
        .returning(Future.successful(Some(sessionData)))

      val claim = TestClaims.testClaimWithRepaymentClaimDetailsOnly()

      (mockClaimsConnector
        .getClaim(_: String)(using _: HeaderCarrier))
        .expects(*, *)
        .returning(Future.successful(Some(claim)))

      val getUploadSummaryResponseWithGiftAidAwaitingUpload =
        GetUploadSummaryResponse(uploads =
          Seq(
            UploadSummary(
              reference = FileUploadReference("gift-aid-ref-123"),
              validationType = ValidationType.GiftAid,
              fileStatus = FileStatus.AWAITING_UPLOAD,
              uploadUrl = Some("https://www.foo.bar.com"),
              fields = Some(Map("key" -> "value"))
            )
          )
        )

      (mockClaimsValidationConnector
        .getUploadSummary(_: String)(using _: HeaderCarrier))
        .expects(*, *)
        .returning(
          Future.successful(getUploadSummaryResponseWithGiftAidAwaitingUpload)
        )

      (mockSessionCache
        .store(_: SessionData)(using _: HeaderCarrier))
        .expects(*, *)
        .returning(Future.successful(()))

      val result = action.invokeBlock(
        authorisedRequestAgent,
        (req: DataRequest[?]) =>
          req.sessionData shouldEqual SessionData
            .from(
              claim,
              testCharitiesReference,
              Some(getUploadSummaryResponseWithGiftAidAwaitingUpload)
            )
            .copy(isAgent = true)
          Future.successful(Ok)
      )
      status(result) shouldBe OK
    }

  }

  "refines AuthorisedRequest into a DataRequest when session data object exists but no claim has been submitted yet" in {
    val mockSessionCache              = mock[SessionCache]
    val mockClaimsConnector           = mock[ClaimsConnector]
    val mockClaimsValidationConnector = mock[ClaimsValidationConnector]
    val action                        = new DefaultRefreshDataAction(
      mockSessionCache,
      mockClaimsConnector,
      mockClaimsValidationConnector,
      new DefaultDataRetrievalAction(
        mockSessionCache,
        mockClaimsConnector,
        mockClaimsValidationConnector,
        testFrontendAppConfig
      ),
      testFrontendAppConfig
    )

    val sessionData = RepaymentClaimDetailsAnswers.setClaimingTaxDeducted(true)

    (mockSessionCache
      .get()(using _: HeaderCarrier))
      .expects(*)
      .anyNumberOfTimes()
      .returning(Future.successful(Some(sessionData)))

    val result = action.invokeBlock(
      authorisedRequestAgent,
      (req: DataRequest[?]) =>
        req.sessionData shouldBe sessionData
        Future.successful(Ok)
    )
    status(result) shouldBe SEE_OTHER
    redirectLocation(result) shouldBe Some(testFrontendAppConfig.charityRepaymentDashboardUrl)
  }

  "refines AuthorisedRequest into a DataRequest when submitted session data object exists and claimId=blank is provided" in {
    val mockSessionCache              = mock[SessionCache]
    val mockClaimsConnector           = mock[ClaimsConnector]
    val mockClaimsValidationConnector = mock[ClaimsValidationConnector]
    val action                        = new DefaultRefreshDataAction(
      mockSessionCache,
      mockClaimsConnector,
      mockClaimsValidationConnector,
      new DefaultDataRetrievalAction(
        mockSessionCache,
        mockClaimsConnector,
        mockClaimsValidationConnector,
        testFrontendAppConfig
      ),
      testFrontendAppConfig
    )

    val sessionData = SessionData
      .empty(testCharitiesReference)
      .copy(submissionReference = Some("submission-123"))

    (mockSessionCache
      .get()(using _: HeaderCarrier))
      .expects(*)
      .returning(Future.successful(Some(sessionData)))

    (mockSessionCache
      .store(_: SessionData)(using _: HeaderCarrier))
      .expects(*, *)
      .returning(Future.successful(()))

    val request =
      AuthorisedRequest(FakeRequest("GET", "/test?claimId=blank"), AffinityGroup.Organisation, testCharitiesReference)

    val result = action.invokeBlock(
      request,
      (req: DataRequest[?]) =>
        req.sessionData shouldBe SessionData.empty(testCharitiesReference)
        Future.successful(Ok)
    )
    status(result) shouldBe OK
  }

  "refines AuthorisedRequest into a DataRequest when unsubmitted claim exists and claimId=blank is provided" in {
    val mockSessionCache              = mock[SessionCache]
    val mockClaimsConnector           = mock[ClaimsConnector]
    val mockClaimsValidationConnector = mock[ClaimsValidationConnector]
    val action                        = new DefaultRefreshDataAction(
      mockSessionCache,
      mockClaimsConnector,
      mockClaimsValidationConnector,
      new DefaultDataRetrievalAction(
        mockSessionCache,
        mockClaimsConnector,
        mockClaimsValidationConnector,
        testFrontendAppConfig
      ),
      testFrontendAppConfig
    )

    val sessionData =
      RepaymentClaimDetailsAnswers
        .setClaimingTaxDeducted(true)
        .and(SessionData.setUnsubmittedClaimId("claim-123"))

    (mockSessionCache
      .get()(using _: HeaderCarrier))
      .expects(*)
      .returning(Future.successful(Some(sessionData)))

    (mockSessionCache
      .store(_: SessionData)(using _: HeaderCarrier))
      .expects(*, *)
      .returning(Future.successful(()))

    val getUploadSummaryResponseWithGiftAidAwaitingUpload =
      GetUploadSummaryResponse(uploads =
        Seq(
          UploadSummary(
            reference = FileUploadReference("gift-aid-ref-123"),
            validationType = ValidationType.GiftAid,
            fileStatus = FileStatus.AWAITING_UPLOAD,
            uploadUrl = Some("https://www.foo.bar.com"),
            fields = Some(Map("key" -> "value"))
          )
        )
      )

    (mockClaimsValidationConnector
      .getUploadSummary(_: String)(using _: HeaderCarrier))
      .expects(*, *)
      .returning(
        Future.successful(getUploadSummaryResponseWithGiftAidAwaitingUpload)
      )

    val claim = TestClaims.testClaimWithRepaymentClaimDetailsOnly()

    (mockClaimsConnector
      .getClaim(_: String)(using _: HeaderCarrier))
      .expects(*, *)
      .returning(Future.successful(Some(claim)))

    val request =
      AuthorisedRequest(FakeRequest("GET", "/test?claimId=blank"), AffinityGroup.Organisation, testCharitiesReference)

    val result = action.invokeBlock(
      request,
      (req: DataRequest[?]) =>
        req.sessionData shouldBe
          SessionData.from(claim, testCharitiesReference, Some(getUploadSummaryResponseWithGiftAidAwaitingUpload))

        req.sessionData.organisationDetailsAnswers                        shouldBe None
        req.sessionData.giftAidScheduleData                               shouldBe None
        req.sessionData.giftAidScheduleFileUploadReference                shouldBe Some(FileUploadReference("gift-aid-ref-123"))
        req.sessionData.giftAidScheduleUpscanInitialization               shouldBe Some(
          UpscanInitiateResponse(
            reference = UpscanReference("gift-aid-ref-123"),
            uploadRequest = UploadRequest(
              href = "https://www.foo.bar.com",
              fields = Map("key" -> "value")
            )
          )
        )
        req.sessionData.includedAnyAdjustmentsInClaimPrompt               shouldBe None
        req.sessionData.understandFalseStatements                         shouldBe None
        req.sessionData.otherIncomeScheduleData                           shouldBe None
        req.sessionData.otherIncomeScheduleFileUploadReference            shouldBe None
        req.sessionData.communityBuildingsScheduleData                    shouldBe None
        req.sessionData.communityBuildingsScheduleFileUploadReference     shouldBe None
        req.sessionData.connectedCharitiesScheduleData                    shouldBe None
        req.sessionData.connectedCharitiesScheduleFileUploadReference     shouldBe None
        req.sessionData.giftAidSmallDonationsSchemeDonationDetailsAnswers shouldBe None
        Future.successful(Ok)
    )
    status(result) shouldBe OK
  }
}

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

package controllers.connectedCharitiesSchedule

import connectors.ClaimsValidationConnector
import controllers.ControllerSpec
import controllers.connectedCharitiesSchedule.routes
import models.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import services.{ClaimsService, ClaimsValidationService, SaveService}
import util.TestResources

import scala.concurrent.Future
import uk.gov.hmrc.auth.core.AffinityGroup

class CheckYourConnectedCharitiesScheduleControllerSpec extends ControllerSpec {

  val mockClaimsValidationConnector: ClaimsValidationConnector = mock[ClaimsValidationConnector]
  val mockClaimsValidationService: ClaimsValidationService     = mock[ClaimsValidationService]
  val mockClaimsService: ClaimsService                         = mock[ClaimsService]
  val mockSaveService: SaveService                             = mock[SaveService]

  override protected val additionalBindings: List[GuiceableModule] = List(
    bind[ClaimsValidationConnector].toInstance(mockClaimsValidationConnector),
    bind[ClaimsValidationService].toInstance(mockClaimsValidationService),
    bind[ClaimsService].toInstance(mockClaimsService),
    bind[SaveService].toInstance(mockSaveService)
  )

  val testClaimId: String                          = "test-claim-id-123"
  val testFileUploadReference: FileUploadReference = FileUploadReference("test-file-upload-ref")

  lazy val testValidatedJsonString: String =
    TestResources.readTestResource("/test-get-upload-result-validated-connected-charities.json")

  lazy val testValidatedResponse: GetUploadResultValidatedConnectedCharities =
    Json.parse(testValidatedJsonString).as[GetUploadResultValidatedConnectedCharities]

  def validSessionData: SessionData = completeGasdsSession
    .copy(
      unsubmittedClaimId = Some(testClaimId),
      connectedCharitiesScheduleFileUploadReference = Some(testFileUploadReference)
    )

  def validSessionDataCompleted: SessionData = validSessionData.copy(connectedCharitiesScheduleCompleted = true)

  def sessionDataFailingGuard: SessionData = RepaymentClaimDetailsAnswers
    .setClaimingUnderGiftAidSmallDonationsScheme(true)
    .and(RepaymentClaimDetailsAnswers.setConnectedToAnyOtherCharities(false))
    .and(SessionData.setUnsubmittedClaimId(testClaimId))

  "CheckYourConnectedCharitiesScheduleController" - {

    "onPageLoad" - {
      "should render ClaimCompleteController if submissionReference is defined" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          lastUpdatedReference = Some(testCharitiesReference),
          submissionReference = Some(testCharitiesReference)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CheckYourConnectedCharitiesScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }
      "should redirect to ClaimsTaskListController when data guard is triggered" in {
        given application: Application = applicationBuilder(sessionData = sessionDataFailingGuard).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CheckYourConnectedCharitiesScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should render the page when schedule data is available - organisation user" in {
        when(mockClaimsValidationService.getConnectedCharitiesScheduleData(using any(), any()))
          .thenReturn(Future.successful(testValidatedResponse.connectedCharitiesData))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CheckYourConnectedCharitiesScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result)  should include(messages("checkYourConnectedCharitiesSchedule.title"))
          (contentAsString(result) should not).include("checkYourConnectedCharitiesSchedule.title.agent")
          contentAsString(result)  should include(messages("checkYourConnectedCharitiesSchedule.heading"))
          (contentAsString(result) should not).include("checkYourConnectedCharitiesSchedule.heading.agent")
        }
      }

      "should render the page when schedule data is available - agent user" in {
        when(mockClaimsValidationService.getConnectedCharitiesScheduleData(using any(), any()))
          .thenReturn(Future.successful(testValidatedResponse.connectedCharitiesData))

        given application: Application =
          applicationBuilder(sessionData = validSessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CheckYourConnectedCharitiesScheduleController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual OK
          contentAsString(result)  should include(messages("checkYourConnectedCharitiesSchedule.title.agent"))
          (contentAsString(result) should not).include("checkYourConnectedCharitiesSchedule.title")
          contentAsString(result)  should include(messages("checkYourConnectedCharitiesSchedule.heading.agent"))
          (contentAsString(result) should not).include("checkYourConnectedCharitiesSchedule.heading")
        }
      }

      "should display connected charities data in the table" in {
        when(mockClaimsValidationService.getConnectedCharitiesScheduleData(using any(), any()))
          .thenReturn(Future.successful(testValidatedResponse.connectedCharitiesData))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CheckYourConnectedCharitiesScheduleController.onPageLoad.url)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("Charity of the 501st Legion")
          content should include("CW501")
        }
      }

      "should default to page 1 when page query parameter is not a valid integer" in {
        when(mockClaimsValidationService.getConnectedCharitiesScheduleData(using any(), any()))
          .thenReturn(Future.successful(testValidatedResponse.connectedCharitiesData))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CheckYourConnectedCharitiesScheduleController.onPageLoad.url + "?page=invalid")

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual OK
          content should include("Charity of the 501st Legion")
        }
      }
    }

    "onSubmit" - {
      "should render ClaimCompleteController if submissionReference is defined" in {
        val sessionData = SessionData(
          charitiesReference = testCharitiesReference,
          lastUpdatedReference = Some(testCharitiesReference),
          submissionReference = Some(testCharitiesReference)
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.CheckYourConnectedCharitiesScheduleController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }

      "should return BadRequest when form onSubmit has errors and display error message - organisation user" in {
        when(mockClaimsValidationService.getConnectedCharitiesScheduleData(using any(), any()))
          .thenReturn(Future.successful(testValidatedResponse.connectedCharitiesData))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CheckYourConnectedCharitiesScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("other" -> "field")

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual BAD_REQUEST
          contentAsString(result)  should include(messages("checkYourConnectedCharitiesSchedule.title"))
          (contentAsString(result) should not).include("checkYourConnectedCharitiesSchedule.title.agent")
          contentAsString(result)  should include(messages("checkYourConnectedCharitiesSchedule.heading"))
          (contentAsString(result) should not).include("checkYourConnectedCharitiesSchedule.heading.agent")
          content                  should include("Select ‘Yes’ if you need to update this Connected Charities schedule")
        }
      }

      "should return BadRequest when form onSubmit has errors and display error message - agent user" in {
        when(mockClaimsValidationService.getConnectedCharitiesScheduleData(using any(), any()))
          .thenReturn(Future.successful(testValidatedResponse.connectedCharitiesData))

        given application: Application =
          applicationBuilder(sessionData = validSessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CheckYourConnectedCharitiesScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("other" -> "field")

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) shouldEqual BAD_REQUEST
          contentAsString(result)  should include(messages("checkYourConnectedCharitiesSchedule.title.agent"))
          (contentAsString(result) should not).include("checkYourConnectedCharitiesSchedule.title")
          contentAsString(result)  should include(messages("checkYourConnectedCharitiesSchedule.heading.agent"))
          (contentAsString(result) should not).include("checkYourConnectedCharitiesSchedule.heading")
          content                  should include("Select ‘Yes’ if you need to update this Connected Charities schedule")
        }
      }

      "should redirect to update screen when Yes is selected" in {
        when(mockClaimsValidationService.getConnectedCharitiesScheduleData(using any(), any()))
          .thenReturn(Future.successful(testValidatedResponse.connectedCharitiesData))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CheckYourConnectedCharitiesScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.UpdateConnectedCharitiesScheduleController.onPageLoad.url
          )
        }
      }

      "should redirect to Task List when No is selected and schedule is already completed previously" in {
        when(mockClaimsValidationService.getConnectedCharitiesScheduleData(using any(), any()))
          .thenReturn(Future.successful(testValidatedResponse.connectedCharitiesData))

        given application: Application = applicationBuilder(sessionData = validSessionDataCompleted).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CheckYourConnectedCharitiesScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should save and redirect to UploadSuccessful when No is selected and schedule not yet completed before" in {
        when(mockClaimsValidationService.getConnectedCharitiesScheduleData(using any(), any()))
          .thenReturn(Future.successful(testValidatedResponse.connectedCharitiesData))

        when(mockSaveService.save(any())(using any())).thenReturn(Future.successful(()))

        when(mockClaimsService.save(using any())).thenReturn(Future.successful(()))

        given application: Application = applicationBuilder(sessionData = validSessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.CheckYourConnectedCharitiesScheduleController.onSubmit.url)
              .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(
            routes.ConnectedCharitiesScheduleUploadSuccessfulController.onPageLoad.url
          )
        }
      }
    }
  }
}

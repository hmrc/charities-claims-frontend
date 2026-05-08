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

package controllers.giftAidSmallDonationsScheme

import controllers.ControllerSpec
import models.Mode.{CheckMode, NormalMode}
import models.{RepaymentClaimDetailsAnswers, SessionData}
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup
import views.html.GasdsAdjustmentAmountCheckYourAnswersView

class GasdsAdjustmentAmountCheckYourAnswersControllerSpec extends ControllerSpec {

  val sessionData: SessionData = SessionData(
    charitiesReference = testCharitiesReference,
    unsubmittedClaimId = Some("test-claim-id"),
    repaymentClaimDetailsAnswers = Some(
      RepaymentClaimDetailsAnswers(
        claimingGiftAid = Some(false),
        claimingTaxDeducted = Some(false),
        claimingUnderGiftAidSmallDonationsScheme = Some(true),
        claimingDonationsNotFromCommunityBuilding = Some(true),
        claimingDonationsCollectedInCommunityBuildings = Some(false),
        makingAdjustmentToPreviousClaim = Some(true),
        connectedToAnyOtherCharities = Some(false),
        claimingReferenceNumber = Some(false)
      )
    )
  )

  "GasdsAdjustmentAmountCheckYourAnswersController" - {
    "onPageLoad" - {
      "organisation user" - {
        val isAgent = false
        "should render ClaimCompleteController if submissionReference is defined" in {
          val sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            lastUpdatedReference = Some(testCharitiesReference),
            submissionReference = Some(testCharitiesReference)
          )

          given application: Application = applicationBuilder(sessionData = sessionData).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.GasdsAdjustmentAmountCheckYourAnswersController.onPageLoad(NormalMode).url)

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
            )
          }
        }
        "should render the page correctly when the guard condition is met" in {
          given application: Application = applicationBuilder(sessionData = sessionData).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.GasdsAdjustmentAmountCheckYourAnswersController.onPageLoad(NormalMode).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[GasdsAdjustmentAmountCheckYourAnswersView]

            status(result) shouldEqual OK

            contentAsString(result) shouldEqual
              view(sessionData.giftAidSmallDonationsSchemeDonationDetailsAnswers, NormalMode, isAgent).body
            contentAsString(result)  should include(messages("gasdsAdjustmentAmountCheckYourAnswers.heading"))
            contentAsString(result)  should include(messages("gasdsAdjustmentAmountCheckYourAnswers.title"))
            (contentAsString(result) should not).include(
              messages("agasdsAdjustmentAmountCheckYourAnswers.heading.agent")
            )
            (contentAsString(result) should not).include(messages("agasdsAdjustmentAmountCheckYourAnswers.title.agent"))
          }
        }

        "should redirect to task list page when makingAdjustmentToPreviousClaim is false" in {
          given application: Application = applicationBuilder(sessionData =
            sessionData.copy(
              repaymentClaimDetailsAnswers = completeGasdsSession.repaymentClaimDetailsAnswers.map(
                _.copy(makingAdjustmentToPreviousClaim = Some(false))
              )
            )
          ).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.GasdsAdjustmentAmountCheckYourAnswersController.onPageLoad(NormalMode).url)

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result).value shouldEqual controllers.routes.ClaimsTaskListController.onPageLoad.url
          }
        }

        "should redirect to task list page when repaymentClaimsDetails is incomplete" in {
          given application: Application = applicationBuilder(sessionData =
            sessionData.copy(
              repaymentClaimDetailsAnswers = None
            )
          ).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.GasdsAdjustmentAmountCheckYourAnswersController.onPageLoad(NormalMode).url)

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result).value shouldEqual controllers.routes.ClaimsTaskListController.onPageLoad.url
          }
        }
      }
      "agent user" - {
        val isAgent = true
        "should render ClaimCompleteController if submissionReference is defined" in {
          val sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            lastUpdatedReference = Some(testCharitiesReference),
            submissionReference = Some(testCharitiesReference)
          )

          given application: Application =
            applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.GasdsAdjustmentAmountCheckYourAnswersController.onPageLoad(NormalMode).url)

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
            )
          }
        }
        "should render the page correctly when the guard condition is met" in {
          given application: Application =
            applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.GasdsAdjustmentAmountCheckYourAnswersController.onPageLoad(NormalMode).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[GasdsAdjustmentAmountCheckYourAnswersView]

            status(result) shouldEqual OK

            contentAsString(result) shouldEqual
              view(sessionData.giftAidSmallDonationsSchemeDonationDetailsAnswers, NormalMode, isAgent).body
            contentAsString(result)  should include(messages("gasdsAdjustmentAmountCheckYourAnswers.heading.agent"))
            contentAsString(result)  should include(messages("gasdsAdjustmentAmountCheckYourAnswers.title.agent"))
            (contentAsString(result) should not).include(messages("agasdsAdjustmentAmountCheckYourAnswers.heading"))
            (contentAsString(result) should not).include(messages("agasdsAdjustmentAmountCheckYourAnswers.title"))
          }
        }

        "should redirect to task list page when makingAdjustmentToPreviousClaim is false" in {
          given application: Application = applicationBuilder(
            sessionData = sessionData.copy(
              repaymentClaimDetailsAnswers = completeGasdsSession.repaymentClaimDetailsAnswers.map(
                _.copy(makingAdjustmentToPreviousClaim = Some(false))
              )
            ),
            affinityGroup = AffinityGroup.Agent
          ).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.GasdsAdjustmentAmountCheckYourAnswersController.onPageLoad(NormalMode).url)

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result).value shouldEqual controllers.routes.ClaimsTaskListController.onPageLoad.url
          }
        }

        "should redirect to task list page when repaymentClaimsDetails is incomplete" in {
          given application: Application = applicationBuilder(
            sessionData = sessionData.copy(
              repaymentClaimDetailsAnswers = None
            ),
            affinityGroup = AffinityGroup.Agent
          ).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.GasdsAdjustmentAmountCheckYourAnswersController.onPageLoad(NormalMode).url)

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result).value shouldEqual controllers.routes.ClaimsTaskListController.onPageLoad.url
          }
        }
      }
    }

    "onSubmit" - {
      "organisation user" - {
        "should render ClaimCompleteController if submissionReference is defined" in {
          val sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            lastUpdatedReference = Some(testCharitiesReference),
            submissionReference = Some(testCharitiesReference)
          )

          given application: Application = applicationBuilder(sessionData = sessionData).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(POST, routes.GasdsAdjustmentAmountCheckYourAnswersController.onSubmit(NormalMode).url)

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
            )
          }
        }
        "redirect to WhichTaxYearAreYouClaimingFor when claiming top-up under GASDS" in {
          given application: Application = applicationBuilder(sessionData =
            sessionData.copy(
              repaymentClaimDetailsAnswers = sessionData.repaymentClaimDetailsAnswers.map(
                _.copy(claimingDonationsNotFromCommunityBuilding = Some(true))
              )
            )
          ).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(POST, routes.GasdsAdjustmentAmountCheckYourAnswersController.onSubmit(NormalMode).url)

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result).value shouldEqual
              routes.WhichTaxYearAreYouClaimingForController.onPageLoad(1, NormalMode).url
          }
        }

        "redirect to GASDS donation details check page when not claiming top up under GASDS" in {
          given application: Application = applicationBuilder(sessionData =
            sessionData.copy(
              repaymentClaimDetailsAnswers = sessionData.repaymentClaimDetailsAnswers.map(
                _.copy(claimingDonationsNotFromCommunityBuilding = Some(false))
              )
            )
          ).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(POST, routes.GasdsAdjustmentAmountCheckYourAnswersController.onSubmit(NormalMode).url)

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result).value shouldEqual
              routes.GiftAidSmallDonationsSchemeDetailsCheckYourAnswersController.onPageLoad.url
          }
        }

        "redirect to GASDS details check page in CheckMode" in {
          given application: Application = applicationBuilder(sessionData = sessionData).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(
                POST,
                routes.GasdsAdjustmentAmountCheckYourAnswersController.onSubmit(CheckMode).url
              )

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result).value shouldEqual
              routes.GiftAidSmallDonationsSchemeDetailsCheckYourAnswersController.onPageLoad.url
          }
        }
      }
      "agent user" - {
        "should render ClaimCompleteController if submissionReference is defined" in {
          val sessionData = SessionData(
            charitiesReference = testCharitiesReference,
            lastUpdatedReference = Some(testCharitiesReference),
            submissionReference = Some(testCharitiesReference)
          )

          given application: Application =
            applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(POST, routes.GasdsAdjustmentAmountCheckYourAnswersController.onSubmit(NormalMode).url)

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result) shouldEqual Some(
              controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
            )
          }
        }
        "redirect to WhichTaxYearAreYouClaimingFor when claiming top-up under GASDS" in {
          given application: Application = applicationBuilder(
            sessionData = sessionData.copy(
              repaymentClaimDetailsAnswers = sessionData.repaymentClaimDetailsAnswers.map(
                _.copy(claimingDonationsNotFromCommunityBuilding = Some(true))
              )
            ),
            affinityGroup = AffinityGroup.Agent
          ).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(POST, routes.GasdsAdjustmentAmountCheckYourAnswersController.onSubmit(NormalMode).url)

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result).value shouldEqual
              routes.WhichTaxYearAreYouClaimingForController.onPageLoad(1, NormalMode).url
          }
        }

        "redirect to GASDS donation details check page when not claiming top up under GASDS" in {
          given application: Application = applicationBuilder(
            sessionData = sessionData.copy(
              repaymentClaimDetailsAnswers = sessionData.repaymentClaimDetailsAnswers.map(
                _.copy(claimingDonationsNotFromCommunityBuilding = Some(false))
              )
            ),
            affinityGroup = AffinityGroup.Agent
          ).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(POST, routes.GasdsAdjustmentAmountCheckYourAnswersController.onSubmit(NormalMode).url)

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result).value shouldEqual
              routes.GiftAidSmallDonationsSchemeDetailsCheckYourAnswersController.onPageLoad.url
          }
        }

        "redirect to GASDS details check page in CheckMode" in {
          given application: Application =
            applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(
                POST,
                routes.GasdsAdjustmentAmountCheckYourAnswersController.onSubmit(CheckMode).url
              )

            val result = route(application, request).value

            status(result) shouldEqual SEE_OTHER
            redirectLocation(result).value shouldEqual
              routes.GiftAidSmallDonationsSchemeDetailsCheckYourAnswersController.onPageLoad.url
          }
        }
      }
    }
  }
}

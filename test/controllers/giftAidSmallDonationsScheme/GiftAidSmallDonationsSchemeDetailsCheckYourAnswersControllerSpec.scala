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
import models.*
import org.scalamock.scalatest.MockFactory
import play.api.Application
import play.api.inject.bind
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import services.ClaimsService
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier
import views.html.GiftAidSmallDonationsSchemeDetailsCheckYourAnswersView

import scala.concurrent.Future

class GiftAidSmallDonationsSchemeDetailsCheckYourAnswersControllerSpec extends ControllerSpec with MockFactory {

  private val validGasdsAnswers =
    GiftAidSmallDonationsSchemeDonationDetailsAnswers(
      adjustmentForGiftAidOverClaimed = Some(1000.00),
      claims = Some(
        Seq(
          Some(
            GiftAidSmallDonationsSchemeClaimAnswers(
              taxYear = 2025,
              amountOfDonationsReceived = Some(BigDecimal(1000.00))
            )
          )
        )
      )
    )

  private def repaymentAnswers(
    adjustment: Option[Boolean],
    donations: Option[Boolean],
    claimingUnderGasds: Option[Boolean] = Some(true)
  ): RepaymentClaimDetailsAnswers =
    completeGasdsAnswers.copy(
      claimingUnderGiftAidSmallDonationsScheme = claimingUnderGasds,
      makingAdjustmentToPreviousClaim = adjustment,
      claimingDonationsNotFromCommunityBuilding = donations
    )

  private def getRequest =
    FakeRequest(
      GET,
      routes.GiftAidSmallDonationsSchemeDetailsCheckYourAnswersController.onPageLoad.url
    )

  private def postRequest =
    FakeRequest(
      POST,
      routes.GiftAidSmallDonationsSchemeDetailsCheckYourAnswersController.onSubmit.url
    )

  "GiftAidSmallDonationsSchemeDetailsCheckYourAnswersController" - {

    "onPageLoad" - {

      "should redirect to ClaimCompleteController when submissionReference exists" in {

        val sessionData =
          SessionData(
            charitiesReference = testCharitiesReference,
            lastUpdatedReference = Some(testCharitiesReference),
            submissionReference = Some(testCharitiesReference)
          )

        given application: Application =
          applicationBuilder(sessionData = sessionData).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            getRequest

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result) shouldEqual Some(
            controllers.claimDeclaration.routes.ClaimCompleteController.onPageLoad.url
          )
        }
      }

      "should render both summary lists when both flags are true" in {

        val sessionData =
          completeGasdsSession.copy(
            repaymentClaimDetailsAnswers = Some(repaymentAnswers(adjustment = Some(true), donations = Some(true))),
            giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(validGasdsAnswers)
          )

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            getRequest

          val result = route(application, request).value

          val view =
            application.injector
              .instanceOf[
                GiftAidSmallDonationsSchemeDetailsCheckYourAnswersView
              ]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual
            view(
              oSummaryListForAdjustmentToGiftAidOverclaimed = Some(
                viewmodels.GiftAidSmallDonationsSchemeDonationDetailsAnswersHelper
                  .buildSummaryListForAdjustmentToGiftAidOverclaimed(
                    sessionData.giftAidSmallDonationsSchemeDonationDetailsAnswers
                  )
              ),
              oSummaryListForNumberOfTaxYearsAdded = Some(
                viewmodels.GiftAidSmallDonationsSchemeDonationDetailsAnswersHelper
                  .buildSummaryListForNumberOfTaxYearsAdded(
                    sessionData.giftAidSmallDonationsSchemeDonationDetailsAnswers
                  )
              ),
              isAgent = true
            ).body
        }
      }

      "should render only adjustment summary list when adjustment=true and donations=false" in {

        val sessionData =
          completeGasdsSession.copy(
            repaymentClaimDetailsAnswers = Some(repaymentAnswers(adjustment = Some(true), donations = Some(false))),
            giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(validGasdsAnswers)
          )

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            getRequest

          val result = route(application, request).value

          val view =
            application.injector
              .instanceOf[
                GiftAidSmallDonationsSchemeDetailsCheckYourAnswersView
              ]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual
            view(
              oSummaryListForAdjustmentToGiftAidOverclaimed = Some(
                viewmodels.GiftAidSmallDonationsSchemeDonationDetailsAnswersHelper
                  .buildSummaryListForAdjustmentToGiftAidOverclaimed(
                    sessionData.giftAidSmallDonationsSchemeDonationDetailsAnswers
                  )
              ),
              None,
              isAgent = true
            ).body
        }
      }

      "should render only tax years summary list when adjustment=false and donations=true" in {

        val sessionData =
          completeGasdsSession.copy(
            repaymentClaimDetailsAnswers = Some(repaymentAnswers(adjustment = Some(false), donations = Some(true))),
            giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(validGasdsAnswers)
          )

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            getRequest

          val result = route(application, request).value

          val view =
            application.injector
              .instanceOf[
                GiftAidSmallDonationsSchemeDetailsCheckYourAnswersView
              ]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual
            view(
              None,
              oSummaryListForNumberOfTaxYearsAdded = Some(
                viewmodels.GiftAidSmallDonationsSchemeDonationDetailsAnswersHelper
                  .buildSummaryListForNumberOfTaxYearsAdded(
                    sessionData.giftAidSmallDonationsSchemeDonationDetailsAnswers
                  )
              ),
              isAgent = true
            ).body
        }
      }

      "should render no summary lists when both flags are false" in {

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(
            repaymentClaimDetailsAnswers = Some(repaymentAnswers(adjustment = Some(false), donations = Some(false))),
            giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(validGasdsAnswers)
          )

        given application: Application =
          applicationBuilder(sessionData = sessionData, affinityGroup = AffinityGroup.Agent).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            getRequest

          val result = route(application, request).value

          val view =
            application.injector
              .instanceOf[
                GiftAidSmallDonationsSchemeDetailsCheckYourAnswersView
              ]

          contentAsString(result) shouldEqual
            view(
              None,
              None,
              isAgent = true
            ).body
        }
      }

      "should redirect when claimingUnderGiftAidSmallDonationsScheme=false" in {

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(
            repaymentClaimDetailsAnswers = Some(
              repaymentAnswers(
                adjustment = Some(false),
                donations = Some(false),
                claimingUnderGasds = Some(false)
              )
            )
          )

        given application: Application =
          applicationBuilder(sessionData = sessionData).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            getRequest

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
        }
      }
    }

    "onSubmit" - {

      "should save and redirect to ClaimsTaskListController when answers are complete" in {

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(
            repaymentClaimDetailsAnswers = Some(repaymentAnswers(adjustment = Some(true), donations = Some(true))),
            giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(validGasdsAnswers)
          )

        val mockClaimsService =
          mock[ClaimsService]

        (mockClaimsService
          .save(using _: HeaderCarrier))
          .expects(*)
          .returning(Future.successful(()))

        given application: Application =
          applicationBuilder(sessionData = sessionData)
            .overrides(
              bind[ClaimsService]
                .toInstance(mockClaimsService)
            )
            .build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            postRequest

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result) shouldEqual Some(
            controllers.routes.ClaimsTaskListController.onPageLoad.url
          )
        }
      }

      "should redirect to incomplete answers page when answers are incomplete" in {

        val incompleteAnswers =
          GiftAidSmallDonationsSchemeDonationDetailsAnswers()

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(
            repaymentClaimDetailsAnswers = Some(repaymentAnswers(adjustment = Some(true), donations = Some(true))),
            giftAidSmallDonationsSchemeDonationDetailsAnswers = Some(incompleteAnswers)
          )

        given application: Application =
          applicationBuilder(sessionData = sessionData).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            postRequest

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER

          redirectLocation(result) shouldEqual Some(
            routes.GasdsDonationDetailsIncompleteAnswersController.onPageLoad.url
          )
        }
      }

      "should redirect when claimingUnderGiftAidSmallDonationsScheme=false" in {

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(
            repaymentClaimDetailsAnswers = Some(
              repaymentAnswers(
                adjustment = Some(false),
                donations = Some(false),
                claimingUnderGasds = Some(false)
              )
            )
          )

        given application: Application =
          applicationBuilder(sessionData = sessionData).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            postRequest

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
        }
      }
    }
  }
}

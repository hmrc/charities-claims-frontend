package controllers.giftAidSmallDonationsScheme

import controllers.ControllerSpec
import models.*
import org.scalamock.scalatest.MockFactory
import play.api.Application
import play.api.inject.bind
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import services.ClaimsService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.GiftAidSmallDonationsSchemeDetailsCheckYourAnswersView

import scala.concurrent.Future

class GiftAidSmallDonationsSchemeDetailsCheckYourAnswersControllerSpec
  extends ControllerSpec
    with MockFactory {

  private val validGasdsAnswers =
    GiftAidSmallDonationsSchemeDonationDetailsAnswers(
      adjustmentForGiftAidOverClaimed = Some(BigDecimal(100)),
      claims = Some(
        Seq(
          Some(
            GiftAidSmallDonationsSchemeClaimAnswers(
              taxYear = 1,
              amountOfDonationsReceived = Some(BigDecimal(500))
            )
          )
        )
      )
    )

  private def repaymentAnswers(
                                adjustment: Boolean,
                                donations: Boolean,
                                claimingUnderGasds: Boolean = true
                              ): RepaymentClaimDetailsAnswers =
    RepaymentClaimDetailsAnswers(
      claimingUnderGiftAidSmallDonationsScheme =
        Some(claimingUnderGasds),
      makingAdjustmentToPreviousClaim =
        Some(adjustment),
      claimingDonationsNotFromCommunityBuilding =
        Some(donations)
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
          completeRepaymentDetailsAnswersSession.copy(
            repaymentClaimDetailsAnswers =
              Some(repaymentAnswers(adjustment = true, donations = true)),
            giftAidSmallDonationsSchemeDonationDetailsAnswers =
              Some(validGasdsAnswers)
          )

        given application: Application =
          applicationBuilder(sessionData = sessionData).build()

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
              oSummaryListForAdjustmentToGiftAidOverclaimed =
                Some(
                  viewmodels.GiftAidSmallDonationsSchemeDonationDetailsAnswersHelper
                    .buildSummaryListForAdjustmentToGiftAidOverclaimed(
                      Some(validGasdsAnswers)
                    )
                ),
              oSummaryListForNumberOfTaxYearsAdded =
                Some(
                  viewmodels.GiftAidSmallDonationsSchemeDonationDetailsAnswersHelper
                    .buildSummaryListForNumberOfTaxYearsAdded(
                      Some(validGasdsAnswers)
                    )
                )
            ).body
        }
      }

      "should render only adjustment summary list when adjustment=true and donations=false" in {

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(
            repaymentClaimDetailsAnswers =
              Some(repaymentAnswers(adjustment = true, donations = false)),
            giftAidSmallDonationsSchemeDonationDetailsAnswers =
              Some(validGasdsAnswers)
          )

        given application: Application =
          applicationBuilder(sessionData = sessionData).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            getRequest

          val result = route(application, request).value

          status(result) shouldEqual OK

          contentAsString(result) should include(
            "giftAidSmallDonationsScheme"
          )
        }
      }

      "should render only tax years summary list when adjustment=false and donations=true" in {

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(
            repaymentClaimDetailsAnswers =
              Some(repaymentAnswers(adjustment = false, donations = true)),
            giftAidSmallDonationsSchemeDonationDetailsAnswers =
              Some(validGasdsAnswers)
          )

        given application: Application =
          applicationBuilder(sessionData = sessionData).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            getRequest

          val result = route(application, request).value

          status(result) shouldEqual OK

          contentAsString(result) should include(
            "giftAidSmallDonationsScheme"
          )
        }
      }

      "should render no summary lists when both flags are false" in {

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(
            repaymentClaimDetailsAnswers =
              Some(repaymentAnswers(adjustment = false, donations = false)),
            giftAidSmallDonationsSchemeDonationDetailsAnswers =
              Some(validGasdsAnswers)
          )

        given application: Application =
          applicationBuilder(sessionData = sessionData).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            getRequest

          val result = route(application, request).value

          status(result) shouldEqual OK
        }
      }

      "should render successfully when repayment claim flags are missing" in {

        val repaymentAnswers =
          RepaymentClaimDetailsAnswers(
            claimingUnderGiftAidSmallDonationsScheme = Some(true)
          )

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(
            repaymentClaimDetailsAnswers =
              Some(repaymentAnswers)
          )

        given application: Application =
          applicationBuilder(sessionData = sessionData).build()

        running(application) {

          given request: FakeRequest[AnyContentAsEmpty.type] =
            getRequest

          val result = route(application, request).value

          status(result) shouldEqual OK
        }
      }

      "should redirect when claimingUnderGiftAidSmallDonationsScheme=false" in {

        val sessionData =
          completeRepaymentDetailsAnswersSession.copy(
            repaymentClaimDetailsAnswers =
              Some(
                repaymentAnswers(
                  adjustment = false,
                  donations = false,
                  claimingUnderGasds = false
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
            repaymentClaimDetailsAnswers =
              Some(repaymentAnswers(adjustment = true, donations = true)),
            giftAidSmallDonationsSchemeDonationDetailsAnswers =
              Some(validGasdsAnswers)
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
            repaymentClaimDetailsAnswers =
              Some(repaymentAnswers(adjustment = true, donations = true)),
            giftAidSmallDonationsSchemeDonationDetailsAnswers =
              Some(incompleteAnswers)
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
            repaymentClaimDetailsAnswers =
              Some(
                repaymentAnswers(
                  adjustment = false,
                  donations = false,
                  claimingUnderGasds = false
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
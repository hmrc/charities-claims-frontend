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

package controllers.repaymentClaimDetails

import play.api.test.FakeRequest
import play.api.mvc.AnyContentAsEmpty
import controllers.ControllerSpec
import views.html.RepaymentClaimDetailsCheckYourAnswersView
import play.api.Application
import play.api.test.Helpers.*
import models.RepaymentClaimDetailsAnswers
import models.*

class RepaymentClaimDetailsCheckYourAnswersControllerSpec extends ControllerSpec {
  val repaymentClaimDetailsDefaultAnswersOld = RepaymentClaimDetailsAnswersOld(
    claimingGiftAid = Some(true),
    claimingTaxDeducted = Some(false),
    claimingUnderGiftAidSmallDonationsScheme = Some(true),
    claimingReferenceNumber = Some(true),
    claimReferenceNumber = Some("12345678AB")
  )

  "RepaymentClaimDetailsCheckYourAnswersController" - {
    "onPageLoad" - {
      "claimingUnderGiftAidSmallDonationsScheme is true" - {
        "should render the page correctly when claimingUnderGiftAidSmallDonationsScheme is true" in {

          val sessionData = SessionData(
            repaymentClaimDetailsAnswersOld = repaymentClaimDetailsDefaultAnswersOld,
            repaymentClaimDetailsAnswers = Some(
              RepaymentClaimDetailsAnswers(
                claimingGiftAid = Some(false),
                claimingTaxDeducted = Some(false),
                claimingUnderGiftAidSmallDonationsScheme = Some(true),
                claimingDonationsNotFromCommunityBuilding = Some(true),
                claimingDonationsCollectedInCommunityBuildings = Some(true),
                connectedToAnyOtherCharities = Some(true),
                makingAdjustmentToPreviousClaim = Some(true),
                claimingReferenceNumber = Some(true),
                claimReferenceNumber = Some("12345678AB")
              )
            )
          )

          given application: Application = applicationBuilder(sessionData = sessionData).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[RepaymentClaimDetailsCheckYourAnswersView]

            status(result) shouldEqual OK

            contentAsString(result) shouldEqual view(sessionData.repaymentClaimDetailsAnswers).body
          }
        }

        "should render the page correctly when claimingGiftAid is true" in {

          val sessionData = SessionData(
            repaymentClaimDetailsAnswersOld = repaymentClaimDetailsDefaultAnswersOld,
            repaymentClaimDetailsAnswers = Some(
              RepaymentClaimDetailsAnswers(
                claimingGiftAid = Some(true),
                claimingTaxDeducted = Some(false),
                claimingUnderGiftAidSmallDonationsScheme = Some(true),
                claimingDonationsNotFromCommunityBuilding = Some(false),
                claimingDonationsCollectedInCommunityBuildings = Some(false),
                connectedToAnyOtherCharities = Some(false),
                makingAdjustmentToPreviousClaim = Some(false),
                claimingReferenceNumber = Some(true),
                claimReferenceNumber = Some("12345678AB")
              )
            )
          )

          given application: Application = applicationBuilder(sessionData = sessionData).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[RepaymentClaimDetailsCheckYourAnswersView]

            status(result) shouldEqual OK

            contentAsString(result) shouldEqual view(sessionData.repaymentClaimDetailsAnswers).body
          }
        }
        "should render the page correctly when claimingTaxDeducted is true " in {

          val sessionData = SessionData(
            repaymentClaimDetailsAnswersOld = repaymentClaimDetailsDefaultAnswersOld,
            repaymentClaimDetailsAnswers = Some(
              RepaymentClaimDetailsAnswers(
                claimingGiftAid = Some(false),
                claimingTaxDeducted = Some(true),
                claimingUnderGiftAidSmallDonationsScheme = Some(true),
                claimingDonationsNotFromCommunityBuilding = Some(false),
                claimingDonationsCollectedInCommunityBuildings = Some(false),
                connectedToAnyOtherCharities = Some(false),
                makingAdjustmentToPreviousClaim = Some(false),
                claimingReferenceNumber = Some(true),
                claimReferenceNumber = Some("12345678AB")
              )
            )
          )

          given application: Application = applicationBuilder(sessionData = sessionData).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[RepaymentClaimDetailsCheckYourAnswersView]

            status(result) shouldEqual OK

            contentAsString(result) shouldEqual view(sessionData.repaymentClaimDetailsAnswers).body
          }
        }

        "should render the page correctly when claimingGiftAid & claimingTaxDeducted are true" in {

          val sessionData = SessionData(
            repaymentClaimDetailsAnswersOld = repaymentClaimDetailsDefaultAnswersOld,
            repaymentClaimDetailsAnswers = Some(
              RepaymentClaimDetailsAnswers(
                claimingGiftAid = Some(true),
                claimingTaxDeducted = Some(true),
                claimingUnderGiftAidSmallDonationsScheme = Some(true),
                claimingDonationsNotFromCommunityBuilding = Some(false),
                claimingDonationsCollectedInCommunityBuildings = Some(false),
                connectedToAnyOtherCharities = Some(false),
                makingAdjustmentToPreviousClaim = Some(false),
                claimingReferenceNumber = Some(true),
                claimReferenceNumber = Some("12345678AB")
              )
            )
          )

          given application: Application = applicationBuilder(sessionData = sessionData).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[RepaymentClaimDetailsCheckYourAnswersView]

            status(result) shouldEqual OK

            contentAsString(result) shouldEqual view(sessionData.repaymentClaimDetailsAnswers).body
          }
        }

        "should render the page correctly when claimingGiftAid & claimingTaxDeducted are true and some answers is not defined" in {

          val sessionData = SessionData(
            repaymentClaimDetailsAnswersOld = repaymentClaimDetailsDefaultAnswersOld,
            repaymentClaimDetailsAnswers = Some(
              RepaymentClaimDetailsAnswers(
                claimingGiftAid = Some(true),
                claimingTaxDeducted = Some(true),
                claimingUnderGiftAidSmallDonationsScheme = Some(true),
                claimingDonationsNotFromCommunityBuilding = None,
                claimingDonationsCollectedInCommunityBuildings = None,
                connectedToAnyOtherCharities = None,
                makingAdjustmentToPreviousClaim = None,
                claimingReferenceNumber = Some(true),
                claimReferenceNumber = Some("12345678AB")
              )
            )
          )

          given application: Application = applicationBuilder(sessionData = sessionData).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[RepaymentClaimDetailsCheckYourAnswersView]

            status(result) shouldEqual OK

            contentAsString(result) shouldEqual view(sessionData.repaymentClaimDetailsAnswers).body
          }
        }

        "should render the page correctly when makingAdjustmentToPreviousClaim are false and some answers is not defined" in {

          val sessionData = SessionData(
            repaymentClaimDetailsAnswersOld = repaymentClaimDetailsDefaultAnswersOld,
            repaymentClaimDetailsAnswers = Some(
              RepaymentClaimDetailsAnswers(
                claimingGiftAid = Some(true),
                claimingTaxDeducted = Some(true),
                claimingUnderGiftAidSmallDonationsScheme = Some(true),
                claimingDonationsNotFromCommunityBuilding = None,
                claimingDonationsCollectedInCommunityBuildings = None,
                connectedToAnyOtherCharities = None,
                makingAdjustmentToPreviousClaim = Some(false),
                claimingReferenceNumber = Some(true),
                claimReferenceNumber = Some("12345678AB")
              )
            )
          )

          given application: Application = applicationBuilder(sessionData = sessionData).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[RepaymentClaimDetailsCheckYourAnswersView]

            status(result) shouldEqual OK

            contentAsString(result) shouldEqual view(sessionData.repaymentClaimDetailsAnswers).body
          }
        }

      }
      "claimingUnderGiftAidSmallDonationsScheme is false" - {
        "should render the page correctly when claimingGiftAid is true " in {

          val sessionData = SessionData(
            repaymentClaimDetailsAnswersOld = repaymentClaimDetailsDefaultAnswersOld,
            repaymentClaimDetailsAnswers = Some(
              RepaymentClaimDetailsAnswers(
                claimingGiftAid = Some(true),
                claimingTaxDeducted = Some(false),
                claimingUnderGiftAidSmallDonationsScheme = Some(false),
                claimingDonationsNotFromCommunityBuilding = Some(false),
                claimingDonationsCollectedInCommunityBuildings = Some(false),
                connectedToAnyOtherCharities = Some(false),
                makingAdjustmentToPreviousClaim = Some(false),
                claimingReferenceNumber = Some(false),
                claimReferenceNumber = None
              )
            )
          )

          given application: Application = applicationBuilder(sessionData = sessionData).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[RepaymentClaimDetailsCheckYourAnswersView]

            status(result) shouldEqual OK

            contentAsString(result) shouldEqual view(sessionData.repaymentClaimDetailsAnswers).body
          }
        }
        "should render the page correctly when claimingGiftAid & claimingTaxDeducted is true " in {

          val sessionData = SessionData(
            repaymentClaimDetailsAnswersOld = repaymentClaimDetailsDefaultAnswersOld,
            repaymentClaimDetailsAnswers = Some(
              RepaymentClaimDetailsAnswers(
                claimingGiftAid = Some(true),
                claimingTaxDeducted = Some(true),
                claimingUnderGiftAidSmallDonationsScheme = Some(false),
                claimingDonationsNotFromCommunityBuilding = Some(false),
                claimingDonationsCollectedInCommunityBuildings = Some(false),
                connectedToAnyOtherCharities = Some(false),
                makingAdjustmentToPreviousClaim = Some(false),
                claimingReferenceNumber = Some(true),
                claimReferenceNumber = None
              )
            )
          )

          given application: Application = applicationBuilder(sessionData = sessionData).build()

          running(application) {
            given request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(GET, routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[RepaymentClaimDetailsCheckYourAnswersView]

            status(result) shouldEqual OK

            contentAsString(result) shouldEqual view(sessionData.repaymentClaimDetailsAnswers).body
          }
        }
      }

      "should render the page correctly when some answers are missing & claimingGiftAid is true" in {

        val sessionData = SessionData(
          repaymentClaimDetailsAnswersOld = repaymentClaimDetailsDefaultAnswersOld,
          repaymentClaimDetailsAnswers = Some(
            RepaymentClaimDetailsAnswers(
              claimingGiftAid = Some(true),
              claimingTaxDeducted = None,
              claimingUnderGiftAidSmallDonationsScheme = Some(false),
              claimingDonationsNotFromCommunityBuilding = None,
              claimingDonationsCollectedInCommunityBuildings = Some(false),
              connectedToAnyOtherCharities = Some(false),
              makingAdjustmentToPreviousClaim = Some(false),
              claimingReferenceNumber = None,
              claimReferenceNumber = Some("12345678AB")
            )
          )
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RepaymentClaimDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.repaymentClaimDetailsAnswers).body

        }
      }

      "should render the page correctly when some answers are missing & claimingTaxDeducted is true" in {

        val sessionData = SessionData(
          repaymentClaimDetailsAnswersOld = repaymentClaimDetailsDefaultAnswersOld,
          repaymentClaimDetailsAnswers = Some(
            RepaymentClaimDetailsAnswers(
              claimingGiftAid = None,
              claimingTaxDeducted = Some(true),
              claimingUnderGiftAidSmallDonationsScheme = Some(false),
              claimingDonationsNotFromCommunityBuilding = None,
              claimingDonationsCollectedInCommunityBuildings = Some(false),
              connectedToAnyOtherCharities = Some(false),
              makingAdjustmentToPreviousClaim = Some(false),
              claimingReferenceNumber = None,
              claimReferenceNumber = Some("12345678AB")
            )
          )
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RepaymentClaimDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.repaymentClaimDetailsAnswers).body

        }
      }
      "should render the page correctly when some answers are missing & claimingUnderGiftAidSmallDonationsScheme missing" in {

        val sessionData = SessionData(
          repaymentClaimDetailsAnswersOld = repaymentClaimDetailsDefaultAnswersOld,
          repaymentClaimDetailsAnswers = Some(
            RepaymentClaimDetailsAnswers(
              claimingGiftAid = None,
              claimingTaxDeducted = Some(true),
              claimingUnderGiftAidSmallDonationsScheme = None,
              claimingDonationsNotFromCommunityBuilding = None,
              claimingDonationsCollectedInCommunityBuildings = Some(false),
              connectedToAnyOtherCharities = Some(false),
              makingAdjustmentToPreviousClaim = Some(false),
              claimingReferenceNumber = None,
              claimReferenceNumber = Some("12345678AB")
            )
          )
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RepaymentClaimDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.repaymentClaimDetailsAnswers).body

        }
      }

      "should render the page correctly when claimingUnderGiftAidSmallDonationsScheme & claimingGiftAid & claimingTaxDeducted are missing" in {

        val sessionData = SessionData(
          repaymentClaimDetailsAnswersOld = repaymentClaimDetailsDefaultAnswersOld,
          repaymentClaimDetailsAnswers = Some(
            RepaymentClaimDetailsAnswers(
              claimingGiftAid = None,
              claimingTaxDeducted = None,
              claimingUnderGiftAidSmallDonationsScheme = None,
              claimingDonationsNotFromCommunityBuilding = None,
              claimingDonationsCollectedInCommunityBuildings = Some(false),
              connectedToAnyOtherCharities = Some(false),
              makingAdjustmentToPreviousClaim = Some(false),
              claimingReferenceNumber = None,
              claimReferenceNumber = Some("12345678AB")
            )
          )
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RepaymentClaimDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.repaymentClaimDetailsAnswers).body

        }
      }

      "should render the page correctly when all are missing" in {

        val sessionData = SessionData(
          repaymentClaimDetailsAnswersOld = repaymentClaimDetailsDefaultAnswersOld,
          repaymentClaimDetailsAnswers = Some(
            RepaymentClaimDetailsAnswers(
              claimingGiftAid = None,
              claimingTaxDeducted = None,
              claimingUnderGiftAidSmallDonationsScheme = None,
              claimingDonationsNotFromCommunityBuilding = None,
              claimingDonationsCollectedInCommunityBuildings = None,
              connectedToAnyOtherCharities = None,
              makingAdjustmentToPreviousClaim = None,
              claimingReferenceNumber = None,
              claimReferenceNumber = None
            )
          )
        )

        given application: Application = applicationBuilder(sessionData = sessionData).build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RepaymentClaimDetailsCheckYourAnswersView]

          status(result) shouldEqual OK

          contentAsString(result) shouldEqual view(sessionData.repaymentClaimDetailsAnswers).body

        }
      }

    }
  }

  "onSubmit" - {
    "should save the claim and redirect to the next page" in {

      val sessionData = SessionData(
        repaymentClaimDetailsAnswersOld = repaymentClaimDetailsDefaultAnswersOld,
        repaymentClaimDetailsAnswers = Some(
          RepaymentClaimDetailsAnswers(
            claimingGiftAid = Some(true),
            claimingTaxDeducted = Some(true),
            claimingUnderGiftAidSmallDonationsScheme = Some(true),
            claimingReferenceNumber = Some(true),
            claimingDonationsNotFromCommunityBuilding = Some(false),
            claimingDonationsCollectedInCommunityBuildings = Some(false),
            connectedToAnyOtherCharities = Some(false),
            makingAdjustmentToPreviousClaim = Some(false),
            claimReferenceNumber = Some("12345678AB")
          )
        )
      )

      given application: Application =
        applicationBuilder(sessionData = sessionData).mockSaveClaim.build()

      running(application) {
        given request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(POST, routes.RepaymentClaimDetailsCheckYourAnswersController.onSubmit.url)

        val result = route(application, request).value

        status(result) shouldEqual SEE_OTHER

        redirectLocation(result) shouldEqual Some(
          controllers.routes.ClaimsTaskListController.onPageLoad.url
        )

      }
    }

    "should redirect to the incomplete answers page if the answers are not complete" in {

      val sessionData = SessionData(
        repaymentClaimDetailsAnswersOld = repaymentClaimDetailsDefaultAnswersOld,
        repaymentClaimDetailsAnswers = Some(
          RepaymentClaimDetailsAnswers(
            claimingGiftAid = Some(true),
            claimingTaxDeducted = None,
            claimingUnderGiftAidSmallDonationsScheme = Some(true),
            claimingDonationsNotFromCommunityBuilding = None,
            claimingDonationsCollectedInCommunityBuildings = None,
            connectedToAnyOtherCharities = None,
            makingAdjustmentToPreviousClaim = None,
            claimingReferenceNumber = Some(true),
            claimReferenceNumber = Some("12345678AB")
          )
        )
      )

      given application: Application =
        applicationBuilder(sessionData = sessionData).build()

      running(application) {
        given request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(POST, routes.RepaymentClaimDetailsCheckYourAnswersController.onSubmit.url)

        val result = route(application, request).value

        status(result) shouldEqual SEE_OTHER

        redirectLocation(result) shouldEqual Some(
          routes.RepaymentClaimDetailsIncompleteAnswersController.onPageLoad.url
        )

      }
    }

    "should redirect to CannotViewOrManageClaim page when UpdatedByAnotherUserException is thrown" in {

      val sessionData = SessionData(
        repaymentClaimDetailsAnswersOld = repaymentClaimDetailsDefaultAnswersOld,
        repaymentClaimDetailsAnswers = Some(
          RepaymentClaimDetailsAnswers(
            claimingGiftAid = Some(true),
            claimingTaxDeducted = Some(false),
            claimingUnderGiftAidSmallDonationsScheme = Some(false),
            claimingReferenceNumber = Some(true),
            claimReferenceNumber = Some("12345678AB")
          )
        )
      )

      val mockClaimsService = mock[services.ClaimsService]
      (mockClaimsService
        .save(using _: models.requests.DataRequest[?], _: uk.gov.hmrc.http.HeaderCarrier))
        .expects(*, *)
        .returning(scala.concurrent.Future.failed(UpdatedByAnotherUserException()))

      given application: Application =
        applicationBuilder(sessionData = sessionData)
          .overrides(play.api.inject.bind[services.ClaimsService].toInstance(mockClaimsService))
          .build()

      running(application) {
        val request     = FakeRequest(POST, routes.RepaymentClaimDetailsCheckYourAnswersController.onSubmit.url)
        val caught      = intercept[UpdatedByAnotherUserException](await(route(application, request).value))
        val errorResult = application.injector.instanceOf[handlers.ErrorHandler].resolveError(request, caught)

        status(errorResult) shouldEqual SEE_OTHER
        redirectLocation(errorResult) shouldEqual Some(
          controllers.organisationDetails.routes.CannotViewOrManageClaimController.onPageLoad.url
        )
      }
    }
  }
}

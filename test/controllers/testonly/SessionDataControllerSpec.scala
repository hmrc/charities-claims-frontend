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

package controllers.testonly

import play.api.test.FakeRequest
import play.api.mvc.AnyContentAsEmpty
import controllers.ControllerSpec
import play.api.Application
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.libs.json.Json
import models.{NameOfCharityRegulator, OrganisationDetailsAnswers, RepaymentClaimDetailsAnswers, SessionData}
import repositories.SessionCache
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.SessionId

class SessionDataControllerSpec extends ControllerSpec {

  "SessionDataControllerSpec" - {

    "onPageLoad" - {
      "should return OK if no session data is found" in {
        given application: Application =
          applicationBuilder()
            .configure(
              "application.router" -> "testOnlyDoNotUseInAppConf.Routes"
            )
            .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.SessionDataController.onPageLoad.url)

          val result = route(application, request).value

          status(result) shouldEqual OK
        }
      }

      "should return OK if session data is found" in {
        given application: Application =
          applicationBuilder()
            .configure(
              "application.router" -> "testOnlyDoNotUseInAppConf.Routes"
            )
            .build()

        given HeaderCarrier(sessionId = Some(SessionId("test")))

        application.injector
          .instanceOf[SessionCache]
          .store(
            SessionData(
              repaymentClaimDetailsAnswers = RepaymentClaimDetailsAnswers(claimingGiftAid = Some(true)),
              organisationDetailsAnswers =
                Some(OrganisationDetailsAnswers(nameOfCharityRegulator = Some(NameOfCharityRegulator.EnglandAndWales)))
            )
          )

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.SessionDataController.onPageLoad.url)
              .withSession(
                "sessionId" -> "test"
              )

          val result = route(application, request).value

          status(result) shouldEqual OK
        }
      }
    }

    "onSubmit" - {
      "should return OK if no session data is provided" in {
        given application: Application =
          applicationBuilder()
            .configure(
              "application.router" -> "testOnlyDoNotUseInAppConf.Routes"
            )
            .build()

        running(application) {
          given request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(POST, routes.SessionDataController.onSubmit.url)

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.SessionDataController.onPageLoad.url)
        }
      }

      "should return OK if session data is provided" in {
        given application: Application =
          applicationBuilder()
            .configure(
              "application.router" -> "testOnlyDoNotUseInAppConf.Routes"
            )
            .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.SessionDataController.onSubmit.url)
              .withFormUrlEncodedBody(
                "sessionData" -> Json
                  .toJson(
                    SessionData(
                      repaymentClaimDetailsAnswers = RepaymentClaimDetailsAnswers(claimingGiftAid = Some(true)),
                      organisationDetailsAnswers = Some(
                        OrganisationDetailsAnswers(nameOfCharityRegulator =
                          Some(NameOfCharityRegulator.EnglandAndWales)
                        )
                      )
                    )
                  )
                  .toString()
              )

          val result = route(application, request).value

          status(result) shouldEqual SEE_OTHER
          redirectLocation(result) shouldEqual Some(routes.SessionDataController.onPageLoad.url)
        }
      }

      "should return OK if malformed session data is provided" in {
        given application: Application =
          applicationBuilder()
            .configure(
              "application.router" -> "testOnlyDoNotUseInAppConf.Routes"
            )
            .build()

        running(application) {
          given request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, routes.SessionDataController.onSubmit.url)
              .withFormUrlEncodedBody(
                "sessionData" -> Json
                  .toJson(
                    SessionData(
                      repaymentClaimDetailsAnswers = RepaymentClaimDetailsAnswers(claimingGiftAid = Some(true)),
                      organisationDetailsAnswers = Some(
                        OrganisationDetailsAnswers(nameOfCharityRegulator =
                          Some(NameOfCharityRegulator.EnglandAndWales)
                        )
                      )
                    )
                  )
                  .toString()
                  .dropRight(5)
              )

          val result = route(application, request).value

          status(result) shouldEqual OK
        }
      }
    }
  }
}
